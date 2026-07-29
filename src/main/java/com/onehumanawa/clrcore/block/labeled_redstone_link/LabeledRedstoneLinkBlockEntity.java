package com.onehumanawa.clrcore.block.labeled_redstone_link;

import com.onehumanawa.clrcore.ModBlockEntityTypes;
import com.simibubi.create.content.equipment.clipboard.ClipboardCloneable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class LabeledRedstoneLinkBlockEntity extends SmartBlockEntity implements ClipboardCloneable {

    public static final String DEFAULT_FREQUENCY = "默认红石频率";
    private static final String CLIPBOARD_KEY = "labeled_redstone_link";

    private String frequencyText = DEFAULT_FREQUENCY;
    private int transmittedSignal = 0;
    private int receivedSignal = 0;
    private boolean receivedSignalChanged = false;

    // FactoryPanelSupportBehaviour 来自 Create，暂不启用
    public Object panelSupport; // 临时用 Object 占位，后续适配

    public LabeledRedstoneLinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LABELED_REDSTONE_LINK.get(), pos, state);
    }

    @Override
    public void addBehaviours(List behaviours) {
        // FactoryPanelSupportBehaviour 暂不实现
        // this.panelSupport = new FactoryPanelSupportBehaviour(this, this::isReceiver, () -> this.receivedSignal > 0, this::notifyTransmitSignal);
        // behaviours.add(this.panelSupport);
    }

    private void notifyTransmitSignal() {
        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof LabeledRedstoneLinkBlock block) {
            block.updateTransmittedSignal(state, this.level, this.worldPosition);
        }
    }

    public String getFrequencyText() {
        return this.frequencyText;
    }

    public void setFrequencyText(String text) {
        String newFreq = (text != null && !text.isBlank()) ? text : DEFAULT_FREQUENCY;
        if (!newFreq.equals(this.frequencyText)) {
            if (this.level != null && !this.level.isClientSide()) {
                LabeledRedstoneLinkNetworkHandler handler = LabeledRedstoneLinkNetworkHandler.get(this.level);
                String old = this.frequencyText;
                this.frequencyText = newFreq;
                if (handler != null) {
                    handler.onFrequencyChanged(this, old);
                }
                this.sendData();
            } else {
                this.frequencyText = newFreq;
            }
            this.setChanged();
        }
    }

    public boolean isReceiver() {
        BlockState state = this.getBlockState();
        return state.hasProperty(LabeledRedstoneLinkBlock.RECEIVER) && state.getValue(LabeledRedstoneLinkBlock.RECEIVER);
    }

    public void transmit(int power) {
        this.transmittedSignal = power;
        LabeledRedstoneLinkNetworkHandler handler = LabeledRedstoneLinkNetworkHandler.get(this.level);
        if (handler != null) {
            handler.updateNetworkOf(this);
        }
    }

    public int getTransmittedSignal() {
        return this.transmittedSignal;
    }

    public void onReceivedSignal(int power) {
        if (this.receivedSignal != power) {
            this.receivedSignalChanged = true;
            this.receivedSignal = power;
        }
    }

    public int getReceivedSignal() {
        return this.receivedSignal;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide()) return;

        BlockState state = this.getBlockState();

        if (!this.isReceiver()) {
            if (state.getBlock() instanceof LabeledRedstoneLinkBlock block) {
                block.updateTransmittedSignal(state, this.level, this.worldPosition);
            }
        } else {
            boolean shouldBePowered = this.receivedSignal > 0;
            boolean currentlyPowered = state.getValue(LabeledRedstoneLinkBlock.POWERED);

            if (shouldBePowered != currentlyPowered) {
                this.receivedSignalChanged = true;
                this.level.setBlockAndUpdate(this.worldPosition, state.setValue(LabeledRedstoneLinkBlock.POWERED, shouldBePowered));
            }

            if (this.receivedSignalChanged) {
                this.updateSelfAndAttached(state);
            }
        }
    }

    private void updateSelfAndAttached(BlockState state) {
        if (this.level == null) return;

        Direction facing = state.getValue(LabeledRedstoneLinkBlock.FACING);
        BlockPos attachedPos = this.worldPosition.relative(facing.getOpposite());

        this.level.blockUpdated(this.worldPosition, state.getBlock());
        this.level.blockUpdated(attachedPos, this.level.getBlockState(attachedPos).getBlock());

        this.receivedSignalChanged = false;

        // if (this.panelSupport != null) {
        //     this.panelSupport.notifyPanels();
        // }
    }

    @Override
    public void initialize() {
        super.initialize();
        if (this.level != null && !this.level.isClientSide()) {
            LabeledRedstoneLinkNetworkHandler handler = LabeledRedstoneLinkNetworkHandler.get(this.level);
            if (handler != null) {
                handler.addToNetwork(this);
            }
        }
    }

    @Override
    public void remove() {
        if (this.level != null && !this.level.isClientSide()) {
            LabeledRedstoneLinkNetworkHandler handler = LabeledRedstoneLinkNetworkHandler.get(this.level);
            if (handler != null) {
                handler.removeFromNetwork(this);
                handler.updateAll(this.frequencyText);
            }
            this.updateSelfAndAttached(this.getBlockState());
        }
        super.remove();
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putString("FrequencyText", this.frequencyText);
        tag.putInt("TransmittedSignal", this.transmittedSignal);
        tag.putInt("ReceivedSignal", this.receivedSignal);
        tag.putBoolean("ReceivedSignalChanged", this.receivedSignalChanged);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        this.frequencyText = tag.getString("FrequencyText");
        if (this.frequencyText.isBlank()) {
            this.frequencyText = DEFAULT_FREQUENCY;
        }
        this.transmittedSignal = tag.getInt("TransmittedSignal");
        this.receivedSignal = tag.getInt("ReceivedSignal");
        this.receivedSignalChanged = tag.getBoolean("ReceivedSignalChanged");
    }

    @Override
    public String getClipboardKey() {
        return CLIPBOARD_KEY;
    }

    @Override
    public boolean writeToClipboard(CompoundTag tag, Direction side) {
        tag.putString("Frequency", this.frequencyText);
        return true;
    }

    @Override
    public boolean readFromClipboard(CompoundTag tag, Player player, Direction side, boolean simulate) {
        if (!tag.contains("Frequency")) {
            return false;
        }
        if (simulate) {
            return true;
        }
        this.setFrequencyText(tag.getString("Frequency"));
        return true;
    }
}