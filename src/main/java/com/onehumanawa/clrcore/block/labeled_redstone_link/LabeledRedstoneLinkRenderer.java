package com.onehumanawa.clrcore.block.labeled_redstone_link;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;

public class LabeledRedstoneLinkRenderer extends SmartBlockEntityRenderer<LabeledRedstoneLinkBlockEntity> {

    public LabeledRedstoneLinkRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(LabeledRedstoneLinkBlockEntity be, float partialTicks, PoseStack poseStack,
                              MultiBufferSource buffer, int light, int overlay) {
        // 暂时先不渲染标签，后续可以用配置控制
        // 因为需要配置系统支持
        String freq = be.getFrequencyText();
        if (freq != null && !freq.isEmpty()) {
            // 渲染标签
            this.renderNameplateOnHover(be, Component.literal(freq), 0.25F, poseStack, buffer, light);
        }
    }
}