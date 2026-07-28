package com.onehumanawa.clrcore.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
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
        // 使用 putUUID 方法存储 UUID（会自动转为 INT[]）
        tag.putUUID("NetworkId", networkId);
        return tag;
    }

    @Nullable
    public static NetworkSelectedState deserialize(CompoundTag tag) {
        if (tag == null || !tag.contains("LabelName") || !tag.contains("NetworkId")) {
            return null;
        }
        String labelName = tag.getString("LabelName");
        // 使用 getUUID 读取
        UUID networkId = tag.getUUID("NetworkId");
        return new NetworkSelectedState(labelName, networkId);
    }

    @Nullable
    public static NetworkSelectedState fromItemStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return null;
        CompoundTag tag = stack.getTag();
        if (!tag.contains("NetworkSelectedState")) return null;
        return deserialize(tag.getCompound("NetworkSelectedState"));
    }

    public static void setToItemStack(ItemStack stack, NetworkSelectedState state) {
        stack.getOrCreateTag().put("NetworkSelectedState", state.serialize());
    }

    public static void removeFromItemStack(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove("NetworkSelectedState");
        }
    }
}