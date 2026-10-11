package com.micaftic.morpher.client.upload.picker;

import com.micaftic.morpher.YesSteveModel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.sdl.SDLDialog.SDL_ShowOpenFileDialog;
import static org.lwjgl.sdl.SDLError.SDL_GetError;

/**
 * Minecraft 26.3 ships SDL instead of tinyfd and forces AWT into headless mode.
 * Use the game's SDL window and native file dialog without adding native libraries.
 */
public final class SdlPickerBackend {
    private static final AtomicLong REQUEST_IDS = new AtomicLong();
    private static final AtomicReference<Request> ACTIVE = new AtomicReference<>();

    static boolean isPicking() {
        return ACTIVE.get() != null;
    }

    static void cancelPicking() {
        Request request = ACTIVE.get();
        if (request != null) {
            // SDL cannot dismiss an open dialog. Ignore its eventual result, but retain
            // the request until the callback so a second native dialog cannot overlap it.
            request.cancelled.set(true);
        }
    }

    static boolean tryStartSystemPicker() {
        if (FilePickerCoordinator.isAndroidRuntime()) {
            return false;
        }
        Request request = null;
        try {
            Minecraft client = Minecraft.getInstance();
            if (client == null || !client.isSameThread()) {
                throw new IllegalStateException("SDL file dialog must open on the Minecraft main thread");
            }
            long window = client.getWindow().handle();
            if (window == MemoryUtil.NULL) {
                throw new IllegalStateException("Minecraft SDL window is unavailable");
            }
            request = new Request(REQUEST_IDS.incrementAndGet());
            if (!ACTIVE.compareAndSet(null, request)) {
                FilePickerCoordinator.setError(Component.translatable("gui.sparkle_morpher.import.error.picker_open"));
                return true;
            }
            FilePickerCoordinator.clearLastError();
            SDL_ShowOpenFileDialog(NativeDialog.CALLBACK, request.id, window,
                    NativeDialog.FILTERS, (CharSequence) null, true);
            FilePickerCoordinator.rememberPickerProbe("SDL system file dialog opened");
            return true;
        } catch (Throwable t) {
            if (request != null) {
                ACTIVE.compareAndSet(request, null);
            }
            FilePickerCoordinator.rememberPickerProbe("SDL file dialog unavailable: " + FilePickerCoordinator.safeMessage(t));
            return false;
        }
    }

    private static void onSelection(long requestId, long fileList, int filter) {
        Request request = ACTIVE.get();
        if (request == null || request.id != requestId) {
            return;
        }
        // No exception may escape an FFM/native callback. SDL owns fileList and frees
        // it when this callback returns, so copy the UTF-8 paths before deferring IO.
        try {
            if (fileList == MemoryUtil.NULL) {
                finish(request, List.of(), FilePickerCoordinator.error(
                        "gui.sparkle_morpher.import.error.open_picker", SDL_GetError()));
                return;
            }
            List<String> paths = new ArrayList<>();
            for (int index = 0; ; index++) {
                long path = MemoryUtil.memGetAddress(fileList + (long) index * Pointer.POINTER_SIZE);
                if (path == MemoryUtil.NULL) {
                    break;
                }
                paths.add(MemoryUtil.memUTF8(path));
            }
            if (paths.isEmpty()) {
                finish(request, List.of(), Component.translatable("gui.sparkle_morpher.import.error.cancelled"));
                return;
            }
            CompletableFuture.runAsync(() -> readSelection(request, paths));
        } catch (Throwable t) {
            // Reporting is guarded too: the VM must always be able to return to SDL.
            try {
                finish(request, List.of(), FilePickerCoordinator.error(
                        "gui.sparkle_morpher.import.error.open_picker", FilePickerCoordinator.safeMessage(t)));
                YesSteveModel.LOGGER.warn("[SM] SDL file dialog callback failed", t);
            } catch (Throwable ignored) {
                ACTIVE.compareAndSet(request, null);
            }
        }
    }

    private static void readSelection(Request request, List<String> paths) {
        try {
            List<FilePickerCoordinator.PickedFile> files = new ArrayList<>(paths.size());
            for (String selected : paths) {
                if (request.cancelled.get()) {
                    finish(request, List.of(), null);
                    return;
                }
                Path path = Path.of(selected);
                String name = path.getFileName().toString();
                if (!FilePickerCoordinator.isImportFileName(name)) {
                    finish(request, List.of(), Component.translatable("gui.sparkle_morpher.import.error.invalid_extension"));
                    return;
                }
                files.add(new FilePickerCoordinator.PickedFile(name, Files.readAllBytes(path)));
            }
            finish(request, files, null);
        } catch (Throwable t) {
            finish(request, List.of(), FilePickerCoordinator.error(
                    "gui.sparkle_morpher.import.error.read_file", FilePickerCoordinator.safeMessage(t)));
            YesSteveModel.LOGGER.warn("[SM] Failed to read SDL file selection", t);
        }
    }

    private static void finish(Request request, List<FilePickerCoordinator.PickedFile> files, Component error) {
        synchronized (FilePickerCoordinator.class) {
            if (!ACTIVE.compareAndSet(request, null) || request.cancelled.get()) {
                return;
            }
            if (error != null) {
                FilePickerCoordinator.setError(error);
            } else {
                FilePickerCoordinator.completeFiles(files);
            }
        }
    }

    private static final class Request {
        private final long id;
        private final AtomicBoolean cancelled = new AtomicBoolean();

        private Request(long id) {
            this.id = id;
        }
    }

    private static final class NativeDialog {
        // SDL may call back on another thread long after the opener returns. These
        // small, shared native resources live for the process lifetime: never put
        // filters on MemoryStack or free a callback stub while it is executing.
        private static final ByteBuffer LABEL = MemoryUtil.memUTF8(FilePickerCoordinator.FILE_FILTER_DESCRIPTION);
        private static final ByteBuffer PATTERN = MemoryUtil.memUTF8("ysm;zip;bbmodel;gltf;glb");
        private static final SDL_DialogFileFilter.Buffer FILTERS = SDL_DialogFileFilter.calloc(1)
                .put(0, SDL_DialogFileFilter.create().set(LABEL, PATTERN));
        private static final SDL_DialogFileCallback CALLBACK = SDL_DialogFileCallback.create(SdlPickerBackend::onSelection);
    }

    private SdlPickerBackend() {
    }
}
