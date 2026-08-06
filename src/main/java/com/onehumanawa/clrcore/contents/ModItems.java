package com.onehumanawa.clrcore.contents;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkManagerItem;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final ItemEntry<Item> KINDLED_FUEL_ROD = CLRCore.REGISTRATE
            .item("kindled_fuel_rod", Item::new)
            .register();

    public static final ItemEntry<Item> SEETHING_FUEL_ROD = CLRCore.REGISTRATE
            .item("seething_fuel_rod", Item::new)
            .register();

    public static final ItemEntry<Item> NETWORK_MANAGER = CLRCore.REGISTRATE
            .item("network_manager", Item::new)
            .properties(p -> p.stacksTo(1).rarity(Rarity.UNCOMMON))
            .register();

    public static void register() {}
}