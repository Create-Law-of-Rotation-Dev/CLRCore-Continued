package com.onehumanawa.clrcore.network;

import com.onehumanawa.clrcore.network.packets.brass_scrap_bucket.SaveBrassScrapBucketConfigPacket;
import com.onehumanawa.clrcore.network.packets.brass_scrap_bucket.UpdateBrassScrapBucketAmountPacket;
import com.onehumanawa.clrcore.network.packets.labeled_redstone_link.OpenLabeledRedstoneLinkGuiPacket;
import com.onehumanawa.clrcore.network.packets.labeled_redstone_link.SaveLabeledRedstoneLinkConfigPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.misc.ApplyNetworkPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.misc.ClearNetworkSelectionPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.misc.SetNetworkSelectionPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.open.OpenNetworkManagerEditPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.open.OpenNetworkManagerEditorPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.open.OpenNetworkManagerGuiPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.save.SaveNetworkManagerDataPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.save.SaveNetworkManagerSearchPacket;

import static com.mojang.text2speech.Narrator.LOGGER;
import static com.onehumanawa.clrcore.core.CLRCore.CHANNEL;

public class PacketRegistry {
    public PacketRegistry () {}
    public static void registerPackets() {
        int id = 0;

        CHANNEL.messageBuilder(UpdateBrassScrapBucketAmountPacket.class, id++)
                .encoder(UpdateBrassScrapBucketAmountPacket::encode)
                .decoder(UpdateBrassScrapBucketAmountPacket::decode)
                .consumerMainThread(UpdateBrassScrapBucketAmountPacket::handle)
                .add();

        CHANNEL.messageBuilder(SaveBrassScrapBucketConfigPacket.class, id++)
                .encoder(SaveBrassScrapBucketConfigPacket::encode)
                .decoder(SaveBrassScrapBucketConfigPacket::decode)
                .consumerMainThread(SaveBrassScrapBucketConfigPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenNetworkManagerGuiPacket.class, id++)
                .encoder(OpenNetworkManagerGuiPacket::encode)
                .decoder(OpenNetworkManagerGuiPacket::decode)
                .consumerMainThread(OpenNetworkManagerGuiPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenNetworkManagerEditorPacket.class, id++)
                .encoder(OpenNetworkManagerEditorPacket::encode)
                .decoder(OpenNetworkManagerEditorPacket::decode)
                .consumerMainThread(OpenNetworkManagerEditorPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenNetworkManagerEditPacket.class, id++)
                .encoder(OpenNetworkManagerEditPacket::encode)
                .decoder(OpenNetworkManagerEditPacket::decode)
                .consumerMainThread(OpenNetworkManagerEditPacket::handle)
                .add();

        CHANNEL.messageBuilder(ApplyNetworkPacket.class, id++)
                .encoder(ApplyNetworkPacket::encode)
                .decoder(ApplyNetworkPacket::decode)
                .consumerMainThread(ApplyNetworkPacket::handle)
                .add();

        CHANNEL.messageBuilder(ClearNetworkSelectionPacket.class, id++)
                .encoder(ClearNetworkSelectionPacket::encode)
                .decoder(ClearNetworkSelectionPacket::decode)
                .consumerMainThread(ClearNetworkSelectionPacket::handle)
                .add();

        CHANNEL.messageBuilder(SaveNetworkManagerDataPacket.class, id++)
                .encoder(SaveNetworkManagerDataPacket::encode)
                .decoder(SaveNetworkManagerDataPacket::decode)
                .consumerMainThread(SaveNetworkManagerDataPacket::handle)
                .add();

        CHANNEL.messageBuilder(SaveNetworkManagerSearchPacket.class, id++)
                .encoder(SaveNetworkManagerSearchPacket::encode)
                .decoder(SaveNetworkManagerSearchPacket::decode)
                .consumerMainThread(SaveNetworkManagerSearchPacket::handle)
                .add();

        CHANNEL.messageBuilder(SetNetworkSelectionPacket.class, id++)
                .encoder(SetNetworkSelectionPacket::encode)
                .decoder(SetNetworkSelectionPacket::decode)
                .consumerMainThread(SetNetworkSelectionPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenLabeledRedstoneLinkGuiPacket.class, id++)
                .encoder(OpenLabeledRedstoneLinkGuiPacket::encode)
                .decoder(OpenLabeledRedstoneLinkGuiPacket::decode)
                .consumerMainThread(OpenLabeledRedstoneLinkGuiPacket::handle)
                .add();

        CHANNEL.messageBuilder(SaveLabeledRedstoneLinkConfigPacket.class, id++)
                .encoder(SaveLabeledRedstoneLinkConfigPacket::encode)
                .decoder(SaveLabeledRedstoneLinkConfigPacket::decode)
                .consumerMainThread(SaveLabeledRedstoneLinkConfigPacket::handle)
                .add();

        LOGGER.info("All network packets registered!");
    }
}