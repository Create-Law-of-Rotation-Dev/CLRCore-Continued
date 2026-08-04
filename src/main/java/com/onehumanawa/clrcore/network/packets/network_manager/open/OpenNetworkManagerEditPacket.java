package com.onehumanawa.clrcore.network.packets.network_manager.open;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.ModMenuTypes;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkLabel;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerLabelEditMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenNetworkManagerEditPacket {

    private final InteractionHand hand;
    private final List<NetworkLabel> existingLabels;
    private final int editingIndex;
    private final ItemStack editingIcon;
    private final String editingName;

    public OpenNetworkManagerEditPacket(InteractionHand hand, List<NetworkLabel> existingLabels,
                                        int editingIndex, ItemStack editingIcon, String editingName) {
        this.hand = hand;
        this.existingLabels = new ArrayList<>(existingLabels);
        this.editingIndex = editingIndex;
        this.editingIcon = editingIcon.copy();
        this.editingName = editingName;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buf.writeInt(existingLabels.size());
        for (NetworkLabel label : existingLabels) {
            label.serialize(buf);
        }
        buf.writeInt(editingIndex);
        buf.writeItem(editingIcon);
        buf.writeUtf(editingName);
    }

    public static OpenNetworkManagerEditPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int size = buf.readInt();
        List<NetworkLabel> labels = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            labels.add(NetworkLabel.deserialize(buf));
        }
        int editingIndex = buf.readInt();
        ItemStack editingIcon = buf.readItem();
        String editingName = buf.readUtf();
        return new OpenNetworkManagerEditPacket(hand, labels, editingIndex, editingIcon, editingName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.get().getSender();
            if (player == null) return;

            CLRCore.LOGGER.info("[NetworkManager] 服务端收到编辑包，index={}, name={}", editingIndex, editingName);
            NetworkManagerLabelEditMenu.setPendingIcon(editingIcon.copy());

            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.empty();
                }

                @Override
                public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
                    return new NetworkManagerLabelEditMenu(
                            ModMenuTypes.NETWORK_MANAGER_LABEL_EDIT.get(),
                            id,
                            inv,
                            hand,
                            existingLabels,
                            editingIndex,
                            editingIcon,
                            editingName
                    );
                }
            }, buf -> {
                buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
                buf.writeInt(existingLabels.size());
                for (NetworkLabel label : existingLabels) {
                    label.serialize(buf);
                }
                buf.writeInt(editingIndex);
                buf.writeItem(editingIcon);
                buf.writeUtf(editingName);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}