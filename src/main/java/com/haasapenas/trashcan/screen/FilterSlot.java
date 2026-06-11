package com.haasapenas.trashcan.screen;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FilterSlot extends Slot {

    private final FilterMenu menu;

    public FilterSlot(FilterMenu menu, int index, int x, int y){
        super(new SimpleContainer(1), index, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack){
        return FilterMenu.isTrashFilter(stack);
    }

    @Override
    public ItemStack getItem(){
        return this.menu.validateObjectOrClose() ? this.menu.object.itemFilterStack : ItemStack.EMPTY;
    }

    @Override
    public void set(ItemStack stack){
        if(!this.menu.validateObjectOrClose())
            return;

        ItemStack filter = stack.copy();
        filter.setCount(filter.isEmpty() ? 0 : 1);
        this.menu.object.itemFilterStack = filter;
        this.menu.object.dataChanged();
    }

    @Override
    public int getMaxStackSize(){
        return 1;
    }

    @Override
    public ItemStack remove(int amount){
        if(!this.menu.validateObjectOrClose() || this.menu.object.itemFilterStack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack removed = this.menu.object.itemFilterStack.split(amount);
        if(this.menu.object.itemFilterStack.isEmpty())
            this.menu.object.itemFilterStack = ItemStack.EMPTY;
        this.menu.object.dataChanged();
        return removed;
    }

    @Override
    public void setChanged(){
        if(this.menu.validateObjectOrClose())
            this.menu.object.dataChanged();
    }
}