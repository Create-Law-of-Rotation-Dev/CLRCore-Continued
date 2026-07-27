package com.onehumanawa.clrcore.client;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModBlockEntityTypes;
import com.onehumanawa.clrcore.block.BrassScrapBucketRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CLRCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntityTypes.BRASS_SCRAP_BUCKET.get(),
                BrassScrapBucketRenderer::new
        );
    }
}