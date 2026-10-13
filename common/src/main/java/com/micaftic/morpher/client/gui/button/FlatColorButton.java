package com.micaftic.morpher.client.gui.button;

import com.micaftic.morpher.core.gui.VanillaUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

/** Compatibility widget retaining selection and tooltip support with vanilla visuals. */
public class FlatColorButton extends Button {
    private boolean selected;
    private List<Component> tooltip;

    public FlatColorButton(int x, int y, int width, int height, Component component, OnPress onPress) {
        super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
    }

    public FlatColorButton setTooltipText(String str) {
        this.tooltip = Collections.singletonList(Component.translatable(str));
        return this;
    }

    public FlatColorButton setTooltipLines(List<Component> list) {
        this.tooltip = list;
        return this;
    }

    public void renderTooltip(GuiGraphicsExtractor guiGraphics, Screen screen, int mouseX, int mouseY) {
        if (isHoveredOrFocused() && this.tooltip != null) {
            guiGraphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        VanillaUi.button(g, getX(), getY(), width, height, active, isHoveredOrFocused(), selected);
        if (width <= 4 || height <= 2) return;
        Font font = Minecraft.getInstance().font;
        Component message = getMessage();
        int textWidth = font.width(message);
        int textX = textWidth <= width - 4 ? getX() + (width - textWidth) / 2 : getX() + 2;
        int textY = getY() + (height - font.lineHeight) / 2;
        g.enableScissor(getX() + 2, getY() + 1, getX() + width - 2, getY() + height - 1);
        g.text(font, message, textX, textY, active ? 0xFFFFFFFF : 0xFFA0A0A0, true);
        g.disableScissor();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
