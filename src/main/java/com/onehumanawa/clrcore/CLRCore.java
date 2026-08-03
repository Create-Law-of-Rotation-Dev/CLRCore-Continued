package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.config.CLRCoreConfig;
import com.onehumanawa.clrcore.network.*;
import com.onehumanawa.clrcore.event.SuperCoolantHandler;
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

@SuppressWarnings("removal")
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
        PacketRegistry.registerPackets();

        SuperCoolantHandler.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}