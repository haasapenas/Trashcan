package com.haasapenas.trashcan.screen;

import com.haasapenas.trashcan.TrashBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
public abstract class WidgetScreen<T extends BaseMenu> extends AbstractContainerScreen<T> {

    protected WidgetScreen(T menu, Inventory inventory, Component title){
        super(menu, inventory, title, menu.width, menu.height);
        this.titleLabelX = 0;
        this.inventoryLabelX = 21;
    }

    @Override
    protected void init(){
        super.init();
        this.inventoryLabelY = this.imageHeight - 94;
        TrashBlockEntity entity = this.menu.getBlockEntity();
        if(entity != null)
            this.addWidgets(entity);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick){
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getBackground(), this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY){
        graphics.text(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, 6, -12566464, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
        TrashBlockEntity entity = this.menu.getBlockEntity();
        if(entity != null)
            this.drawText(graphics, entity);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick){
        TrashBlockEntity entity = this.menu.getBlockEntity();
        if(entity != null)
            this.update(entity);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if(entity != null)
            this.extractExtraTooltips(graphics, mouseX, mouseY, entity);
    }

    protected <W extends AbstractWidget> W addRelativeWidget(W widget){
        widget.setX(this.leftPos + widget.getX());
        widget.setY(this.topPos + widget.getY());
        return this.addRenderableWidget(widget);
    }

    public int left(){
        return this.leftPos;
    }

    public int top(){
        return this.topPos;
    }

    protected abstract Identifier getBackground();

    protected void addWidgets(TrashBlockEntity entity){
    }

    protected void update(TrashBlockEntity entity){
    }

    protected void extractExtraTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY, TrashBlockEntity entity){
    }

    protected abstract void drawText(GuiGraphicsExtractor graphics, TrashBlockEntity entity);

    @Override
    public boolean keyPressed(KeyEvent event){
        this.updateModifiers(event);
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event){
        this.updateModifiers(event);
        return super.keyReleased(event);
    }

    protected void updateModifiers(KeyEvent event){
    }
}
