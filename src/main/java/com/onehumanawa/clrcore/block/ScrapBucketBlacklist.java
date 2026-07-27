package com.onehumanawa.clrcore.block;

import com.onehumanawa.clrcore.config.CLRCoreConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ScrapBucketBlacklist {

    private ScrapBucketBlacklist() {}

    /**
     * 检查物品是否在黑名单中
     * @param stack 要检查的物品
     * @return true表示在黑名单中，不能被销毁
     */
    public static boolean isBlacklisted(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        List<? extends String> blacklist = CLRCoreConfig.SERVER.scrapBucketBlacklistedItems.get();
        if (blacklist.isEmpty()) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (String raw : blacklist) {
            ResourceLocation parsed = ResourceLocation.tryParse(raw);
            if (parsed != null && parsed.equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查流体是否在黑名单中
     * @param stack 要检查的流体
     * @return true表示在黑名单中，不能被销毁
     */
    public static boolean isBlacklisted(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        List<? extends String> blacklist = CLRCoreConfig.SERVER.scrapBucketBlacklistedFluids.get();
        if (blacklist.isEmpty()) {
            return false;
        }

        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        for (String raw : blacklist) {
            ResourceLocation parsed = ResourceLocation.tryParse(raw);
            if (parsed != null && parsed.equals(fluidId)) {
                return true;
            }
        }
        return false;
    }
}