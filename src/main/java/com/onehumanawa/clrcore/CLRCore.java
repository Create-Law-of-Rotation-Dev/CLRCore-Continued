package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.config.CLRCoreConfig;
import com.onehumanawa.clrcore.datagen.ModBlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CLRCore.MOD_ID)
public class CLRCore {
    public static final String MOD_ID = "clrcore";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CLRCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CLRCoreConfig.SERVER_SPEC);

        // 注册数据生成器
        modEventBus.addListener(this::gatherData);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(
                event.includeServer(),
                new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper)
        );
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}