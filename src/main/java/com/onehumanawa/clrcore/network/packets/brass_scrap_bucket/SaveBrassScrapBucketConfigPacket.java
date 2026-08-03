package com.onehumanawa.clrcore.network.packets.brass_scrap_bucket;

import com.onehumanawa.clrcore.block.brass_scrap_bucket.BrassScrapBucketBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveBrassScrapBucketConfigPacket {
    private final BlockPos pos;
    private final int keepAmount;
    private final boolean keepInStacks;

    public SaveBrassScrapBucketConfigPacket(BlockPos pos, int keepAmount, boolean keepInStacks) {
        this.pos = pos;
        this.keepAmount = keepAmount;
        this.keepInStacks = keepInStacks;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(keepAmount);
        buf.writeBoolean(keepInStacks);
    }

    public static SaveBrassScrapBucketConfigPacket decode(FriendlyByteBuf buf) {
        return new SaveBrassScrapBucketConfigPacket(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readBoolean()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockEntity be = player.level().getBlockEntity(pos);
                if (be instanceof BrassScrapBucketBlockEntity brassBE) {
                    brassBE.keepAmount = keepAmount;
                    brassBE.keepInStacks = keepInStacks;
                    brassBE.setChanged();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}