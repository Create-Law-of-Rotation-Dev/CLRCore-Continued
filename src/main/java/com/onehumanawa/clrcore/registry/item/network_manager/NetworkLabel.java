package com.onehumanawa.clrcore.registry.item.network_manager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkLabel {
    private String name;
    private ItemStack icon;
    private UUID networkId; // 可为 null

    public NetworkLabel(String name, ItemStack icon, @Nullable UUID networkId) {
        this.name = name;
        this.icon = icon.copy();
        this.icon.setCount(1);
        this.networkId = networkId;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon.copy();
    }

    @Nullable
    public UUID getNetworkId() {
        return networkId;
    }

    public boolean hasNetworkId() {
        return networkId != null;
    }

    // ========== NBT 序列化（用于物品存储） ==========
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name);
        tag.put("Icon", icon.save(new CompoundTag()));
        if (networkId != null) {
            tag.putUUID("NetworkId", networkId);
        }
        return tag;
    }

    public static NetworkLabel deserializeNBT(CompoundTag tag) {
        String name = tag.getString("Name");
        ItemStack icon = ItemStack.of(tag.getCompound("Icon"));
        UUID networkId = null;
        if (tag.contains("NetworkId")) {
            networkId = tag.getUUID("NetworkId");
        }
        return new NetworkLabel(name, icon, networkId);
    }

    // ========== 网络序列化（用于数据包传输） ==========
    public void serialize(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeItem(icon);
        buf.writeBoolean(networkId != null);
        if (networkId != null) {
            buf.writeUUID(networkId);
        }
    }

    public static NetworkLabel deserialize(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        ItemStack icon = buf.readItem();
        UUID networkId = null;
        if (buf.readBoolean()) {
            networkId = buf.readUUID();
        }
        return new NetworkLabel(name, icon, networkId);
    }

    // ========== List 辅助方法（NBT） ==========
    public static List<NetworkLabel> listFromNBT(CompoundTag tag, String key) {
        List<NetworkLabel> list = new ArrayList<>();
        if (tag.contains(key)) {
            CompoundTag listTag = tag.getCompound(key);
            int size = listTag.getInt("Size");
            for (int i = 0; i < size; i++) {
                list.add(deserializeNBT(listTag.getCompound("Entry" + i)));
            }
        }
        return list;
    }

    public static void listToNBT(CompoundTag tag, String key, List<NetworkLabel> labels) {
        CompoundTag listTag = new CompoundTag();
        listTag.putInt("Size", labels.size());
        for (int i = 0; i < labels.size(); i++) {
            listTag.put("Entry" + i, labels.get(i).serializeNBT());
        }
        tag.put(key, listTag);
    }

    public static boolean isListEmpty(CompoundTag tag, String key) {
        return !tag.contains(key) || tag.getCompound(key).getInt("Size") == 0;
    }
}