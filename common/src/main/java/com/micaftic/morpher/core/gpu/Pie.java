package com.micaftic.morpher.core.gpu;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Bounded radial-strip renderer adapted from YSM-Multiplayer's
 * AnimationRouletteScreen at 430a593 (MIT, Copyright (c) 2026 OpenYSM).
 * See META-INF/licenses/YSM-Multiplayer.txt for the upstream notice.
 *
 * Keeps the 26.3 extraction command stream: no immediate GL and no per-pixel
 * coverage arrays. An eighth-circle uses 24 strips, regardless of GUI scale.
 */
public final class Pie {
    public static final float tau = (float) (Math.PI * 2.0);
    private static final int STRIPS_PER_CIRCLE = 192;
    private static final float COORDINATE_SCALE = 16.0f;

    private Pie() { }

    public static void draw(GuiGraphicsExtractor graphics, float cx, float cy, float inner,
                            float outer, float start, float end, int color) {
        draw(graphics, cx, cy, inner, outer, start, end, color, 1.0f);
    }

    public static void draw(GuiGraphicsExtractor graphics, float cx, float cy, float inner,
                            float outer, float start, float end, int color, float feather) {
        if (!Float.isFinite(cx) || !Float.isFinite(cy) || !Float.isFinite(inner)
                || !Float.isFinite(outer) || outer <= Math.max(0, inner) || (color >>> 24) == 0) return;
        int count = segmentCount(start, end);
        if (count == 0) return;
        float span = Math.copySign(Math.min(tau, Math.abs(end - start)), end - start);
        float step = span / count;
        float halfWidth = outer * (float) Math.sin(Math.abs(step) / 2.0f) * 1.05f;
        // Subpixel local coordinates avoid the upstream ceil-to-GUI-pixel width
        // becoming disproportionately thick at large GUI scales.
        int y = Math.max(1, (int) Math.ceil(halfWidth * COORDINATE_SCALE));
        int x0 = Math.round(Math.max(0, inner) * COORDINATE_SCALE);
        int x1 = Math.round(outer * COORDINATE_SCALE);
        var pose = graphics.pose();
        pose.pushMatrix();
        try {
            pose.translate(cx, cy);
            pose.scale(1 / COORDINATE_SCALE, 1 / COORDINATE_SCALE);
            for (int i = 0; i < count; i++) {
                pose.pushMatrix();
                try {
                    pose.rotate(start + (i + 0.5f) * step);
                    graphics.fill(x0, -y, x1, y, color);
                } finally {
                    pose.popMatrix();
                }
            }
        } finally {
            pose.popMatrix();
        }
    }

    static int segmentCount(float start, float end) {
        if (!Float.isFinite(start) || !Float.isFinite(end)) return 0;
        float span = Math.min(tau, Math.abs(end - start));
        if (span < 0.00001f) return 0;
        return Math.max(1, Math.min(STRIPS_PER_CIRCLE,
                (int) Math.ceil(span / tau * STRIPS_PER_CIRCLE)));
    }
}
