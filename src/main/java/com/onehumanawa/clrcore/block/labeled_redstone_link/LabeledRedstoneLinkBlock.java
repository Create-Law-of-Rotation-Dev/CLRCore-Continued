package com.onehumanawa.clrcore.block.labeled_redstone_link;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModBlockEntityTypes;
import com.onehumanawa.clrcore.network.OpenLabeledRedstoneLinkGuiPacket;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class LabeledRedstoneLinkBlock extends WrenchableDirectionalBlock implements IBE<LabeledRedstoneLinkBlockEntity> {

    public static final BooleanProperty RECEIVER = BooleanProperty.create("receiver");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LabeledRedstoneLinkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(FACING, Direction.UP)
                        .setValue(RECEIVER, false)
                        .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RECEIVER, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos neighbourPos = pos.relative(facing.getOpposite());
        return !world.getBlockState(neighbourPos).canBeReplaced();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return AllShapes.REDSTONE_BRIDGE.get(state.getValue(FACING));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(RECEIVER) && state.getValue(POWERED);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        if (!state.getValue(RECEIVER)) {
            return 0;
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof LabeledRedstoneLinkBlockEntity lrbe) {
            return lrbe.getReceivedSignal();
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return side != state.getValue(FACING) ? 0 : this.getSignal(state, world, pos, side);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            if (fromPos.equals(pos.relative(facing.getOpposite())) && !this.canSurvive(state, level, pos)) {
                level.destroyBlock(pos, true);
            } else {
                this.updateTransmittedSignal(state, level, pos);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock() && !isMoving) {
            this.updateTransmittedSignal(state, level, pos);
        }
    }

    public void updateTransmittedSignal(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) return;

        if (!state.getValue(RECEIVER)) {
            int power = this.getPower(level, state, pos);
            int powerFromPanels = 0;

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LabeledRedstoneLinkBlockEntity lrbe) {
                // 安全调用 panelSupport
                Object support = lrbe.panelSupport;
                if (support != null) {
                    try {
                        // 使用反射调用 shouldBePoweredTristate
                        java.lang.reflect.Method method = support.getClass().getMethod("shouldBePoweredTristate");
                        Boolean tri = (Boolean) method.invoke(support);
                        if (tri == null) return;
                        powerFromPanels = Boolean.TRUE.equals(tri) ? 15 : 0;
                    } catch (Exception e) {
                        // panelSupport 不支持该方法，忽略
                    }
                }
            }

            power = Math.max(power, powerFromPanels);
            boolean currentlyPowered = state.getValue(POWERED);
            boolean shouldBePowered = power > 0;

            if (currentlyPowered != shouldBePowered) {
                level.setBlock(pos, state.cycle(POWERED), 2);
            }

            int finalPower = power;
            this.withBlockEntityDo(level, pos, ble -> ble.transmit(finalPower));
        }
    }


    private int getPower(Level level, BlockState state, BlockPos pos) {
        int power = 0;
        Direction facing = state.getValue(FACING);

        for (Direction d : Iterate.directions) {
            power = Math.max(power, level.getSignal(pos.relative(d), d));
        }

        for (Direction d : Iterate.directions) {
            if (d != facing.getOpposite()) {
                power = Math.max(power, level.getSignal(pos.relative(d), Direction.UP));
            }
        }

        return power;
    }

    public InteractionResult toggleReceiverMode(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean wasReceiver = state.getValue(RECEIVER);
        BlockState newState = state.setValue(RECEIVER, !wasReceiver).setValue(POWERED, false);
        level.setBlock(pos, newState, 3);

        this.withBlockEntityDo(level, pos, be -> {
            if (!wasReceiver) {
                LabeledRedstoneLinkNetworkHandler handler = LabeledRedstoneLinkNetworkHandler.get(level);
                if (handler != null) {
                    be.transmit(0);
                }
            } else {
                be.onReceivedSignal(0);
                this.updateTransmittedSignal(newState, level, pos);
            }
        });

        level.scheduleTick(pos, this, 1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return this.toggleReceiverMode(state, context.getLevel(), context.getClickedPos());
    }

    @NotNull
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (AllItems.WRENCH.isIn(player.getMainHandItem())) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            return this.toggleReceiverMode(state, level, pos);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LabeledRedstoneLinkBlockEntity lrbe) {
            CLRCore.CHANNEL.sendTo(
                    new OpenLabeledRedstoneLinkGuiPacket(pos, lrbe.getFrequencyText()),
                    ((ServerPlayer) player).connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getValue(RECEIVER)) {
            Direction facing = state.getValue(FACING);
            BlockPos attachedPos = pos.relative(facing.getOpposite());
            level.blockUpdated(attachedPos, level.getBlockState(attachedPos).getBlock());
        }

        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public Class<LabeledRedstoneLinkBlockEntity> getBlockEntityClass() {
        return LabeledRedstoneLinkBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LabeledRedstoneLinkBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.LABELED_REDSTONE_LINK.get();
    }
}