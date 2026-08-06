package com.onehumanawa.clrcore.mixin;

import com.onehumanawa.clrcore.contents.util.PackageUnpackHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Inject(
            method = "clicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void clrcore$onClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (clickType == ClickType.PICKUP && button == 1) {
            if (slotId >= 0 && slotId < menu.slots.size()) {
                ItemStack carried = menu.getCarried();
                if (carried.isEmpty()) {
                    boolean handled = PackageUnpackHelper.tryUnpack(menu, slotId, player);
                    if (handled) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}