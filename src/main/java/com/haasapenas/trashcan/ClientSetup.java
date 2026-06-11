package com.haasapenas.trashcan;

import com.haasapenas.trashcan.screen.TrashScreen;
import com.haasapenas.trashcan.screen.FilterScreen;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientSetup {

    public static void registerScreens(RegisterMenuScreensEvent event){
        event.register(TrashCan.TRASH_MENU.get(), TrashScreen::new);
        event.register(TrashCan.FILTER_MENU.get(), FilterScreen::new);
    }
}