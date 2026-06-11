package com.haasapenas.trashcan.screen;

import com.haasapenas.trashcan.TrashBlockEntity;
import com.haasapenas.trashcan.NetworkHandler;
import com.haasapenas.trashcan.filter.FilterData;
import com.haasapenas.trashcan.packet.PacketUpdateFilterEntry;
import com.haasapenas.trashcan.packet.PacketSetFilterMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public class FilterScreen extends BaseScreen<FilterMenu> {

    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("trashcan", "textures/filterpage.png");
    private static final int FILTER_GRID_X = 24;
    private static final int FILTER_GRID_Y = 5;
    private static final int FILTER_GRID_COLUMNS = 6;
    private static final int FILTER_GRID_ROWS = 4;
    private static final int FILTER_GRID_VISIBLE = FILTER_GRID_COLUMNS * FILTER_GRID_ROWS;
    private static final int FILTER_SLOT_SIZE = 19;
    private static final int FILTER_ITEM_OFFSET = 3;
    private static final int SELECTED_SLOT_SOURCE_X = 180;
    private static final int SELECTED_SLOT_SOURCE_Y = 6;
    private static final int SELECTED_SLOT_SIZE = 16;
    private static final int FILTER_BUTTON_Y = 84;
    private static final int MODE_BUTTON_X = 5;
    private static final int MODE_BUTTON_Y = 24;
    private Button removeButton;
    private ModeButton modeButton;
    private int scrollOffset;
    private int selectedFilter = -1;

    public FilterScreen(FilterMenu menu, Inventory inventory, Component title){
        super(menu, inventory, title);
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 106;
    }

    @Override
    protected void init(){
        super.init();
        this.inventoryLabelY = 106;
    }

    @Override
    protected Identifier getBackground(){
        return BACKGROUND;
    }

    @Override
    protected void addWidgets(TrashBlockEntity entity){
        this.modeButton = this.addRelativeWidget(new ModeButton(MODE_BUTTON_X, MODE_BUTTON_Y, this::toggleWhitelistMode));
        this.removeButton = this.addRelativeWidget(Button.builder(Component.literal("Delete"), button -> this.removeSelectedFilter()).bounds(FILTER_GRID_X, FILTER_BUTTON_Y, 62, 18).tooltip(Tooltip.create(Component.literal("Delete"))).build());
        this.updateButtons();
    }

    @Override
    protected void update(TrashBlockEntity entity){
        this.updateButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks){
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        this.drawFilterItems(graphics);
    }

    @Override
    protected void drawText(GuiGraphicsExtractor graphics, TrashBlockEntity entity){
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY){
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick){
        if(!this.menu.hasFilter())
            return super.mouseClicked(event, doubleClick);

        int mouseX = (int)event.x();
        int mouseY = (int)event.y();
        if(this.isFilterGridHovered(mouseX, mouseY)){
            ItemStack carried = this.menu.getCarried();
            if(!carried.isEmpty()){
                this.addFilter(carried);
                return true;
            }

            int clicked = this.getClickedFilter(mouseX, mouseY);
            if(clicked >= 0){
                this.selectedFilter = clicked;
                return true;
            }
        }

        if(event.hasShiftDown()){
            Slot slot = this.getHoveredSlot();
            if(slot != null && slot != this.menu.getSlot(FilterMenu.FILTER_SLOT) && !slot.getItem().isEmpty()){
                this.addFilter(slot.getItem());
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    public void addFilterFromJei(ItemStack stack){
        if(!this.menu.hasFilter() || stack.isEmpty())
            return;
        this.addFilter(stack);
    }

    public void addFluidFilterFromJei(FluidStack fluid){
        if(!this.menu.hasLiquidFilter() || fluid.isEmpty())
            return;
        this.addFluidFilter(fluid);
    }

    public Rect2i getJeiFilterTargetArea(){
        return new Rect2i(this.leftPos + FILTER_GRID_X, this.topPos + FILTER_GRID_Y, FILTER_GRID_COLUMNS * FILTER_SLOT_SIZE + 2, FILTER_GRID_ROWS * FILTER_SLOT_SIZE + 2);
    }

    private void addFilter(ItemStack stack){
        if(this.menu.hasLiquidFilter()){
            this.addFluidFilter(stack);
            return;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int existingSlot = this.findFilterSlot(itemId);
        if(existingSlot >= 0){
            this.selectedFilter = existingSlot;
            this.ensureFilterVisible(existingSlot);
            return;
        }

        int emptySlot = this.firstEmptyFilterSlot();
        if(emptySlot >= 0){
            this.setFilter(emptySlot, itemId, this.getCustomDataString(stack));
            this.selectedFilter = emptySlot;
            this.ensureFilterVisible(emptySlot);
        }
    }

    private void addFluidFilter(ItemStack stack){
        FluidStack fluid = FluidUtil.getFirstStackContained(stack);
        if(fluid.isEmpty())
            return;
        this.addFluidFilter(fluid);
    }

    private void addFluidFilter(FluidStack fluid){
        String fluidId = BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString();
        int existingSlot = this.findFluidFilterSlot(fluidId);
        if(existingSlot >= 0){
            this.selectedFilter = existingSlot;
            this.ensureFilterVisible(existingSlot);
            return;
        }

        int emptySlot = this.firstEmptyFilterSlot();
        if(emptySlot >= 0){
            this.setFluidFilter(emptySlot, fluidId);
            this.selectedFilter = emptySlot;
            this.ensureFilterVisible(emptySlot);
        }
    }


    private void removeSelectedFilter(){
        if(this.selectedFilter < 0)
            return;

        if(this.menu.hasLiquidFilter())
            this.setFluidFilter(this.selectedFilter, "");
        else
            this.setFilter(this.selectedFilter, "", "");
        this.selectedFilter = -1;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll()));
    }

    private void toggleWhitelistMode(){
        boolean whitelist = !FilterData.isWhitelist(this.menu.getBlockEntity().itemFilterStack);
        if(this.menu.getBlockEntity() != null)
            FilterData.setWhitelist(this.menu.getBlockEntity().itemFilterStack, whitelist);
        NetworkHandler.sendToServer(new PacketSetFilterMode(this.menu.getBlockEntityPos(), whitelist));
        this.updateButtons();
    }

    private void setFilter(int slot, String itemId, String nbtData){
        if(this.menu.getBlockEntity() != null)
            FilterData.setFilter(this.menu.getBlockEntity().itemFilterStack, slot, itemId, nbtData);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll()));
        NetworkHandler.sendToServer(new PacketUpdateFilterEntry(this.menu.getBlockEntityPos(), slot, itemId, nbtData));
    }

    private void setFluidFilter(int slot, String fluidId){
        if(this.menu.getBlockEntity() != null)
            FilterData.setFluidFilter(this.menu.getBlockEntity().itemFilterStack, slot, fluidId);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll()));
        NetworkHandler.sendToServer(new PacketUpdateFilterEntry(this.menu.getBlockEntityPos(), slot, fluidId, ""));
    }

    private int getClickedFilter(int mouseX, int mouseY){
        int column = (mouseX - this.leftPos - FILTER_GRID_X) / FILTER_SLOT_SIZE;
        int row = (mouseY - this.topPos - FILTER_GRID_Y) / FILTER_SLOT_SIZE;
        if(column < 0 || column >= FILTER_GRID_COLUMNS || row < 0 || row >= FILTER_GRID_ROWS)
            return -1;

        int visible = row * FILTER_GRID_COLUMNS + column + this.scrollOffset;
        int count = 0;
        for(int slot = 0; slot < FilterData.SLOT_COUNT; slot++){
            if(!this.hasEntry(slot))
                continue;
            if(count == visible)
                return slot;
            count++;
        }
        return -1;
    }

    private int firstEmptyFilterSlot(){
        for(int slot = 0; slot < FilterData.SLOT_COUNT; slot++){
            if(!this.hasEntry(slot))
                return slot;
        }
        return -1;
    }

    private int findFilterSlot(String itemId){
        Identifier id = Identifier.tryParse(itemId);
        if(id == null)
            return -1;
        String normalized = id.toString();
        for(int slot = 0; slot < FilterData.SLOT_COUNT; slot++){
            if(normalized.equals(FilterData.getFilterId(this.menu.getBlockEntity().itemFilterStack, slot)))
                return slot;
        }
        return -1;
    }

    private int findFluidFilterSlot(String fluidId){
        Identifier id = Identifier.tryParse(fluidId);
        if(id == null)
            return -1;
        String normalized = id.toString();
        for(int slot = 0; slot < FilterData.SLOT_COUNT; slot++){
            if(normalized.equals(FilterData.getFluidFilterId(this.menu.getBlockEntity().itemFilterStack, slot)))
                return slot;
        }
        return -1;
    }

    private void ensureFilterVisible(int filterSlot){
        int visibleIndex = this.visibleFilterIndex(filterSlot);
        if(visibleIndex < this.scrollOffset)
            this.scrollOffset = visibleIndex;
        else if(visibleIndex >= this.scrollOffset + FILTER_GRID_VISIBLE)
            this.scrollOffset = visibleIndex - FILTER_GRID_VISIBLE + 1;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.maxScroll()));
    }

    private int visibleFilterIndex(int filterSlot){
        int count = 0;
        for(int slot = 0; slot < filterSlot; slot++){
            if(this.hasEntry(slot))
                count++;
        }
        return count;
    }

    private String getCustomDataString(ItemStack stack){
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().isEmpty() ? "" : customData.copyTag().toString();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY){
        if(this.isFilterGridHovered((int)mouseX, (int)mouseY)){
            int maxScroll = this.maxScroll();
            if(maxScroll > 0){
                this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int)Math.signum(deltaY) * FILTER_GRID_COLUMNS));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private void drawFilterItems(GuiGraphicsExtractor graphics){
        if(!this.menu.hasFilter())
            return;

        int visibleIndex = 0;
        int drawn = 0;
        for(int slot = 0; slot < FilterData.SLOT_COUNT; slot++){
            if(!this.hasEntry(slot))
                continue;
            if(visibleIndex++ < this.scrollOffset)
                continue;
            if(drawn >= FILTER_GRID_VISIBLE)
                break;

            int column = drawn % FILTER_GRID_COLUMNS;
            int row = drawn / FILTER_GRID_COLUMNS;
            int x = this.leftPos + FILTER_GRID_X + column * FILTER_SLOT_SIZE;
            int y = this.topPos + FILTER_GRID_Y + row * FILTER_SLOT_SIZE;
            if(slot == this.selectedFilter)
                graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x + FILTER_ITEM_OFFSET, y + FILTER_ITEM_OFFSET, SELECTED_SLOT_SOURCE_X, SELECTED_SLOT_SOURCE_Y, SELECTED_SLOT_SIZE, SELECTED_SLOT_SIZE, 256, 256);
            if(this.menu.hasLiquidFilter())
                this.drawFluid(graphics, FilterData.getFluidFilter(this.menu.getBlockEntity().itemFilterStack, slot), x + FILTER_ITEM_OFFSET, y + FILTER_ITEM_OFFSET);
            else
                graphics.item(FilterData.getFilter(this.menu.getBlockEntity().itemFilterStack, slot), x + FILTER_ITEM_OFFSET, y + FILTER_ITEM_OFFSET, 0);
            drawn++;
        }
    }

    private void drawFluid(GuiGraphicsExtractor graphics, FluidStack fluid, int x, int y){
        if(fluid.isEmpty())
            return;

        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
        var material = model.stillMaterial();
        if(material == null)
            return;

        var tintSource = model.fluidTintSource();
        int tint = tintSource == null ? -1 : tintSource.colorAsStack(fluid);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, material.sprite(), x, y, 16, 16, tint);
    }

    private boolean isFilterGridHovered(int mouseX, int mouseY){
        int x = this.leftPos + FILTER_GRID_X;
        int y = this.topPos + FILTER_GRID_Y;
        return mouseX >= x && mouseX < x + FILTER_GRID_COLUMNS * FILTER_SLOT_SIZE && mouseY >= y && mouseY < y + FILTER_GRID_ROWS * FILTER_SLOT_SIZE;
    }

    private int maxScroll(){
        return Math.max(0, this.filterCount() - FILTER_GRID_VISIBLE);
    }

    private void updateButtons(){
        boolean hasFilter = this.menu.hasFilter();
        boolean whitelist = hasFilter && FilterData.isWhitelist(this.menu.getBlockEntity().itemFilterStack);
        if(this.modeButton != null)
            this.modeButton.update(whitelist, hasFilter);
        if(this.removeButton != null)
            this.removeButton.active = this.selectedFilter >= 0;
    }

    private int filterCount(){
        if(!this.menu.hasFilter())
            return 0;
        if(this.selectedFilter >= 0 && !this.hasEntry(this.selectedFilter))
            this.selectedFilter = -1;
        int count = 0;
        for(int slot = 0; slot < FilterData.SLOT_COUNT; slot++){
            if(this.hasEntry(slot))
                count++;
        }
        return count;
    }

    private boolean hasEntry(int slot){
        if(this.menu.hasLiquidFilter())
            return !FilterData.getFluidFilter(this.menu.getBlockEntity().itemFilterStack, slot).isEmpty();
        return !FilterData.getFilter(this.menu.getBlockEntity().itemFilterStack, slot).isEmpty();
    }

    @Override
    protected void extractExtraTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY, TrashBlockEntity entity){
        int filter = this.getClickedFilter(mouseX, mouseY);
        if(filter >= 0){
            if(this.menu.hasLiquidFilter()){
                FluidStack fluid = FilterData.getFluidFilter(entity.itemFilterStack, filter);
                if(!fluid.isEmpty())
                    graphics.setTooltipForNextFrame(this.font, fluid.getHoverName(), mouseX, mouseY);
            }else{
                ItemStack stack = FilterData.getFilter(entity.itemFilterStack, filter);
                if(!stack.isEmpty())
                    graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
            }
        }
    }
}

