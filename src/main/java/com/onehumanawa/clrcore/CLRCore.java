package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.config.CLRCoreConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CLRCore.MOD_ID)
public class CLRCore {
    public static final String MOD_ID = "clrcore";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CLRCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册所有内容
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);  // 新增

        // 注册配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CLRCoreConfig.SERVER_SPEC);

        // 监听配置重载事件
        modEventBus.addListener(this::onConfigReload);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == CLRCoreConfig.SERVER_SPEC) {
            LOGGER.info("CLRCore config reloaded!");
        }
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}