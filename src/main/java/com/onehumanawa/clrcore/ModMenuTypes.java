package com.onehumanawa.clrcore;

import com.onehumanawa.clrcore.screen.BrassScrapBucketMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CLRCore.MOD_ID);

    public static final RegistryObject<MenuType<BrassScrapBucketMenu>> BRASS_SCRAP_BUCKET =
            MENU_TYPES.register("brass_scrap_bucket",
                    () -> IForgeMenuType.create(BrassScrapBucketMenu::new));
}