package com.onehumanawa.clrcore.contents.registry.block.labeled_redstone_link;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.WeakHashMap;

public class LabeledRedstoneLinkNetworkHandler {

    private static final Map<ServerLevel, LabeledRedstoneLinkNetworkHandler> INSTANCES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private final Map<String, Set<LabeledRedstoneLinkBlockEntity>> networks = new HashMap<>();

    private LabeledRedstoneLinkNetworkHandler() {}

    public static LabeledRedstoneLinkNetworkHandler get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return INSTANCES.computeIfAbsent(serverLevel, k -> new LabeledRedstoneLinkNetworkHandler());
        }
        return null;
    }

    public void addToNetwork(LabeledRedstoneLinkBlockEntity be) {
        String freq = be.getFrequencyText();
        networks.computeIfAbsent(freq, k -> new HashSet<>()).add(be);
    }

    public void removeFromNetwork(LabeledRedstoneLinkBlockEntity be) {
        String freq = be.getFrequencyText();
        Set<LabeledRedstoneLinkBlockEntity> group = networks.get(freq);
        if (group != null) {
            group.remove(be);
            if (group.isEmpty()) {
                networks.remove(freq);
            }
        }
    }

    public void updateNetworkOf(LabeledRedstoneLinkBlockEntity transmitter) {
        String freq = transmitter.getFrequencyText();
        updateAll(freq);
    }

    public void updateAll(String frequency) {
        Set<LabeledRedstoneLinkBlockEntity> group = networks.get(frequency);
        if (group == null || group.isEmpty()) return;

        int maxPower = 0;
        for (LabeledRedstoneLinkBlockEntity be : group) {
            if (!be.isReceiver()) {
                maxPower = Math.max(maxPower, be.getTransmittedSignal());
            }
        }

        for (LabeledRedstoneLinkBlockEntity be : group) {
            if (be.isReceiver()) {
                be.onReceivedSignal(maxPower);
            }
        }
    }

    public void onFrequencyChanged(LabeledRedstoneLinkBlockEntity be, String oldFreq) {
        Set<LabeledRedstoneLinkBlockEntity> oldGroup = networks.get(oldFreq);
        if (oldGroup != null) {
            oldGroup.remove(be);
            if (oldGroup.isEmpty()) {
                networks.remove(oldFreq);
            }
            updateAll(oldFreq);
        }

        addToNetwork(be);
        updateAll(be.getFrequencyText());
    }
}