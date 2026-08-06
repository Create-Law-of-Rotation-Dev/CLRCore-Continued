package com.onehumanawa.clrcore.contents;

import com.onehumanawa.clrcore.contents.registry.screen.brass_scrap_bucket.BrassScrapBucketScreen;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerLabelEditScreen;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerLabelEditorScreen;
import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.screen.brass_scrap_bucket.BrassScrapBucketMenu;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerLabelEditMenu;
import com.onehumanawa.clrcore.contents.registry.screen.network_manager.NetworkManagerLabelEditorMenu;
import com.simibubi.create.Create;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final MenuEntry<BrassScrapBucketMenu> BRASS_SCRAP_BUCKET =
            register("brass_scrap_bucket", BrassScrapBucketMenu::new, () -> BrassScrapBucketScreen::new);

    public static final MenuEntry<NetworkManagerLabelEditMenu> NETWORK_MANAGER_LABEL_EDIT =
            register("network_manager_label_edit", NetworkManagerLabelEditMenu::new, () -> NetworkManagerLabelEditScreen::new);

    public static final MenuEntry<NetworkManagerLabelEditorMenu> NETWORK_MANAGER_LABEL_EDITOR =
            register("network_manager_label_editor", NetworkManagerLabelEditorMenu::new, () -> NetworkManagerLabelEditorScreen::new);

    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
            String name, MenuBuilder.ForgeMenuFactory<C> factory, NonNullSupplier<MenuBuilder.ScreenFactory<C, S>> screenFactory) {
        return CLRCore.REGISTRATE
                .menu(name, factory, screenFactory)
                .register();
    }

    public static void register() {}
}