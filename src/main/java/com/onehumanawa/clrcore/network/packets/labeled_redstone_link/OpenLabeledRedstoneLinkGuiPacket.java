package com.onehumanawa.clrcore.network.packets.labeled_redstone_link;

import com.onehumanawa.clrcore.gui.screen.labeled_redstone_link.LabeledRedstoneLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenLabeledRedstoneLinkGuiPacket {

    private final BlockPos pos;
    private final String frequencyText;

    public OpenLabeledRedstoneLinkGuiPacket(BlockPos pos, String frequencyText) {
        this.pos = pos;
        this.frequencyText = frequencyText;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(frequencyText);
    }

    public static OpenLabeledRedstoneLinkGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenLabeledRedstoneLinkGuiPacket(
                buf.readBlockPos(),
                buf.readUtf()
        );
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LabeledRedstoneLinkScreen.open(this);
        });
        ctx.get().setPacketHandled(true);
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getFrequencyText() {
        return frequencyText;
    }
}