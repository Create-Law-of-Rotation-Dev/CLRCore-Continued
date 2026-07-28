package com.onehumanawa.clrcore.item;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModMenuTypes;
import com.onehumanawa.clrcore.client.NetworkManagerClientHandler;
import com.onehumanawa.clrcore.network.OpenNetworkManagerGuiPacket;
import com.onehumanawa.clrcore.network.OpenNetworkManagerEditorPacket;
import com.onehumanawa.clrcore.screen.NetworkManagerLabelEditorMenu;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (NetworkSelectedState.fromItemStack(stack) == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockEntity be = level.getBlockEntity(context.getClickedPos());
        if (be == null) return InteractionResult.PASS;

        LogisticallyLinkedBehaviour linkedBehaviour = getBehaviour(be);
        boolean isPanel = be instanceof FactoryPanelBlockEntity;
        if (linkedBehaviour == null && !isPanel) return InteractionResult.PASS;

        if (level.isClientSide) {
            NetworkManagerClientHandler.startLongPressTracking(context.getClickedPos(), context.getClickLocation(), context.getHand());
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        final InteractionHand hand = context.getHand();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        BlockEntity be = level.getBlockEntity(context.getClickedPos());

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        final UUID networkId;
        if (be != null) {
            networkId = getFreqId(be);
        } else {
            networkId = null;
        }

        if (networkId == null) {
            List<NetworkLabel> labels = getLabels(stack);
            CLRCore.CHANNEL.sendToServer(new OpenNetworkManagerGuiPacket(hand, labels));
        } else {
            final List<NetworkLabel> labels = getLabels(stack);
            final Optional<UUID> targetNetworkId = Optional.of(networkId);

            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new NetworkManagerLabelEditorMenu(
                            ModMenuTypes.NETWORK_MANAGER_LABEL_EDITOR.get(),
                            id,
                            inv,
                            hand,
                            labels,
                            targetNetworkId
                    );
                }
            }, buf -> {
                buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
                // 写入 labels 列表
                buf.writeInt(labels.size());
                for (NetworkLabel label : labels) {
                    label.serialize(buf);
                }
                buf.writeBoolean(true);
                buf.writeUUID(networkId);
            });
        }

        return InteractionResult.SUCCESS;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        NetworkSelectedState state = getSelectedState(player);
        if (state == null) return;

        Component text = Component.translatable("clrcore.hud.network_manager.selected_prefix")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(state.getLabelName()).withStyle(ChatFormatting.GOLD));

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int textWidth = mc.font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 61;

        event.getGuiGraphics().drawString(mc.font, text, x, y, 0xFFFFFF, true);
    }

    // ==================== Helper Methods ====================

    public static LogisticallyLinkedBehaviour getBehaviour(BlockEntity be) {
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
    private static NetworkSelectedState getSelectedState(Player player) {
        if (player == null) return null;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof NetworkManagerItem) {
                NetworkSelectedState state = NetworkSelectedState.fromItemStack(stack);
                if (state != null) return state;
            }
        }
        return null;
    }
}