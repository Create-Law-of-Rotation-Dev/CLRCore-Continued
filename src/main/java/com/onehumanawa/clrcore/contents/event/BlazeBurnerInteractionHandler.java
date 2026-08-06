package com.onehumanawa.clrcore.contents.event;

import com.onehumanawa.clrcore.core.CLRCore;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CLRCore.MOD_ID)
public class BlazeBurnerInteractionHandler {

    private static final String KINDLED_FUEL_ID = "clrcore:kindled_fuel_rod";
    private static final String SEETHING_FUEL_ID = "clrcore:seething_fuel_rod";

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        InteractionHand hand = event.getHand();
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();

        if (!(state.getBlock() instanceof BlazeBurnerBlock)) {
            return;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(heldItem.getItem());
        if (itemId == null) {
            return;
        }
        String itemIdStr = itemId.toString();

        if (itemIdStr.equals(KINDLED_FUEL_ID)) {
            applyKindledFuel(level, pos, state, player, heldItem);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (itemIdStr.equals(SEETHING_FUEL_ID)) {
            applySeethingFuel(level, pos, state, player, heldItem);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static void applyKindledFuel(Level level, BlockPos pos, BlockState state,
                                         Player player, ItemStack heldItem) {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof BlazeBurnerBlockEntity burnerBE) {
            if (burnerBE.isCreative()) {
                CLRCore.LOGGER.debug("Blaze Burner already in creative mode");
                return;
            }

            setBurnerToCreative(level, pos, state, BlazeBurnerBlock.HeatLevel.KINDLED);

            if (!player.isCreative()) {
                heldItem.shrink(1);
            }

            level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.5f, 1.0f);
            CLRCore.LOGGER.info("Applied KINDLED fuel to Blaze Burner at {}", pos);
        }
    }

    private static void applySeethingFuel(Level level, BlockPos pos, BlockState state,
                                          Player player, ItemStack heldItem) {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof BlazeBurnerBlockEntity burnerBE) {
            if (burnerBE.isCreative()) {
                CLRCore.LOGGER.debug("Blaze Burner already in creative mode");
                return;
            }

            setBurnerToCreative(level, pos, state, BlazeBurnerBlock.HeatLevel.SEETHING);

            if (!player.isCreative()) {
                heldItem.shrink(1);
            }

            level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.75f, 1.5f);
            level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5f, 1.2f);
            CLRCore.LOGGER.info("Applied SEETHING fuel to Blaze Burner at {}", pos);
        }
    }

    private static void setBurnerToCreative(Level level, BlockPos pos, BlockState state,
                                            BlazeBurnerBlock.HeatLevel heatLevel) {
        try {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof BlazeBurnerBlockEntity burnerBE)) {
                return;
            }

            java.lang.reflect.Field isCreativeField = BlazeBurnerBlockEntity.class
                    .getDeclaredField("isCreative");
            isCreativeField.setAccessible(true);
            isCreativeField.setBoolean(burnerBE, true);

            Class<?> fuelTypeClass = Class.forName("com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity$FuelType");
            Object noneFuel = Enum.valueOf((Class<Enum>) fuelTypeClass, "NONE");
            java.lang.reflect.Field activeFuelField = BlazeBurnerBlockEntity.class
                    .getDeclaredField("activeFuel");
            activeFuelField.setAccessible(true);
            activeFuelField.set(burnerBE, noneFuel);

            java.lang.reflect.Field remainingBurnTimeField = BlazeBurnerBlockEntity.class
                    .getDeclaredField("remainingBurnTime");
            remainingBurnTimeField.setAccessible(true);
            remainingBurnTimeField.setInt(burnerBE, 0);

            BlockState newState = state.setValue(BlazeBurnerBlock.HEAT_LEVEL, heatLevel);
            level.setBlockAndUpdate(pos, newState);

            burnerBE.setChanged();
            burnerBE.notifyUpdate();

        } catch (Exception e) {
            CLRCore.LOGGER.error("Failed to set burner to creative mode", e);
        }
    }
}