package com.onehumanawa.clrcore;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CLRCore.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CLRCORE_TAB =
            CREATIVE_TABS.register("clrcore_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.clrcore"))
                            .icon(() -> new ItemStack(ModItems.ANDESITE_SCRAP_BUCKET.get()))
                            .displayItems((parameters, output) -> {
                                // 添加所有物品到标签页
                                output.accept(ModItems.KINDLED_FUEL_ROD.get());
                                output.accept(ModItems.SEETHING_FUEL_ROD.get());
                                output.accept(ModItems.ANDESITE_SCRAP_BUCKET.get());
                            })
                            .build()
            );
}