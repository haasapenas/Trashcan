package com.haasapenas.trashcan.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public abstract class BaseScreen<T extends BaseMenu> extends WidgetScreen<T> {

    protected BaseScreen(T menu, Inventory inventory, Component title){
        super(menu, inventory, title);
    }
}
