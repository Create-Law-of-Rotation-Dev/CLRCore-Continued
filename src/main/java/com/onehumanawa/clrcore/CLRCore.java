package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.config.CLRCoreConfig;
import com.onehumanawa.clrcore.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CLRCore.MOD_ID)
public class CLRCore {
    public static final String MOD_ID = "clrcore";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public CLRCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // ===== 注册所有内容 =====
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        // ===== 注册配置 =====
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CLRCoreConfig.SERVER_SPEC);

        // ===== 注册网络包 =====
        registerPackets();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void registerPackets() {
        int id = 0;

        // ===== 黄铜废料桶 =====
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

        // ===== 网络管理器 =====
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

        LOGGER.info("All network packets registered!");
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}