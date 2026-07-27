package com.onehumanawa.clrcore.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

public class BrassScrapBucketRenderer extends SmartBlockEntityRenderer<BrassScrapBucketBlockEntity> {

    public BrassScrapBucketRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BrassScrapBucketBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        // 获取过滤器槽位的物品
        ItemStack filterStack = be.getFilterSlot();
        if (filterStack.isEmpty()) return;

        // 获取过滤器槽位的位置偏移
        var slotPos = new BrassScrapBucketFilterSlotPositioning();
        var state = be.getBlockState();
        var level = be.getLevel();
        var pos = be.getBlockPos();

        if (!slotPos.shouldRender(level, pos, state)) return;

        ms.pushPose();
        // 应用位置变换
        slotPos.transform(level, pos, state, ms);
        // 稍微缩放使物品显示在框内
        ms.scale(0.4f, 0.4f, 0.4f);
        // 物品始终面向玩家
        ms.mulPose(Axis.YP.rotationDegrees(180));

        // 渲染物品
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