package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.item.KindledFuelRodItem;
import com.onehumanawa.clrcore.item.SeethingFuelRodItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CLRCore.MOD_ID);

    // 燃料棒
    public static final RegistryObject<Item> KINDLED_FUEL_ROD =
            ITEMS.register("kindled_fuel_rod",
                    () -> new KindledFuelRodItem(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SEETHING_FUEL_ROD =
            ITEMS.register("seething_fuel_rod",
                    () -> new SeethingFuelRodItem(new Item.Properties().stacksTo(64)));

    // 安山岩废料桶（方块物品）
    public static final RegistryObject<Item> ANDESITE_SCRAP_BUCKET =
            ITEMS.register("andesite_scrap_bucket",
                    () -> new BlockItem(ModBlocks.ANDESITE_SCRAP_BUCKET.get(),
                            new Item.Properties().stacksTo(16))
            );
}