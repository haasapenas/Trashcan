package com.haasapenas.trashcan.screen;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class DiscardSlot extends Slot {

    private final Predicate<ItemStack> validator;

    public DiscardSlot(int index, int x, int y, Predicate<ItemStack> validator){
        super(new SimpleContainer(1), index, x, y);
        this.validator = validator;
    }

    @Override
    public boolean mayPlace(ItemStack stack){
        return this.validator.test(stack);
    }

    @Override
    public ItemStack getItem(){
        return ItemStack.EMPTY;
    }

    @Override
    public void set(ItemStack stack){
    }

    @Override
    public void setChanged(){
    }

    @Override
    public ItemStack remove(int amount){
        return ItemStack.EMPTY;
    }
}