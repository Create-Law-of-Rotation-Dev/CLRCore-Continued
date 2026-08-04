package com.onehumanawa.clrcore.client.handler;

import com.onehumanawa.clrcore.core.CLRCore;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkManagerItem;
import com.onehumanawa.clrcore.contents.registry.item.network_manager.NetworkSelectedState;
import com.onehumanawa.clrcore.network.packets.network_manager.misc.ApplyNetworkPacket;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.outliner.Outliner;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CLRCore.MOD_ID, value = Dist.CLIENT)
public class NetworkManagerClientHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isClient()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            BlockPos pos = event.getPos();

            NetworkSelectedState state = NetworkSelectedState.fromItemStack(player.getMainHandItem());
            if (state == null) {
                state = NetworkSelectedState.fromItemStack(player.getOffhandItem());
            }
            if (state == null) return;

            if (mc.level != null) {
                BlockEntity be = mc.level.getBlockEntity(pos);
                if (be != null) {
                    boolean isTarget = NetworkManagerItem.getBehaviour(be) != null;
                    if (isTarget) {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.FAIL);

                        // Shift + 右键：切换到整个网络模式
                        boolean applyToWholeNetwork = player.isShiftKeyDown();
                        Vec3 clickLocation = event.getHitVec() != null ? event.getHitVec().getLocation() : Vec3.atCenterOf(pos);

                        CLRCore.CHANNEL.sendToServer(new ApplyNetworkPacket(
                                event.getHand(),
                                pos,
                                clickLocation,
                                applyToWholeNetwork
                        ));

                        // 显示提示信息
                        Component message = Component.translatable(
                                applyToWholeNetwork ?
                                        "clrcore.message.network_manager.applied_whole" :
                                        "clrcore.message.network_manager.applied_single"
                        );
                        player.displayClientMessage(message, true);
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
                int color = AnimationTickHolder.getTicks() % 16 < 8 ? 0x3399FF : 0x66CCFF;
                Outliner.getInstance()
                        .showAABB(be.getBlockPos(), aabb)
                        .lineWidth(0.03125F)
                        .disableLineNormals()
                        .colored(color);
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