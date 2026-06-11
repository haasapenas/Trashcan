package com.haasapenas.trashcan;

import com.haasapenas.trashcan.filter.FilterData;
import com.haasapenas.trashcan.packet.PacketUpdateFilterEntry;
import com.haasapenas.trashcan.packet.PacketOpenFilter;
import com.haasapenas.trashcan.packet.PacketSetFilterMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

public class NetworkHandler {

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path){
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TrashCan.MOD_ID, path));
    }

    public static void register(IEventBus eventBus){
        eventBus.addListener(NetworkHandler::registerPayloads);
    }

    public static void sendToServer(CustomPacketPayload payload){
        ClientPacketDistributor.sendToServer(payload);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(PacketOpenFilter.TYPE, PacketOpenFilter.STREAM_CODEC, NetworkHandler::handle);
        registrar.playToServer(PacketSetFilterMode.TYPE, PacketSetFilterMode.STREAM_CODEC, NetworkHandler::handle);
        registrar.playToServer(PacketUpdateFilterEntry.TYPE, PacketUpdateFilterEntry.STREAM_CODEC, NetworkHandler::handle);
    }

    private static void handle(PacketOpenFilter packet, IPayloadContext context){
        if(!(context.player() instanceof ServerPlayer player))
            return;

        withTrashCan(context, packet.pos(), entity -> {
            MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, p) -> new com.haasapenas.trashcan.screen.FilterMenu(containerId, player, packet.pos()),
                Component.translatable("trashcan.gui.trash_filter.title")
            );
            player.openMenu(provider, buffer -> buffer.writeBlockPos(packet.pos()));
        });
    }

    private static void handle(PacketSetFilterMode packet, IPayloadContext context){
        withTrashCan(context, packet.pos(), entity -> {
            if(FilterData.isFilter(entity.itemFilterStack)){
                FilterData.setWhitelist(entity.itemFilterStack, packet.whitelist());
                entity.dataChanged();
            }
        });
    }

    private static void handle(PacketUpdateFilterEntry packet, IPayloadContext context){
        if(packet.filterSlot() < 0 || packet.filterSlot() >= FilterData.SLOT_COUNT)
            return;
        if(packet.itemId().length() > PacketUpdateFilterEntry.MAX_ITEM_ID_LENGTH || packet.nbtData().length() > PacketUpdateFilterEntry.MAX_NBT_DATA_LENGTH)
            return;

        String entryId = packet.itemId().trim();
        String nbtData = packet.nbtData().trim();

        final String acceptedEntryId = entryId;
        final String acceptedNbtData = nbtData;
        withTrashCan(context, packet.pos(), entity -> {
            if(FilterData.isItemFilter(entity.itemFilterStack)){
                String itemId = normalizeItemId(acceptedEntryId);
                if(itemId != null){
                    FilterData.setFilter(entity.itemFilterStack, packet.filterSlot(), itemId, acceptedNbtData);
                    entity.dataChanged();
                }
            }else if(FilterData.isLiquidFilter(entity.itemFilterStack)){
                String fluidId = normalizeFluidId(acceptedEntryId);
                if(fluidId != null){
                    FilterData.setFluidFilter(entity.itemFilterStack, packet.filterSlot(), fluidId);
                    entity.dataChanged();
                }
            }
        });
    }

    private static String normalizeItemId(String itemId){
        if(itemId.isEmpty())
            return "";

        Identifier id = Identifier.tryParse(itemId);
        return id != null && BuiltInRegistries.ITEM.containsKey(id) ? id.toString() : null;
    }

    private static String normalizeFluidId(String fluidId){
        if(fluidId.isEmpty())
            return "";

        Identifier id = Identifier.tryParse(fluidId);
        return id != null && BuiltInRegistries.FLUID.containsKey(id) ? id.toString() : null;
    }

    private static void withTrashCan(IPayloadContext context, BlockPos pos, Consumer<TrashBlockEntity> action){
        if(!(context.player() instanceof ServerPlayer player))
            return;

        Level level = player.level();
        if(player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64)
            return;

        if(level.getBlockEntity(pos) instanceof TrashBlockEntity entity)
            action.accept(entity);
    }
}
