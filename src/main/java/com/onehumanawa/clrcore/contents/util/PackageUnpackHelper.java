package com.onehumanawa.clrcore.contents.util;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PackageUnpackHelper {

    public static boolean tryUnpack(AbstractContainerMenu menu, int slotId, Player player) {
        if (player.level().isClientSide && !player.isCreative()) {
            return false;
        }

        Slot slot = menu.getSlot(slotId);
        ItemStack stack = slot.getItem();

        if (stack.isEmpty() || !(stack.getItem() instanceof PackageItem)) {
            return false;
        }

        ItemStackHandler contents = PackageItem.getContents(stack);

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack s = contents.getStackInSlot(i);
            if (!s.isEmpty()) {
                items.add(s.copy());
            }
        }

        if (items.isEmpty()) {
            return false;
        }

        Inventory playerInv = player.getInventory();
        boolean slotInHotbar = isSlotInHotbar(slot, playerInv);
        boolean slotInMainInv = isSlotInMainInventory(slot, playerInv);
        boolean slotInContainer = !slotInHotbar && !slotInMainInv;

        List<IItemHandler> fillOrder = buildFillOrder(menu, player, playerInv, slotInHotbar, slotInMainInv, slotInContainer);

        // 清空原槽位
        slot.set(ItemStack.EMPTY);

        // 分发物品
        for (ItemStack item : items) {
            ItemStack remaining = item;
            for (IItemHandler handler : fillOrder) {
                remaining = ItemHandlerHelper.insertItemStacked(handler, remaining, false);
                if (remaining.isEmpty()) {
                    break;
                }
            }
            if (!remaining.isEmpty()) {
                player.drop(remaining, false);
            }
        }

        // 播放音效
        if (player.level().isClientSide) {
            AllSoundEvents.PACKAGE_POP.playAt(player.level(), player.blockPosition(), 1.0F, 1.0F, false);
        } else {
            AllSoundEvents.PACKAGE_POP.playOnServer(player.level(), player.blockPosition());
        }

        return true;
    }

    private static boolean isSlotInHotbar(Slot slot, Inventory playerInv) {
        return slot.container == playerInv && slot.getContainerSlot() < 9;
    }

    private static boolean isSlotInMainInventory(Slot slot, Inventory playerInv) {
        int ci = slot.getContainerSlot();
        return slot.container == playerInv && ci >= 9 && ci <= 35;
    }

    private static List<IItemHandler> buildFillOrder(AbstractContainerMenu menu, Player player, Inventory playerInv,
                                                     boolean slotInHotbar, boolean slotInMainInv, boolean slotInContainer) {
        List<IItemHandler> order = new ArrayList<>();
        IItemHandler hotbar = new HotbarHandler(playerInv);
        IItemHandler mainInv = new MainInventoryHandler(playerInv);
        boolean isInventoryMenu = menu instanceof InventoryMenu;

        if (isInventoryMenu) {
            if (slotInHotbar) {
                order.add(hotbar);
                order.add(mainInv);
            } else {
                order.add(mainInv);
                order.add(hotbar);
            }
        } else {
            IItemHandler container = new ContainerSlotHandler(menu, playerInv);
            if (slotInContainer) {
                order.add(container);
                order.add(mainInv);
                order.add(hotbar);
            } else if (slotInMainInv) {
                order.add(mainInv);
                order.add(hotbar);
                order.add(container);
            } else {
                order.add(hotbar);
                order.add(mainInv);
                order.add(container);
            }
        }

        return order;
    }

    // ===== Inner Classes =====

    private static class HotbarHandler implements IItemHandler {
        private final Inventory inv;

        HotbarHandler(Inventory inv) {
            this.inv = inv;
        }

        @Override
        public int getSlots() {
            return 9;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return (slot >= 0 && slot < 9) ? this.inv.getItem(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= 9 || stack.isEmpty()) {
                return stack;
            }

            ItemStack existing = this.inv.getItem(slot);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, stack)) {
                return stack;
            }

            int limit = Math.min(this.inv.getMaxStackSize(), stack.getMaxStackSize());
            int canInsert = limit - existing.getCount();
            if (canInsert <= 0) {
                return stack;
            }

            int toInsert = Math.min(stack.getCount(), canInsert);
            if (!simulate) {
                if (existing.isEmpty()) {
                    this.inv.setItem(slot, stack.copyWithCount(toInsert));
                } else {
                    existing.grow(toInsert);
                }
                this.inv.setChanged();
            }

            return toInsert >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - toInsert);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    }

    private static class MainInventoryHandler implements IItemHandler {
        private final Inventory inv;

        MainInventoryHandler(Inventory inv) {
            this.inv = inv;
        }

        @Override
        public int getSlots() {
            return 27;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return (slot >= 0 && slot < 27) ? this.inv.getItem(slot + 9) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= 27 || stack.isEmpty()) {
                return stack;
            }

            int realSlot = slot + 9;
            ItemStack existing = this.inv.getItem(realSlot);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, stack)) {
                return stack;
            }

            int limit = Math.min(this.inv.getMaxStackSize(), stack.getMaxStackSize());
            int canInsert = limit - existing.getCount();
            if (canInsert <= 0) {
                return stack;
            }

            int toInsert = Math.min(stack.getCount(), canInsert);
            if (!simulate) {
                if (existing.isEmpty()) {
                    this.inv.setItem(realSlot, stack.copyWithCount(toInsert));
                } else {
                    existing.grow(toInsert);
                }
                this.inv.setChanged();
            }

            return toInsert >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - toInsert);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    }

    private static class ContainerSlotHandler implements IItemHandler {
        private final List<Slot> containerSlots = new ArrayList<>();

        ContainerSlotHandler(AbstractContainerMenu menu, Inventory playerInv) {
            for (Slot s : menu.slots) {
                if (s.container != playerInv) {
                    this.containerSlots.add(s);
                }
            }
        }

        @Override
        public int getSlots() {
            return this.containerSlots.size();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return (slot >= 0 && slot < this.containerSlots.size()) ? this.containerSlots.get(slot).getItem() : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= this.containerSlots.size() || stack.isEmpty()) {
                return stack;
            }

            Slot s = this.containerSlots.get(slot);
            if (!s.mayPlace(stack)) {
                return stack;
            }

            ItemStack existing = s.getItem();
            if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, stack)) {
                return stack;
            }

            int limit = s.getMaxStackSize(stack);
            int canInsert = limit - existing.getCount();
            if (canInsert <= 0) {
                return stack;
            }

            int toInsert = Math.min(stack.getCount(), canInsert);
            if (!simulate) {
                if (existing.isEmpty()) {
                    s.set(stack.copyWithCount(toInsert));
                } else {
                    existing.grow(toInsert);
                    s.set(existing);
                }
                s.setChanged();
            }

            return toInsert >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - toInsert);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return (slot >= 0 && slot < this.containerSlots.size()) ? this.containerSlots.get(slot).getMaxStackSize() : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return (slot >= 0 && slot < this.containerSlots.size()) && this.containerSlots.get(slot).mayPlace(stack);
        }
    }
}