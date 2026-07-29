package com.onehumanawa.clrcore.network;

import com.onehumanawa.clrcore.block.labeled_redstone_link.LabeledRedstoneLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveLabeledRedstoneLinkConfigPacket {

    private final BlockPos pos;
    private final String frequencyText;

    public SaveLabeledRedstoneLinkConfigPacket(BlockPos pos, String frequencyText) {
        this.pos = pos;
        this.frequencyText = frequencyText;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(frequencyText);
    }

    public static SaveLabeledRedstoneLinkConfigPacket decode(FriendlyByteBuf buf) {
        return new SaveLabeledRedstoneLinkConfigPacket(
                buf.readBlockPos(),
                buf.readUtf()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof LabeledRedstoneLinkBlockEntity lrbe) {
                lrbe.setFrequencyText(frequencyText);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}