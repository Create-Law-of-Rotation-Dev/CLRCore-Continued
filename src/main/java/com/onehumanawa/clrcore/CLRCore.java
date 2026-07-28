package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.config.CLRCoreConfig;
import com.onehumanawa.clrcore.network.SaveBrassScrapBucketConfigPacket;
import com.onehumanawa.clrcore.network.UpdateBrassScrapBucketAmountPacket;
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

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CLRCoreConfig.SERVER_SPEC);

        registerPackets();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void registerPackets() {
        int id = 0;

        // UpdateBrassScrapBucketAmountPacket - 服务端 -> 客户端
        CHANNEL.messageBuilder(UpdateBrassScrapBucketAmountPacket.class, id++)
                .encoder(UpdateBrassScrapBucketAmountPacket::encode)
                .decoder(UpdateBrassScrapBucketAmountPacket::decode)
                .consumerMainThread(UpdateBrassScrapBucketAmountPacket::handle)
                .add();

        // SaveBrassScrapBucketConfigPacket - 客户端 -> 服务端
        CHANNEL.messageBuilder(SaveBrassScrapBucketConfigPacket.class, id++)
                .encoder(SaveBrassScrapBucketConfigPacket::encode)
                .decoder(SaveBrassScrapBucketConfigPacket::decode)
                .consumerMainThread(SaveBrassScrapBucketConfigPacket::handle)
                .add();
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}