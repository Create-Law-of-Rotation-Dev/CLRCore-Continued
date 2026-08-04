package com.onehumanawa.clrcore.contents.registry.block.brass_scrap_bucket;

import com.onehumanawa.clrcore.contents.ModBlockEntityTypes;
import com.onehumanawa.clrcore.contents.ModMenuTypes;
import com.onehumanawa.clrcore.contents.registry.screen.brass_scrap_bucket.BrassScrapBucketMenu;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
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

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BrassScrapBucketBlockEntity brassBE)) {
            return InteractionResult.PASS;
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
            player.displayClientMessage(
                    Component.translatable("block.clrcore.brass_scrap_bucket.no_produced"),
                    true
            );
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        // 检查上方是否有容器
        int attachType = brassBE.getAttachType();
        if (attachType == BrassScrapBucketBlockEntity.ATTACH_NONE) {
            player.displayClientMessage(
                    Component.translatable("block.clrcore.brass_scrap_bucket.no_container"),
                    true
            );
            return InteractionResult.PASS;
        }

        // 在 lambda 外部提前计算好所有需要的数据，作为 final 变量传入
        final int fAttachType = attachType;
        final int fKeepAmount = brassBE.keepAmount;
        final boolean fKeepInStacks = brassBE.keepInStacks;
        final ItemStack fFilter = brassBE.filtering.getFilter();

        final int fMaxItems;
        final int fMaxStacks;
        final int fCurrentAmount;
        final int fCurrentStacks;

        boolean hasFilter = !fFilter.isEmpty();

        if (fAttachType == 1) {
            fMaxItems = brassBE.getAboveMaxItems();
            fMaxStacks = brassBE.getAboveMaxStacks();
            if (hasFilter) {
                fCurrentAmount = brassBE.getFilteredCurrentItems();
                fCurrentStacks = brassBE.getFilteredCurrentStacks();
            } else {
                fCurrentAmount = brassBE.getAboveCurrentItems();
                fCurrentStacks = brassBE.getAboveCurrentStacks();
            }
        } else if (fAttachType == 2) {
            fMaxItems = 0;
            fMaxStacks = 0;
            if (hasFilter) {
                fCurrentAmount = brassBE.getFilteredCurrentFluids();
            } else {
                fCurrentAmount = brassBE.getAboveCurrentFluids();
            }
            fCurrentStacks = 0;
        } else {
            fMaxItems = 0;
            fMaxStacks = 0;
            fCurrentAmount = 0;
            fCurrentStacks = 0;
        }

        // 使用 NetworkHooks.openScreen 打开 GUI
        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.clrcore.brass_scrap_bucket");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                return new BrassScrapBucketMenu(
                        ModMenuTypes.BRASS_SCRAP_BUCKET.get(),
                        id,
                        inv,
                        pos,
                        fAttachType,
                        fKeepAmount,
                        fKeepInStacks,
                        fMaxItems,
                        fMaxStacks,
                        fCurrentAmount,
                        fCurrentStacks,
                        fFilter
                );
            }
        }, buf -> {
            // 写入数据到网络包，客户端构造 Menu 时会读取
            buf.writeBlockPos(pos);
            buf.writeInt(fAttachType);
            buf.writeInt(fKeepAmount);
            buf.writeBoolean(fKeepInStacks);
            buf.writeInt(fMaxItems);
            buf.writeInt(fMaxStacks);
            buf.writeInt(fCurrentAmount);
            buf.writeInt(fCurrentStacks);
            buf.writeItem(fFilter);
        });

        return InteractionResult.CONSUME;
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