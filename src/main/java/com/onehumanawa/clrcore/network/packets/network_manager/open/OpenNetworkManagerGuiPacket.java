package com.onehumanawa.clrcore.network.packets.network_manager.open;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkLabel;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenNetworkManagerGuiPacket {

    private final InteractionHand hand;
    private final List<NetworkLabel> labels;

    public OpenNetworkManagerGuiPacket(InteractionHand hand, List<NetworkLabel> labels) {
        this.hand = hand;
        this.labels = new ArrayList<>(labels);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buf.writeInt(labels.size());
        for (NetworkLabel label : labels) {
            label.serialize(buf);
        }
    }

    public static OpenNetworkManagerGuiPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int size = buf.readInt();
        List<NetworkLabel> labels = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            labels.add(NetworkLabel.deserialize(buf));
        }
        return new OpenNetworkManagerGuiPacket(hand, labels);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CLRCore.LOGGER.info("[NetworkManager] 打开主界面，标签数量: {}", labels.size());
            NetworkManagerScreen.open(hand, labels);
        });
        ctx.get().setPacketHandled(true);
    }
}