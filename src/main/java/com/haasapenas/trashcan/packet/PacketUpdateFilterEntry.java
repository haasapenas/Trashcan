package com.haasapenas.trashcan.packet;

import com.haasapenas.trashcan.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PacketUpdateFilterEntry(BlockPos pos, int filterSlot, String itemId, String nbtData) implements CustomPacketPayload {

    public static final int MAX_ITEM_ID_LENGTH = 128;
    public static final int MAX_NBT_DATA_LENGTH = 2048;

    public static final Type<PacketUpdateFilterEntry> TYPE = NetworkHandler.type("update_filter_slot");
    public static final StreamCodec<RegistryFriendlyByteBuf,PacketUpdateFilterEntry> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        PacketUpdateFilterEntry::pos,
        ByteBufCodecs.VAR_INT,
        PacketUpdateFilterEntry::filterSlot,
        ByteBufCodecs.stringUtf8(MAX_ITEM_ID_LENGTH),
        PacketUpdateFilterEntry::itemId,
        ByteBufCodecs.stringUtf8(MAX_NBT_DATA_LENGTH),
        PacketUpdateFilterEntry::nbtData,
        PacketUpdateFilterEntry::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}