package com.haasapenas.trashcan;

import com.haasapenas.trashcan.screen.TrashMenu;
import com.haasapenas.trashcan.screen.BaseMenu;
import com.haasapenas.trashcan.screen.FilterMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TrashCan.MOD_ID)
public class TrashCan {

    public static final String MOD_ID = "trashcan";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    public static final DeferredBlock<TrashBlock> TRASH_BLOCK = BLOCKS.registerBlock(
        "trash_can",
        properties -> new TrashBlock(properties, TrashCan::trashBlockEntity, TrashMenu::new),
        TrashBlock::blockProperties
    );

    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TrashBlockEntity>> TRASH_BLOCK_ENTITY = BLOCK_ENTITIES.register(
        "trash_can_block_entity",
        () -> new BlockEntityType<>((pos, state) -> new TrashBlockEntity(trashBlockEntity(), pos, state), TRASH_BLOCK.get())
    );

    public static final DeferredItem<BlockItem> TRASH_ITEM = ITEMS.registerItem("trash_can", properties -> new BlockItem(TRASH_BLOCK.get(), properties));
    public static final DeferredItem<Item> FILTER_ITEM = ITEMS.registerItem("item_filter", Item::new);
    public static final DeferredItem<Item> LIQUID_FILTER_ITEM = ITEMS.registerItem("liquid_filter", Item::new);

    public static final DeferredHolder<MenuType<?>,MenuType<TrashMenu>> TRASH_MENU = registerMenu("trash_can_menu", (id, inventory, buffer) -> new TrashMenu(id, inventory.player, buffer.readBlockPos()));
    public static final DeferredHolder<MenuType<?>,MenuType<FilterMenu>> FILTER_MENU = registerMenu("filter_config_menu", (id, inventory, buffer) -> new FilterMenu(id, inventory.player, buffer.readBlockPos()));

    public TrashCan(IEventBus eventBus){
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);

        eventBus.addListener(this::init);
        eventBus.addListener(ClientSetup::registerScreens);
        eventBus.addListener(this::addCreativeTabItems);
        eventBus.addListener(CapabilitySetup::register);

        NetworkHandler.register(eventBus);
    }

    public void init(FMLCommonSetupEvent e){
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS){
            event.accept(TRASH_ITEM);
            event.accept(FILTER_ITEM);
            event.accept(LIQUID_FILTER_ITEM);
        }
    }

    private static <T extends BaseMenu> DeferredHolder<MenuType<?>,MenuType<T>> registerMenu(String name, IContainerFactory<T> factory){
        return MENUS.register(name, () -> new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS));
    }

    private static BlockEntityType<TrashBlockEntity> trashBlockEntity(){
        return TRASH_BLOCK_ENTITY.get();
    }
}
