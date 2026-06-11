package com.haasapenas.trashcan.jei;

import com.haasapenas.trashcan.TrashCan;
import com.haasapenas.trashcan.screen.FilterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class JeiTrashFilterIntegration implements IModPlugin {

    private static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath(TrashCan.MOD_ID, "filter_ghost_items");

    @Override
    public Identifier getPluginUid(){
        return PLUGIN_ID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration){
        registration.addGhostIngredientHandler(FilterScreen.class, new JeiFilterDropHandler());
    }
}