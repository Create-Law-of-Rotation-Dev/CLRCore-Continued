package com.onehumanawa.clrcore.event;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CLRCore.MOD_ID, value = Dist.CLIENT)
public class ModTooltipEventHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        var tooltip = event.getToolTip();

        // 检查是否是安山岩废料桶
        if (stack.getItem() == ModBlocks.ANDESITE_SCRAP_BUCKET.get().asItem()) {
            // 直接检查Shift键是否被按下
            long window = Minecraft.getInstance().getWindow().getWindow();
            boolean shiftDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);

            // 构建Shift提示文字 - 使用翻译键
            MutableComponent shiftText = Component.translatable("tooltip.clrcore.andesite_scrap_bucket.shift.prefix")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("tooltip.clrcore.andesite_scrap_bucket.shift.key")
                            .withStyle(shiftDown ? ChatFormatting.WHITE : ChatFormatting.GRAY))
                    .append(Component.translatable("tooltip.clrcore.andesite_scrap_bucket.shift.suffix")
                            .withStyle(ChatFormatting.DARK_GRAY));

            tooltip.add(shiftText);

            // 如果按住Shift，显示详细信息
            if (shiftDown) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("tooltip.clrcore.andesite_scrap_bucket.destroy_items")
                        .withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("tooltip.clrcore.andesite_scrap_bucket.destroy_fluids")
                        .withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("tooltip.clrcore.andesite_scrap_bucket.config")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }
}