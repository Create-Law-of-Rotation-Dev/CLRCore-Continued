package com.onehumanawa.clrcore.block.andesite_scrap_bucket;

import com.onehumanawa.clrcore.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AndesiteScrapBucketBlockEntity extends BlockEntity {

    private static final int SLOT_COUNT = 1;
    private static final int INPUT_SLOT = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            // 摧毁一切输入的物品（无黑名单）
            if (slot == INPUT_SLOT && !stack.isEmpty()) {
                if (!simulate) {
                    setChanged();
                }
                return ItemStack.EMPTY;
            }
            return stack;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // 接受一切物品
            return slot == INPUT_SLOT;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            // 摧毁一切输入的流体（无黑名单）
            if (!action.simulate()) {
                setChanged();
            }
            return resource.getAmount();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    private final LazyOptional<IItemHandler> itemHandlerCap = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IFluidHandler> fluidHandlerCap = LazyOptional.of(() -> fluidHandler);

    public AndesiteScrapBucketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ANDESITE_SCRAP_BUCKET.get(), pos, state);
    }

    public void drops() {
        if (level == null) return;
        // 安山岩废料桶不存储任何物品，无需掉落
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerCap.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerCap.invalidate();
        fluidHandlerCap.invalidate();
    }
}