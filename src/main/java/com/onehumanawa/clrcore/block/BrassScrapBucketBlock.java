package com.onehumanawa.clrcore.block;

import com.onehumanawa.clrcore.ModBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BrassScrapBucketBlock extends BaseEntityBlock implements IWrenchable {

    public BrassScrapBucketBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrassScrapBucketBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntityTypes.BRASS_SCRAP_BUCKET.get(),
                (lvl, pos, blockState, be) -> ((BrassScrapBucketBlockEntity) be).tick());
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrassScrapBucketBlockEntity brassBE) {
                // 检查是否点击了过滤器槽位
                FilteringBehaviour filtering = brassBE.filtering;
                if (filtering != null && filtering.testHit(hitResult.getLocation())) {
                    // 让FilteringBehaviour处理交互
                    filtering.onShortInteract(player, InteractionHand.MAIN_HAND, hitResult.getDirection(), hitResult);
                    return InteractionResult.SUCCESS;
                }

                // Shift + 右键：取出产物
                if (player.isShiftKeyDown()) {
                    ItemStack nuggets = brassBE.takeAllProduced();
                    if (!nuggets.isEmpty()) {
                        if (!player.getInventory().add(nuggets)) {
                            player.drop(nuggets, false);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("block.clrcore.brass_scrap_bucket.message"),
                        true
                );
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrassScrapBucketBlockEntity brassBE) {
                brassBE.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}