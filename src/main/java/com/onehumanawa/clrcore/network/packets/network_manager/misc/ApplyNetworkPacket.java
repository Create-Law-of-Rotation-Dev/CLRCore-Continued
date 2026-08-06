package com.onehumanawa.clrcore.network.packets.network_manager.misc;

import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkManagerItem;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkSelectedState;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ApplyNetworkPacket {

    private final InteractionHand hand;
    private final BlockPos targetPos;
    private final Vec3 clickLocation;
    private final boolean applyToWholeNetwork;

    public ApplyNetworkPacket(InteractionHand hand, BlockPos targetPos, Vec3 clickLocation, boolean applyToWholeNetwork) {
        this.hand = hand;
        this.targetPos = targetPos;
        this.clickLocation = clickLocation;
        this.applyToWholeNetwork = applyToWholeNetwork;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        buf.writeBlockPos(targetPos);
        buf.writeDouble(clickLocation.x);
        buf.writeDouble(clickLocation.y);
        buf.writeDouble(clickLocation.z);
        buf.writeBoolean(applyToWholeNetwork);
    }

    public static ApplyNetworkPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        BlockPos pos = buf.readBlockPos();
        Vec3 click = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        boolean wholeNetwork = buf.readBoolean();
        return new ApplyNetworkPacket(hand, pos, click, wholeNetwork);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getItemInHand(hand);
            NetworkSelectedState state = NetworkSelectedState.fromItemStack(stack);
            if (state == null) return;

            UUID newNetworkId = state.getNetworkId();
            Level level = player.level();
            BlockEntity be = level.getBlockEntity(targetPos);
            if (be == null) return;

            LogisticallyLinkedBehaviour linkedBehaviour = NetworkManagerItem.getBehaviour(be);

            if (linkedBehaviour != null) {
                if (applyToWholeNetwork) {
                    handleWholeNetwork(level, be, linkedBehaviour, newNetworkId);
                } else {
                    handleSingle(be, linkedBehaviour, newNetworkId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handleSingle(BlockEntity be, LogisticallyLinkedBehaviour linkedBehaviour, UUID newNetworkId) {
        NetworkManagerItem.reassignNetwork(linkedBehaviour, be, newNetworkId);
    }

    private void handleWholeNetwork(Level level, BlockEntity be,
                                    LogisticallyLinkedBehaviour linkedBehaviour, UUID newNetworkId) {
        UUID oldNetworkId = NetworkManagerItem.getFreqId(be);
        if (oldNetworkId == null) {
            handleSingle(be, linkedBehaviour, newNetworkId);
            return;
        }

        for (LogisticallyLinkedBehaviour behaviour : LogisticallyLinkedBehaviour.getAllPresent(oldNetworkId, false, false)) {
            BlockEntity linkedBe = behaviour.blockEntity;
            if (linkedBe != null && !linkedBe.isRemoved()) {
                NetworkManagerItem.reassignNetwork(behaviour, linkedBe, newNetworkId);
            }
        }
    }
}