package com.onehumanawa.clrcore.mixin;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.util.PackageUnpackHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class MixinAbstractContainerMenu {

    @Inject(
            method = "clicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void clrcore$onClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        // 检查是否是右键点击 (button == 1) 且是普通点击 (ClickType.PICKUP)
        if (clickType == ClickType.PICKUP && button == 1) {
            if (slotId >= 0 && slotId < menu.slots.size()) {
                // 检查手里是否没有拿着物品（只有空手时才触发拆包）
                ItemStack carried = menu.getCarried();
                if (carried.isEmpty()) {
                    // 尝试拆包
                    boolean handled = PackageUnpackHelper.tryUnpack(menu, slotId, player);
                    if (handled) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}