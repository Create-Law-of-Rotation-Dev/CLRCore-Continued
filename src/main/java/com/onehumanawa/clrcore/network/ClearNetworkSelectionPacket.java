package com.onehumanawa.clrcore.network;

import com.onehumanawa.clrcore.item.NetworkSelectedState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearNetworkSelectionPacket {

    private final InteractionHand hand;

    public ClearNetworkSelectionPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    public static ClearNetworkSelectionPacket decode(FriendlyByteBuf buf) {
        return new ClearNetworkSelectionPacket(
                buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(hand);
            NetworkSelectedState.removeFromItemStack(stack);
        });
        ctx.get().setPacketHandled(true);
    }
}