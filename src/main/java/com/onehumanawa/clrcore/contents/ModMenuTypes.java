package com.onehumanawa.clrcore.contents;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.screen.brass_scrap_bucket.BrassScrapBucketMenu;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerLabelEditMenu;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerLabelEditorMenu;
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

    public static final RegistryObject<MenuType<NetworkManagerLabelEditMenu>> NETWORK_MANAGER_LABEL_EDIT =
            MENU_TYPES.register("network_manager_label_edit",
                    () -> IForgeMenuType.create(NetworkManagerLabelEditMenu::new));

    public static final RegistryObject<MenuType<NetworkManagerLabelEditorMenu>> NETWORK_MANAGER_LABEL_EDITOR =
            MENU_TYPES.register("network_manager_label_editor",
                    () -> IForgeMenuType.create(NetworkManagerLabelEditorMenu::new));
}