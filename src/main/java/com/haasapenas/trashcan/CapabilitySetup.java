package com.haasapenas.trashcan;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CapabilitySetup {

    public static void register(RegisterCapabilitiesEvent event){
        event.registerBlockEntity(Capabilities.Item.BLOCK, TrashCan.TRASH_BLOCK_ENTITY.get(), (trashCan, side) -> trashCan.itemHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, TrashCan.TRASH_BLOCK_ENTITY.get(), (trashCan, side) -> side == null ? null : trashCan.fluidHandler());
    }
}
