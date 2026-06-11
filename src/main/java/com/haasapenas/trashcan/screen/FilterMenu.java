package com.haasapenas.trashcan.screen;

import com.haasapenas.trashcan.TrashBlockEntity;
import com.haasapenas.trashcan.TrashCan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FilterMenu extends BaseMenu {

    public static final int FILTER_SLOT = 0;

    public FilterMenu(int containerId, Player player, BlockPos pos){
        super(TrashCan.FILTER_MENU.get(), containerId, player, pos, 176, 198, 8, 118);
    }

    @Override
    protected void addSlots(Player player, TrashBlockEntity entity){
        this.addSlot(new FilterSlot(this, FILTER_SLOT, 6, 6));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index){
        if(!this.validateObjectOrClose())
            return ItemStack.EMPTY;

        if(index == FILTER_SLOT){
            var slot = this.getSlot(FILTER_SLOT);
            ItemStack stack = slot.getItem();
            if(stack.isEmpty())
                return ItemStack.EMPTY;

            ItemStack moved = stack.copy();
            if(!this.moveItemStackTo(stack, 1, this.slots.size(), true))
                return ItemStack.EMPTY;

            if(stack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();

            return moved;
        }

        ItemStack stack = this.getSlot(index).getItem();
        if(this.object.itemFilterStack.isEmpty() && this.getSlot(FILTER_SLOT).mayPlace(stack)){
            ItemStack moved = stack.copy();
            this.getSlot(FILTER_SLOT).set(stack.split(1));
            if(stack.isEmpty())
                this.getSlot(index).set(ItemStack.EMPTY);
            else
                this.getSlot(index).setChanged();
            return moved;
        }

        return ItemStack.EMPTY;
    }

    public boolean hasItemFilter(){
        return this.validateObjectOrClose() && this.object.itemFilterStack.is(TrashCan.FILTER_ITEM.get());
    }

    public boolean hasLiquidFilter(){
        return this.validateObjectOrClose() && this.object.itemFilterStack.is(TrashCan.LIQUID_FILTER_ITEM.get());
    }

    public boolean hasFilter(){
        return this.hasItemFilter() || this.hasLiquidFilter();
    }

    public static boolean isTrashFilter(ItemStack stack){
        return !stack.isEmpty() && (stack.is(TrashCan.FILTER_ITEM.get()) || stack.is(TrashCan.LIQUID_FILTER_ITEM.get()));
    }
}
