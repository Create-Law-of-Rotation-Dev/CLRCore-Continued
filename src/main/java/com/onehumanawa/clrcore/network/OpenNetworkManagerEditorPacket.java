package com.onehumanawa.clrcore.network;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModMenuTypes;
import com.onehumanawa.clrcore.item.NetworkLabel;
import com.onehumanawa.clrcore.screen.NetworkManagerLabelEditorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class OpenNetworkManagerEditorPacket {

    private final InteractionHand hand;
    private final List<NetworkLabel> existingLabels;
    private final Optional<UUID> targetNetworkId;

    public OpenNetworkManagerEditorPacket(InteractionHand hand, List<NetworkLabel> existingLabels, Optional<UUID> targetNetworkId) {
        this.hand = hand;
        this.existingLabels = new ArrayList<>(existingLabels);
        this.targetNetworkId = targetNetworkId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buf.writeInt(existingLabels.size());
        for (NetworkLabel label : existingLabels) {
            label.serialize(buf);
        }
        buf.writeBoolean(targetNetworkId.isPresent());
        targetNetworkId.ifPresent(buf::writeUUID);
    }

    public static OpenNetworkManagerEditorPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int size = buf.readInt();
        List<NetworkLabel> labels = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            labels.add(NetworkLabel.deserialize(buf));
        }
        boolean hasId = buf.readBoolean();
        Optional<UUID> targetId = hasId ? Optional.of(buf.readUUID()) : Optional.empty();
        return new OpenNetworkManagerEditorPacket(hand, labels, targetId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getSender();
            if (player == null) return;

            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new NetworkManagerLabelEditorMenu(
                            ModMenuTypes.NETWORK_MANAGER_LABEL_EDITOR.get(),
                            id,
                            inv,
                            hand,
                            existingLabels,
                            targetNetworkId
                    );
                }
            }, buf -> {
                buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
                buf.writeInt(existingLabels.size());
                for (NetworkLabel label : existingLabels) {
                    label.serialize(buf);
                }
                buf.writeBoolean(targetNetworkId.isPresent());
                targetNetworkId.ifPresent(buf::writeUUID);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}