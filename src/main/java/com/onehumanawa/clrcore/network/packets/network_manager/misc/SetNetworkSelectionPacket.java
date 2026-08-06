package com.onehumanawa.clrcore.network.packets.network_manager.misc;

import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkSelectedState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SetNetworkSelectionPacket {

    private final InteractionHand hand;
    private final String labelName;
    private final UUID networkId;

    public SetNetworkSelectionPacket(InteractionHand hand, String labelName, UUID networkId) {
        this.hand = hand;
        this.labelName = labelName;
        this.networkId = networkId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buf.writeUtf(labelName);
        buf.writeUUID(networkId);
    }

    public static SetNetworkSelectionPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        String labelName = buf.readUtf();
        UUID networkId = buf.readUUID();
        return new SetNetworkSelectionPacket(hand, labelName, networkId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(hand);
            NetworkSelectedState.setToItemStack(stack, new NetworkSelectedState(labelName, networkId));
        });
        ctx.get().setPacketHandled(true);
    }
}