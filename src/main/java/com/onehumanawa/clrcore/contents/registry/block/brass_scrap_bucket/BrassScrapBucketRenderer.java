package com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BrassScrapBucketRenderer extends SmartBlockEntityRenderer<BrassScrapBucketBlockEntity> {

    public BrassScrapBucketRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BrassScrapBucketBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        ItemStack filterStack = be.getFilterSlot();
        if (filterStack.isEmpty()) return;

        var slotPos = new BrassScrapBucketFilterSlotPositioning();
        var state = be.getBlockState();
        var level = be.getLevel();
        var pos = be.getBlockPos();

        if (!slotPos.shouldRender(level, pos, state)) return;

        ms.pushPose();
        slotPos.transform(level, pos, state, ms);
        ms.scale(0.4f, 0.4f, 0.4f);
        ms.mulPose(Axis.YP.rotationDegrees(180));

        Minecraft.getInstance().getItemRenderer().renderStatic(
                filterStack,
                ItemDisplayContext.FIXED,
                light,
                overlay,
                ms,
                buffer,
                be.getLevel(),
                0
        );

        ms.popPose();
    }
}