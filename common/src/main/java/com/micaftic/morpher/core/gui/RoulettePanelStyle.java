package com.micaftic.morpher.core.gui;

import com.micaftic.morpher.YesSteveModel;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class RoulettePanelStyle {
    public static final Identifier MODEL_PANEL_ICONS = com.micaftic.morpher.core.api.resource.ResourceApi.nativeId(YesSteveModel.MOD_ID, "texture/model_panel_icons.png");

    public static final int BG = 0xA0101010;
    public static final int PANEL = 0xFF373737;
    public static final int PANEL_HOVER = 0xFF666666;
    public static final int PANEL_ACTIVE = 0xFF555555;
    public static final int GLASS = 0xFF303030;
    public static final int BORDER = 0xFF808080;
    public static final int RED = 0xFFE05252;
    public static final int RED_SOFT = 0x77E05252;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFFB0B0B0;
    public static final int ICON = 18;

    private RoulettePanelStyle() {
    }

    public enum Glyph {
        MODEL(0, 0),
        SETTINGS(32, 0),
        ROULETTE(80, 0),
        APPLY(0, 16),
        RELOAD(48, 16),
        UP(64, 16),
        ROOT(80, 16),
        CLEAR(48, 32),
        CANCEL(64, 32),
        CLOSE(80, 32),
        SAVE(96, 32),
        DELETE(112, 32),
        CREATE(0, 48),
        MOVE(16, 48),
        PLUS(32, 48),
        MINUS(48, 48),
        INFO(96, 48),
        CHECK(112, 48);

        public final int u;
        public final int v;

        Glyph(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    public static void fill(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fillGradient(x, y, x + w, y + h, color, color);
    }

    public static void border(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        fill(g, x, y, w, 1, color);
        fill(g, x, y + h - 1, w, 1, color);
        fill(g, x, y, 1, h, color);
        fill(g, x + w - 1, y, 1, h, color);
    }

    public static void glassPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        VanillaUi.panel(g, x, y, w, h);
    }

    public static void secondaryGlassPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        VanillaUi.panel(g, x, y, w, h);
    }

    public static void blurGlass(GuiGraphicsExtractor g, int x, int y, int w, int h, int tint, float radius) {
        if (w <= 0 || h <= 0) {
            return;
        }
        // Retained for callers on secondary screens; vanilla panels do not use blur.
        VanillaUi.panel(g, x, y, w, h);
    }

    public static void iconButton(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y, Glyph glyph, boolean active) {
        boolean hover = active && inside(mouseX, mouseY, x, y, ICON, ICON);
        VanillaUi.button(g, x, y, ICON, ICON, active, hover, false);
        drawIcon(g, glyph, x + 1, y + 1);
    }

    public static void drawIcon(GuiGraphicsExtractor g, Glyph glyph, int x, int y) {
        g.blit(MODEL_PANEL_ICONS, x, y, x + 16, y + 16,
                glyph.u / 128.0f, (glyph.u + 16) / 128.0f,
                glyph.v / 64.0f, (glyph.v + 16) / 64.0f);
    }

    public static void drawCentered(GuiGraphicsExtractor g, Font font, Component text, int centerX, int y, int color) {
        g.text(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    public static String trim(Font font, String value, int maxWidth) {
        String text = value == null ? "" : value;
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int keep = text.length();
        while (keep > 0 && font.width(text.substring(0, keep) + ellipsis) > maxWidth) {
            keep--;
        }
        return text.substring(0, Math.max(0, keep)) + ellipsis;
    }

    public static boolean inside(double px, double py, int x, int y, int w, int h) {
        return px >= x && py >= y && px < x + w && py < y + h;
    }
}
