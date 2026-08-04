package com.onehumanawa.clrcore.contents.registry.screen.network_manager;

import com.onehumanawa.clrcore.contents.ModMenuTypes;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkLabel;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

public class NetworkManagerLabelEditMenu extends GhostItemMenu<ItemStack> {

    public static final int ICON_SLOT_INDEX = 36;

    public final InteractionHand hand;
    public final List<NetworkLabel> existingLabels;
    public final int editingIndex;
    public final ItemStack editingIcon;
    public final String editingName;

    private static ItemStack pendingIcon = null;

    public NetworkManagerLabelEditMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(ModMenuTypes.NETWORK_MANAGER_LABEL_EDIT.get(), id, inv,
                buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                readLabels(buf),
                buf.readInt(),
                buf.readItem(),
                buf.readUtf()
        );
    }

    public NetworkManagerLabelEditMenu(MenuType<?> type, int id, Inventory inv, InteractionHand hand,
                                       List<NetworkLabel> existingLabels, int editingIndex,
                                       ItemStack editingIcon, String editingName) {
        super(type, id, inv, ItemStack.EMPTY);
        this.hand = hand;
        this.existingLabels = existingLabels;
        this.editingIndex = editingIndex;
        this.editingIcon = editingIcon;
        this.editingName = editingName;
    }

    private static List<NetworkLabel> readLabels(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<NetworkLabel> labels = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            labels.add(NetworkLabel.deserialize(buf));
        }
        return labels;
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
        ItemStack icon = pendingIcon;
        pendingIcon = null;
        if (icon != null && !icon.isEmpty()) {
            this.ghostInventory.setStackInSlot(0, icon);
        } else {
            @SuppressWarnings("removal")
            Item stockLink = BuiltInRegistries.ITEM.get(new ResourceLocation("create", "stock_link"));
            this.ghostInventory.setStackInSlot(0, new ItemStack(stockLink));
        }
    }

    @Override
    protected void addSlots() {
        this.addPlayerSlots(18, 116);
        this.addSlot(new SlotItemHandler(this.ghostInventory, 0, 16, 29));
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId != ICON_SLOT_INDEX || !this.getCarried().isEmpty()) {
            super.clicked(slotId, dragType, clickType, player);
        }
    }

    public static void setPendingIcon(ItemStack icon) {
        pendingIcon = icon;
    }

    @Override
    protected void saveData(ItemStack ignored) {}

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean allowRepeats() {
        return true;
    }
}