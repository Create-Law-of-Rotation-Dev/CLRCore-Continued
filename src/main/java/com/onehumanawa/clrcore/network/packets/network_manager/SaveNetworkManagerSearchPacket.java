package com.onehumanawa.clrcore.network.packets.network_manager;

import com.onehumanawa.clrcore.registry.item.network_manager.NetworkManagerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveNetworkManagerSearchPacket {

    private final InteractionHand hand;
    private final String searchText;

    public SaveNetworkManagerSearchPacket(InteractionHand hand, String searchText) {
        this.hand = hand;
        this.searchText = searchText;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buf.writeUtf(searchText);
    }

    public static SaveNetworkManagerSearchPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        String searchText = buf.readUtf();
        return new SaveNetworkManagerSearchPacket(hand, searchText);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(hand);
            NetworkManagerItem.setSearch(stack, searchText);
        });
        ctx.get().setPacketHandled(true);
    }
}