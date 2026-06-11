package com.haasapenas.trashcan.screen;

import com.haasapenas.trashcan.TrashBlockEntity;
import com.haasapenas.trashcan.NetworkHandler;
import com.haasapenas.trashcan.packet.PacketOpenFilter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
public class TrashScreen extends BaseScreen<TrashMenu> {

    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("trashcan", "textures/trashcan.png");
    private static final int CONFIG_BUTTON_X = 178;
    private static final int CONFIG_BUTTON_Y = 4;
    private static final int CONFIG_BUTTON_SIZE = 18;

    public TrashScreen(TrashMenu menu, Inventory inventory, Component title){
        super(menu, inventory, title);
        this.inventoryLabelX = 8;
    }

    @Override
    protected void addWidgets(TrashBlockEntity entity){
    }

    @Override
    protected void update(TrashBlockEntity entity){
    }

    @Override
    protected Identifier getBackground(){
        return BACKGROUND;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks){
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void drawText(GuiGraphicsExtractor graphics, TrashBlockEntity entity){
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick){
        if(this.isConfigButtonHovered((int)event.x(), (int)event.y())){
            NetworkHandler.sendToServer(new PacketOpenFilter(this.menu.getBlockEntityPos()));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isConfigButtonHovered(int mouseX, int mouseY){
        int x = this.leftPos + CONFIG_BUTTON_X;
        int y = this.topPos + CONFIG_BUTTON_Y;
        return mouseX >= x && mouseX < x + CONFIG_BUTTON_SIZE && mouseY >= y && mouseY < y + CONFIG_BUTTON_SIZE;
    }
}
