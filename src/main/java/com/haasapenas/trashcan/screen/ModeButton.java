package com.haasapenas.trashcan.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ModeButton extends AbstractButton {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("trashcan", "textures/filterpage.png");
    private static final int BLACKLIST_ICON_X = 180;
    private static final int BLACKLIST_ICON_Y = 23;
    private static final int WHITELIST_ICON_X = 180;
    private static final int WHITELIST_ICON_Y = 37;
    private static final int BUTTON_SIZE = 18;
    private static final int ICON_SIZE = 16;

    private final Runnable onToggle;
    private boolean whitelist;

    public ModeButton(int x, int y, Runnable onToggle){
        super(x, y, BUTTON_SIZE, BUTTON_SIZE, Component.empty());
        this.onToggle = onToggle;
        this.refreshMessage();
    }

    public void update(boolean whitelist, boolean enabled){
        this.whitelist = whitelist;
        this.active = enabled;
        this.refreshMessage();
    }

    @Override
    public void onPress(InputWithModifiers input){
        this.onToggle.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick){
        int iconX = this.whitelist ? WHITELIST_ICON_X : BLACKLIST_ICON_X;
        int iconY = this.whitelist ? WHITELIST_ICON_Y : BLACKLIST_ICON_Y;
        this.extractDefaultSprite(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + 2, this.getY() + 1, iconX, iconY, ICON_SIZE, ICON_SIZE, 256, 256);
    }

    private void refreshMessage(){
        Component message = Component.translatable("trashcan.gui.whitelist." + (this.whitelist ? "on" : "off"));
        this.setMessage(message);
        this.setTooltip(Tooltip.create(message));
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output){
        this.defaultButtonNarrationText(output);
    }
}