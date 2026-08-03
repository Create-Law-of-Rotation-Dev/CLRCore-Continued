package com.onehumanawa.clrcore.network.packets.network_manager;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.registry.item.network_manager.NetworkLabel;
import com.onehumanawa.clrcore.registry.item.network_manager.NetworkManagerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SaveNetworkManagerDataPacket {

    private final InteractionHand hand;
    private final List<NetworkLabel> labels;
    private final boolean reopenMainMenu;

    public SaveNetworkManagerDataPacket(InteractionHand hand, List<NetworkLabel> labels, boolean reopenMainMenu) {
        this.hand = hand;
        this.labels = new ArrayList<>(labels);
        this.reopenMainMenu = reopenMainMenu;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buf.writeInt(labels.size());
        for (NetworkLabel label : labels) {
            label.serialize(buf);
        }
        buf.writeBoolean(reopenMainMenu);
    }

    public static SaveNetworkManagerDataPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int size = buf.readInt();
        List<NetworkLabel> labels = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            labels.add(NetworkLabel.deserialize(buf));
        }
        boolean reopen = buf.readBoolean();
        return new SaveNetworkManagerDataPacket(hand, labels, reopen);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(hand);
            NetworkManagerItem.setLabels(stack, labels);

            if (reopenMainMenu) {
                CLRCore.CHANNEL.sendToServer(new OpenNetworkManagerGuiPacket(hand, labels));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}