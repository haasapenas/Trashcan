package com.haasapenas.trashcan.packet;

import com.haasapenas.trashcan.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PacketSetFilterMode(BlockPos pos, boolean whitelist) implements CustomPacketPayload {

    public static final Type<PacketSetFilterMode> TYPE = NetworkHandler.type("set_filter_mode");
    public static final StreamCodec<RegistryFriendlyByteBuf,PacketSetFilterMode> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        PacketSetFilterMode::pos,
        ByteBufCodecs.BOOL,
        PacketSetFilterMode::whitelist,
        PacketSetFilterMode::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}