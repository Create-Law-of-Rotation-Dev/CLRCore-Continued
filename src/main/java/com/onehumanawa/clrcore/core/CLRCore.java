package com.onehumanawa.clrcore.core;

import com.onehumanawa.clrcore.contents.ModBlockEntityTypes;
import com.onehumanawa.clrcore.contents.ModBlocks;
import com.onehumanawa.clrcore.contents.ModItems;
import com.onehumanawa.clrcore.contents.ModMenuTypes;
import com.onehumanawa.clrcore.core.config.CLRCoreConfig;
import com.onehumanawa.clrcore.network.*;
import com.onehumanawa.clrcore.contents.event.SuperCoolantHandler;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("removal")
@Mod(CLRCore.MOD_ID)
public class CLRCore {
    public static final String MOD_ID = "clrcore";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public CLRCore() {
        ModBlocks.register();
        ModBlockEntityTypes.register();
        ModItems.register();
        ModMenuTypes.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CLRCoreConfig.SERVER_SPEC);
        PacketRegistry.registerPackets();
        SuperCoolantHandler.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}