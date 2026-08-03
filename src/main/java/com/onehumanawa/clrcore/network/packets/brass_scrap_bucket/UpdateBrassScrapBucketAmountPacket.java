package com.onehumanawa.clrcore.network.packets.brass_scrap_bucket;

import com.onehumanawa.clrcore.gui.screen.brass_scrap_bucket.BrassScrapBucketScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateBrassScrapBucketAmountPacket {
    private final BlockPos pos;
    private final int currentAmount;
    private final int currentStacks;

    public UpdateBrassScrapBucketAmountPacket(BlockPos pos, int currentAmount, int currentStacks) {
        this.pos = pos;
        this.currentAmount = currentAmount;
        this.currentStacks = currentStacks;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(currentAmount);
        buf.writeInt(currentStacks);
    }

    public static UpdateBrassScrapBucketAmountPacket decode(FriendlyByteBuf buf) {
        return new UpdateBrassScrapBucketAmountPacket(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof BrassScrapBucketScreen screen) {
                screen.updateCurrentAmounts(currentAmount, currentStacks);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}