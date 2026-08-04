package com.onehumanawa.clrcore.contents;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkManagerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CLRCore.MOD_ID);

    public static final RegistryObject<Item> KINDLED_FUEL_ROD =
            ITEMS.register("kindled_fuel_rod",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SEETHING_FUEL_ROD =
            ITEMS.register("seething_fuel_rod",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> ANDESITE_SCRAP_BUCKET =
            ITEMS.register("andesite_scrap_bucket",
                    () -> new BlockItem(ModBlocks.ANDESITE_SCRAP_BUCKET.get(),
                            new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> BRASS_SCRAP_BUCKET =
            ITEMS.register("brass_scrap_bucket",
                    () -> new BlockItem(ModBlocks.BRASS_SCRAP_BUCKET.get(),
                            new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> NETWORK_MANAGER =
            ITEMS.register("network_manager",
                    () -> new NetworkManagerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LABELED_REDSTONE_LINK =
            ITEMS.register("labeled_redstone_link",
                    () -> new BlockItem(ModBlocks.LABELED_REDSTONE_LINK.get(),
                            new Item.Properties()));
}