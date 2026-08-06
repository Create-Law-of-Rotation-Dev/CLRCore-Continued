package com.onehumanawa.clrcore.contents.registry.screen.brass_scrap_bucket;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.ModMenuTypes;
import com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket.BrassScrapBucketBlockEntity;
import com.onehumanawa.clrcore.network.packets.brass_scrap_bucket.UpdateBrassScrapBucketAmountPacket;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

public class BrassScrapBucketMenu extends GhostItemMenu<ItemStack> {

    public static final int FILTER_ICON_SLOT_X = 24;
    public static final int FILTER_ICON_SLOT_Y = 24;
    public static final int PLAYER_INV_SLOT_X = 7;
    public static final int PLAYER_INV_SLOT_Y = 101;
    public static final int FILTER_ICON_SLOT_INDEX = 36;

    public final BlockPos pos;
    public final int attachType;
    public final int keepAmount;
    public final boolean keepInStacks;
    public final int maxItems;
    public final int maxStacks;
    public final int currentAmount;
    public final int currentStacks;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    public BrassScrapBucketMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf buf) {
        this(ModMenuTypes.BRASS_SCRAP_BUCKET.get(), id, inv,
                buf.readBlockPos(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readItem()
        );
    }

    public BrassScrapBucketMenu(MenuType<?> type, int id, Inventory inv, BlockPos pos,
                                int attachType, int keepAmount, boolean keepInStacks,
                                int maxItems, int maxStacks, int currentAmount, int currentStacks,
                                ItemStack initialFilterIcon) {
        super(type, id, inv, ItemStack.EMPTY);
        this.pos = pos;
        this.attachType = attachType;
        this.keepAmount = keepAmount;
        this.keepInStacks = keepInStacks;
        this.maxItems = maxItems;
        this.maxStacks = maxStacks;
        this.currentAmount = currentAmount;
        this.currentStacks = currentStacks;
        this.ghostInventory.setStackInSlot(0, initialFilterIcon.copy());
    }

    @Override
    protected ItemStack createOnClient(FriendlyByteBuf buf) {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(1);
    }

    @Override
    protected void initAndReadInventory(ItemStack ignored) {
        this.ghostInventory = this.createGhostInventory();
    }

    @Override
    protected void addSlots() {
        this.addPlayerSlots(PLAYER_INV_SLOT_X, PLAYER_INV_SLOT_Y);

        Level level = player != null ? player.level() : null;
        BlockEntity be = null;
        if (level != null && pos != null) {
            be = level.getBlockEntity(pos);
        }

        if (be instanceof BrassScrapBucketBlockEntity brassBE) {
            this.addSlot(new SlotItemHandler(brassBE.itemHandler, INPUT_SLOT, 77, 38) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return brassBE.itemHandler.isItemValid(INPUT_SLOT, stack);
                }
            });
            this.addSlot(new SlotItemHandler(brassBE.itemHandler, OUTPUT_SLOT, 142, 38) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        } else {
            ItemStackHandler dummyHandler = new ItemStackHandler(1);
            this.addSlot(new SlotItemHandler(dummyHandler, 0, 77, 38) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
                @Override
                public boolean isActive() {
                    return false;
                }
            });
            this.addSlot(new SlotItemHandler(dummyHandler, 0, 142, 38) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
                @Override
                public boolean isActive() {
                    return false;
                }
            });
        }

        this.addSlot(new SlotItemHandler(this.ghostInventory, 0, FILTER_ICON_SLOT_X, FILTER_ICON_SLOT_Y));
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        int GHOST_SLOT_INDEX = 38;
        if (slotId == GHOST_SLOT_INDEX) {
            ItemStack carried = this.getCarried();
            ItemStack current = this.ghostInventory.getStackInSlot(0);
            boolean carriedIsFilter = !carried.isEmpty() && carried.getItem() instanceof FilterItem;
            boolean currentIsFilter = !current.isEmpty() && current.getItem() instanceof FilterItem;

            if (carried.isEmpty()) {
                if (currentIsFilter) {
                    ItemStack toReturn = current.copy();
                    this.ghostInventory.setStackInSlot(0, ItemStack.EMPTY);
                    player.getInventory().placeItemBackInInventory(toReturn);
                } else {
                    this.ghostInventory.setStackInSlot(0, ItemStack.EMPTY);
                }
            } else if (carriedIsFilter) {
                if (currentIsFilter) {
                    ItemStack toReturn = current.copy();
                    ItemStack toPlace = carried.copy();
                    toPlace.setCount(1);
                    this.ghostInventory.setStackInSlot(0, toPlace);
                    carried.shrink(1);
                    this.setCarried(carried);
                    player.getInventory().placeItemBackInInventory(toReturn);
                } else {
                    ItemStack toPlace = carried.copy();
                    toPlace.setCount(1);
                    this.ghostInventory.setStackInSlot(0, toPlace);
                    carried.shrink(1);
                    this.setCarried(carried);
                }
            } else {
                if (currentIsFilter) {
                    ItemStack toReturn = current.copy();
                    player.getInventory().placeItemBackInInventory(toReturn);
                }
                ItemStack copy = carried.copy();
                copy.setCount(1);
                this.ghostInventory.setStackInSlot(0, copy);
            }

            syncFilterToBlockEntity(player);
            return;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    public void submitGhostFilterItem(ItemStack stack, Player player) {
        if (stack.isEmpty()) return;

        ItemStack current = this.ghostInventory.getStackInSlot(0);
        boolean currentIsFilter = !current.isEmpty() && current.getItem() instanceof FilterItem;

        if (currentIsFilter) {
            ItemStack toReturn = current.copy();
            player.getInventory().placeItemBackInInventory(toReturn);
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        this.ghostInventory.setStackInSlot(0, copy);
        syncFilterToBlockEntity(player);
    }

    private void syncFilterToBlockEntity(Player player) {
        if (player == null) return;
        if (player.level().isClientSide()) return;

        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof BrassScrapBucketBlockEntity brassBE)) return;

        ItemStack filter = this.ghostInventory.getStackInSlot(0).copy();
        brassBE.filtering.setFilter(filter);
        brassBE.setChanged();
        brassBE.sendData();

        int newAmount = 0;
        int newStacks = 0;
        if (this.attachType == 1) {
            newAmount = filter.isEmpty() ? brassBE.getAboveCurrentItems() : brassBE.getFilteredCurrentItems();
            newStacks = filter.isEmpty() ? brassBE.getAboveCurrentStacks() : brassBE.getFilteredCurrentStacks();
        } else if (this.attachType == 2) {
            newAmount = filter.isEmpty() ? brassBE.getAboveCurrentFluids() : brassBE.getFilteredCurrentFluids();
        }

        CLRCore.CHANNEL.sendTo(
                new UpdateBrassScrapBucketAmountPacket(pos, newAmount, newStacks),
                ((ServerPlayer) player).connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    @Override
    protected void saveData(ItemStack ignored) {}

    @Override
    public boolean stillValid(Player player) {
        if (pos == null) return false;
        Level level = player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BrassScrapBucketBlockEntity)) return false;
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean allowRepeats() {
        return true;
    }
}