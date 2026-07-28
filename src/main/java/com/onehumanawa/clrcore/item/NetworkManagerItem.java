package com.onehumanawa.clrcore.item;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModMenuTypes;
import com.onehumanawa.clrcore.network.ApplyNetworkPacket;
import com.onehumanawa.clrcore.network.OpenNetworkManagerGuiPacket;
import com.onehumanawa.clrcore.screen.NetworkManagerLabelEditorMenu;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NetworkManagerItem extends Item {

    private static final String NBT_LABELS = "NetworkManagerLabels";
    private static final String NBT_SEARCH = "NetworkManagerSearch";

    public NetworkManagerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return NetworkSelectedState.fromItemStack(stack) != null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        NetworkSelectedState state = NetworkSelectedState.fromItemStack(stack);
        if (state != null) {
            MutableComponent line = Component.translatable("clrcore.hud.network_manager.selected_prefix")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(state.getLabelName()).withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(line);
        }

        tooltipComponents.add(Component.translatable("clrcore.message.network_manager.applied_whole_shift")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static List<NetworkLabel> getLabels(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return List.of();
        return NetworkLabel.listFromNBT(stack.getTag(), NBT_LABELS);
    }

    public static void setLabels(ItemStack stack, List<NetworkLabel> labels) {
        NetworkLabel.listToNBT(stack.getOrCreateTag(), NBT_LABELS, labels);
    }

    public static String getSearch(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return "";
        return stack.getTag().getString(NBT_SEARCH);
    }

    public static void setSearch(ItemStack stack, String search) {
        if (search.isEmpty()) {
            stack.getOrCreateTag().remove(NBT_SEARCH);
        } else {
            stack.getOrCreateTag().putString(NBT_SEARCH, search);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            List<NetworkLabel> labels = getLabels(stack);
            CLRCore.CHANNEL.sendToServer(new OpenNetworkManagerGuiPacket(hand, labels));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        final InteractionHand hand = context.getHand();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        BlockEntity be = level.getBlockEntity(pos);

        // 检查是否有选中的网络
        NetworkSelectedState state = NetworkSelectedState.fromItemStack(stack);
        if (state == null) {
            // 未选中网络，打开标签界面
            List<NetworkLabel> labels = getLabels(stack);
            CLRCore.CHANNEL.sendToServer(new OpenNetworkManagerGuiPacket(hand, labels));
            return InteractionResult.SUCCESS;
        }

        // 检查目标是否有效
        boolean isTarget = getBehaviour(be) != null;
        if (!isTarget) {
            return InteractionResult.PASS;
        }

        // 任何手持网络管理器右键点击元件都应用网络
        // Shift + 右键 = 整个网络，普通右键 = 单个设备
        boolean applyToWholeNetwork = player.isShiftKeyDown();

        // 直接发送应用包（不打开任何界面）
        CLRCore.CHANNEL.sendToServer(new ApplyNetworkPacket(
                hand,
                pos,
                context.getClickLocation(),
                applyToWholeNetwork
        ));

        // 显示提示
        Component message = Component.translatable(
                applyToWholeNetwork ?
                        "clrcore.message.network_manager.applied_whole" :
                        "clrcore.message.network_manager.applied_single"
        );
        player.displayClientMessage(message, true);

        return InteractionResult.SUCCESS;
    }

    // ==================== Helper Methods ====================

    public static LogisticallyLinkedBehaviour getBehaviour(BlockEntity be) {
        if (be == null) return null;
        return (LogisticallyLinkedBehaviour) BlockEntityBehaviour.get(be, LogisticallyLinkedBehaviour.TYPE);
    }

    public static UUID getFreqId(BlockEntity be) {
        if (be == null) return null;
        try {
            LogisticallyLinkedBehaviour behaviour = getBehaviour(be);
            if (behaviour == null) return null;
            Field freqIdField = LogisticallyLinkedBehaviour.class.getDeclaredField("freqId");
            freqIdField.setAccessible(true);
            return (UUID) freqIdField.get(behaviour);
        } catch (Exception e) {
            return null;
        }
    }

    public static void reassignNetwork(LogisticallyLinkedBehaviour behaviour, BlockEntity be, UUID newNetworkId) {
        try {
            LogisticallyLinkedBehaviour.remove(behaviour);
            behaviour.destroy();
            Field freqIdField = LogisticallyLinkedBehaviour.class.getDeclaredField("freqId");
            freqIdField.setAccessible(true);
            freqIdField.set(behaviour, newNetworkId);
            Field addedField = LogisticallyLinkedBehaviour.class.getDeclaredField("addedGlobally");
            addedField.setAccessible(true);
            addedField.set(behaviour, false);
            Field loadedField = LogisticallyLinkedBehaviour.class.getDeclaredField("loadedGlobally");
            loadedField.setAccessible(true);
            loadedField.set(behaviour, false);
            behaviour.initialize();
            be.setChanged();
        } catch (Exception e) {
            CLRCore.LOGGER.error("NetworkManager: failed to reassign network", e);
        }
    }
}