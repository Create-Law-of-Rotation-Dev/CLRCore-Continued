package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.item.KindledFuelRodItem;
import com.onehumanawa.clrcore.item.NetworkManagerItem;
import com.onehumanawa.clrcore.item.SeethingFuelRodItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CLRCore.MOD_ID);

    // ===== 燃料棒 =====
    public static final RegistryObject<Item> KINDLED_FUEL_ROD =
            ITEMS.register("kindled_fuel_rod",
                    () -> new KindledFuelRodItem(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SEETHING_FUEL_ROD =
            ITEMS.register("seething_fuel_rod",
                    () -> new SeethingFuelRodItem(new Item.Properties().stacksTo(64)));

    // ===== 废料桶 =====
    public static final RegistryObject<Item> ANDESITE_SCRAP_BUCKET =
            ITEMS.register("andesite_scrap_bucket",
                    () -> new BlockItem(ModBlocks.ANDESITE_SCRAP_BUCKET.get(),
                            new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> BRASS_SCRAP_BUCKET =
            ITEMS.register("brass_scrap_bucket",
                    () -> new BlockItem(ModBlocks.BRASS_SCRAP_BUCKET.get(),
                            new Item.Properties().stacksTo(16)));

    // ===== 网络管理器 =====
    public static final RegistryObject<Item> NETWORK_MANAGER =
            ITEMS.register("network_manager",
                    () -> new NetworkManagerItem(new Item.Properties().stacksTo(1)));

    // ===== 标码红石信号终端 =====
    public static final RegistryObject<Item> LABELED_REDSTONE_LINK =
            ITEMS.register("labeled_redstone_link",
                    () -> new BlockItem(ModBlocks.LABELED_REDSTONE_LINK.get(),
                            new Item.Properties()));
}