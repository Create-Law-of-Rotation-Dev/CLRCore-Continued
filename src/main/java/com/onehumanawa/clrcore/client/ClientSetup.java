package com.onehumanawa.clrcore.client;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModBlockEntityTypes;
import com.onehumanawa.clrcore.ModMenuTypes;
import com.onehumanawa.clrcore.block.BrassScrapBucketRenderer;
import com.onehumanawa.clrcore.block.labeled_redstone_link.LabeledRedstoneLinkRenderer;
import com.onehumanawa.clrcore.screen.BrassScrapBucketScreen;
import com.onehumanawa.clrcore.screen.LabeledRedstoneLinkScreen;
import com.onehumanawa.clrcore.screen.NetworkManagerLabelEditScreen;
import com.onehumanawa.clrcore.screen.NetworkManagerLabelEditorScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CLRCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // ===== 黄铜废料桶 =====
            MenuScreens.register(ModMenuTypes.BRASS_SCRAP_BUCKET.get(), BrassScrapBucketScreen::new);

            // ===== 网络管理器 =====
            MenuScreens.register(ModMenuTypes.NETWORK_MANAGER_LABEL_EDIT.get(), NetworkManagerLabelEditScreen::new);
            MenuScreens.register(ModMenuTypes.NETWORK_MANAGER_LABEL_EDITOR.get(), NetworkManagerLabelEditorScreen::new);

            CLRCore.LOGGER.info("All screens registered!");
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntityTypes.BRASS_SCRAP_BUCKET.get(),
                BrassScrapBucketRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntityTypes.LABELED_REDSTONE_LINK.get(),
                LabeledRedstoneLinkRenderer::new
        );
        CLRCore.LOGGER.info("BrassScrapBucketRenderer registered!");
    }
}