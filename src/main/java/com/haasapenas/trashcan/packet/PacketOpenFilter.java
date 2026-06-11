package com.haasapenas.trashcan.packet;

import com.haasapenas.trashcan.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PacketOpenFilter(BlockPos pos) implements CustomPacketPayload {

    public static final Type<PacketOpenFilter> TYPE = NetworkHandler.type("open_filter_config");
    public static final StreamCodec<RegistryFriendlyByteBuf,PacketOpenFilter> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        PacketOpenFilter::pos,
        PacketOpenFilter::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}