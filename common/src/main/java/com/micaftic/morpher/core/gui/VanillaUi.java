package com.micaftic.morpher.core.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Vanilla resources plus pixel-aligned frames for panels without a vanilla equivalent.
 * Uses the 26.3 extraction API; no immediate-mode GL or blur passes are needed.
 */
public final class VanillaUi {
    private static final Identifier BUTTON = Identifier.fromNamespaceAndPath("minecraft", "widget/button");
    private static final Identifier HOVER = Identifier.fromNamespaceAndPath("minecraft", "widget/button_highlighted");
    private static final Identifier DISABLED = Identifier.fromNamespaceAndPath("minecraft", "widget/button_disabled");
    private static final Identifier BACKGROUND = texture("gui/menu_background.png");

    private VanillaUi() { }

    private static Identifier texture(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", "textures/" + path);
    }

    public static void button(GuiGraphicsExtractor g, int x, int y, int w, int h,
                              boolean enabled, boolean hovered, boolean selected) {
        if (w < 4 || h < 4) return;
        Identifier texture = !enabled ? DISABLED : hovered ? HOVER : BUTTON;
        // Let Minecraft apply sprite nine-slicing and resource-pack metadata.
        g.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, w, h);
        if (selected && enabled) {
            outline(g, x + 1, y + 1, w - 2, h - 2, 0xFFFFFFA0);
        }
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (w < 4 || h < 4) return;
        fill(g, x + 2, y + 2, w, h, 0x80000000);
        // Tile the original material at native GUI scale, including partial edge tiles.
        for (int ty = y; ty < y + h; ty += 32) {
            for (int tx = x; tx < x + w; tx += 32) {
                int tw = Math.min(32, x + w - tx);
                int th = Math.min(32, y + h - ty);
                g.blit(BACKGROUND, tx, ty, tx + tw, ty + th, 0, tw / 32.0f, 0, th / 32.0f);
            }
        }
        fill(g, x, y, w, h, 0x99000000);
        outline(g, x, y, w, h, 0xFF101010);
        fill(g, x + 1, y + 1, w - 2, 1, 0xFF9A9A9A);
        fill(g, x + 1, y + 2, 1, h - 3, 0xFF707070);
        fill(g, x + 2, y + h - 2, w - 3, 1, 0xFF303030);
        fill(g, x + w - 2, y + 2, 1, h - 3, 0xFF303030);
    }

    public static void slot(GuiGraphicsExtractor g, int x, int y, int w, int h,
                            boolean hovered, boolean selected) {
        if (w < 4 || h < 4) return;
        fill(g, x, y, w, h, hovered ? 0xFF666666 : 0xFF373737);
        outline(g, x, y, w, h, selected ? 0xFFFFFFA0 : hovered ? 0xFFFFFFFF : 0xFF141414);
        fill(g, x + 1, y + 1, w - 2, 1, 0xFF242424);
        fill(g, x + 1, y + 1, 1, h - 2, 0xFF242424);
        fill(g, x + 1, y + h - 2, w - 2, 1, 0xFFAAAAAA);
        fill(g, x + w - 2, y + 1, 1, h - 2, 0xFFAAAAAA);
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        fill(g, x, y, w, 1, color);
        fill(g, x, y + h - 1, w, 1, color);
        fill(g, x, y, 1, h, color);
        fill(g, x + w - 1, y, 1, h, color);
    }

    private static void fill(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w > 0 && h > 0) g.fillGradient(x, y, x + w, y + h, color, color);
    }
}
