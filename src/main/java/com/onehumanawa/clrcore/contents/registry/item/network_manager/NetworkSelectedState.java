package com.onehumanawa.clrcore.contents.registry.item.network_manager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class NetworkSelectedState {
    private final String labelName;
    private final UUID networkId;

    public NetworkSelectedState(String labelName, UUID networkId) {
        this.labelName = labelName;
        this.networkId = networkId;
    }

    public String getLabelName() {
        return labelName;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("LabelName", labelName);
        tag.putUUID("NetworkId", networkId);
        return tag;
    }

    @Nullable
    public static NetworkSelectedState deserialize(CompoundTag tag) {
        if (tag == null || !tag.contains("LabelName") || !tag.contains("NetworkId")) {
            return null;
        }
        String labelName = tag.getString("LabelName");
        UUID networkId = tag.getUUID("NetworkId");
        return new NetworkSelectedState(labelName, networkId);
    }

    @Nullable
    public static NetworkSelectedState fromItemStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return null;
        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.contains("NetworkSelectedState")) return null;
        if (tag != null) {
            return deserialize(tag.getCompound("NetworkSelectedState"));
        }
        return null;
    }

    public static void setToItemStack(ItemStack stack, NetworkSelectedState state) {
        stack.getOrCreateTag().put("NetworkSelectedState", state.serialize());
    }

    public static void removeFromItemStack(ItemStack stack) {
        if (stack.hasTag()) {
            if (stack.getTag() != null) {
                stack.getTag().remove("NetworkSelectedState");
            }
        }
    }
}