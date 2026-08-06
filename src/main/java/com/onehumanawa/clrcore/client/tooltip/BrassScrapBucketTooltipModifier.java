package com.onehumanawa.clrcore.client.tooltip;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.ModBlocks;
import com.onehumanawa.clrcore.core.config.CLRCoreConfig;
import com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket.BrassScrapBucketBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = CLRCore.MOD_ID, value = Dist.CLIENT)
public class BrassScrapBucketTooltipModifier {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        if (stack.getItem() != ModBlocks.BRASS_SCRAP_BUCKET.get().asItem()) {
            return;
        }

        boolean shiftDown = net.minecraft.client.gui.screens.Screen.hasShiftDown();

        // Shift 提示行（始终显示）
        MutableComponent shiftText = Component.translatable("tooltip.clrcore.brass_scrap_bucket.shift.prefix")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("tooltip.clrcore.brass_scrap_bucket.shift.key")
                        .withStyle(shiftDown ? ChatFormatting.WHITE : ChatFormatting.GRAY))
                .append(Component.translatable("tooltip.clrcore.brass_scrap_bucket.shift.suffix")
                        .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(shiftText);

        if (!shiftDown) {
            return;
        }

        // ===== Shift 按下时显示详细信息 =====

        // 获取产出物品名称
        String produceItemName = "经验颗粒";
        Item produceItem = BrassScrapBucketBlockEntity.resolveProduceItem();
        if (produceItem != null) {
            produceItemName = new ItemStack(produceItem).getHoverName().getString();
        }

        tooltip.add(Component.empty());

        // 概要（金色）
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.summary")
                .withStyle(ChatFormatting.GOLD));

        tooltip.add(Component.empty());

        // 行为1：取出产物（灰色）
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.behaviour1.condition")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.behaviour1.action")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.empty());

        // 行为2：过滤器（灰色）
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.behaviour2.condition")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.behaviour2.action")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.empty());

        // 行为3：自动销毁（灰色）
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.behaviour3.condition")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.behaviour3.action", produceItemName)
                .withStyle(ChatFormatting.GRAY));

        // 产出配置信息（深灰色）
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.clrcore.brass_scrap_bucket.produce_info",
                        CLRCoreConfig.SERVER.itemsPerNugget.get(),
                        CLRCoreConfig.SERVER.mbPerNugget.get(),
                        produceItemName)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}