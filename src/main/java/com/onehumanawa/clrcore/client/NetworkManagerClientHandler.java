package com.onehumanawa.clrcore.client;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.config.CLRCoreConfig;
import com.onehumanawa.clrcore.item.NetworkManagerItem;
import com.onehumanawa.clrcore.item.NetworkSelectedState;
import com.onehumanawa.clrcore.network.ApplyNetworkPacket;
import com.onehumanawa.clrcore.screen.NetworkManagerConfigScreen;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CLRCore.MOD_ID, value = Dist.CLIENT)
public class NetworkManagerClientHandler {

    private static int longPressTicks = -1;
    private static BlockPos longPressPos = null;
    private static Vec3 longPressClickLocation = null;
    private static InteractionHand longPressHand = null;

    private static UUID previouslyHeldFrequency = null;

    public static void startLongPressTracking(BlockPos pos, Vec3 clickLocation, InteractionHand hand) {
        longPressTicks = 0;
        longPressPos = pos;
        longPressClickLocation = clickLocation;
        longPressHand = hand;
    }

    public static void cancelLongPress() {
        longPressTicks = -1;
        longPressPos = null;
        longPressClickLocation = null;
        longPressHand = null;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isClient()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            BlockPos pos = event.getPos();

            if (longPressTicks != -1) {
                if (longPressPos != null && longPressPos.equals(pos)) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.FAIL);
                }
                return;
            }

            NetworkSelectedState state = NetworkSelectedState.fromItemStack(player.getMainHandItem());
            if (state == null) {
                state = NetworkSelectedState.fromItemStack(player.getOffhandItem());
            }
            if (state == null) return;

            if (!player.isShiftKeyDown()) {
                if (mc.level != null) {
                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be != null) {
                        boolean isTarget = NetworkManagerItem.getBehaviour(be) != null;
                        if (isTarget) {
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.FAIL);
                            Vec3 clickLocation = event.getHitVec() != null ? event.getHitVec().getLocation() : Vec3.atCenterOf(pos);
                            startLongPressTracking(pos, clickLocation, event.getHand());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        NetworkSelectedState state = getSelectedState(player);
        if (state == null) return;

        UUID networkId = state.getNetworkId();
        previouslyHeldFrequency = networkId;

        if (mc.level == null) return;

        for (LogisticallyLinkedBehaviour behaviour : LogisticallyLinkedBehaviour.getAllPresent(networkId, false, false)) {
            BlockEntity be = behaviour.blockEntity;
            if (be == null || be.isRemoved()) continue;
            if (!player.blockPosition().closerThan(be.getBlockPos(), 64.0)) continue;

            VoxelShape shape = be.getBlockState().getShape(be.getLevel(), be.getBlockPos());
            if (shape.isEmpty()) continue;

            List<AABB> aabbs = shape.toAabbs();
            for (int i = 0; i < aabbs.size(); i++) {
                AABB aabb = aabbs.get(i).inflate(-0.0078125).move(be.getBlockPos());
                // 使用简单线框绘制，这里为了简洁用PoseStack绘制，后续完善
                // 或者使用Outliner（需Create依赖）
            }
        }
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

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        // 处理长按释放
        if (longPressTicks == -1) return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.keyUse.isDown()) {
            int threshold = CLRCoreConfig.SERVER.longPressThreshold.get();
            if (longPressTicks < threshold) {
                CLRCore.CHANNEL.sendToServer(new ApplyNetworkPacket(
                        longPressHand,
                        longPressPos,
                        longPressClickLocation,
                        false
                ));
            }
            cancelLongPress();
        } else {
            if (longPressTicks > 3) {
                mc.player.swinging = false;
            }
            longPressTicks++;
            int threshold = CLRCoreConfig.SERVER.longPressThreshold.get();
            if (longPressTicks >= threshold) {
                // 打开配置界面
                NetworkManagerConfigScreen.open(longPressHand, longPressPos, longPressClickLocation);
                cancelLongPress();
            }
        }
    }

    private static NetworkSelectedState getSelectedState(LocalPlayer player) {
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