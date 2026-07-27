package com.onehumanawa.clrcore.block;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModBlockEntityTypes;
import com.onehumanawa.clrcore.config.CLRCoreConfig;
import com.onehumanawa.clrcore.block.BrassScrapBucketFilterSlotPositioning;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

import java.util.List;
import java.util.Optional;

public class BrassScrapBucketBlockEntity extends SmartBlockEntity {

    // 槽位常量
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FILTER_SLOT = 2;
    private static final int SLOT_COUNT = 3;

    // 过滤器行为
    public FilteringBehaviour filtering;

    // 物品处理器
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return !ScrapBucketBlacklist.isBlacklisted(stack);
            }
            if (slot == OUTPUT_SLOT) {
                return isValidProducedItem(stack);
            }
            if (slot == FILTER_SLOT) {
                return stack.getItem() instanceof com.simibubi.create.content.logistics.filter.FilterItem;
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == FILTER_SLOT) {
                // 同步到FilteringBehaviour
                ItemStack filterStack = getStackInSlot(FILTER_SLOT);
                if (filtering != null) {
                    filtering.setFilter(filterStack);
                }
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == OUTPUT_SLOT) return 64;
            if (slot == FILTER_SLOT) return 1;
            return 64;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == INPUT_SLOT && !stack.isEmpty()) {
                // 检查过滤器
                if (filtering != null && !filtering.test(stack)) {
                    return stack;
                }
                if (ScrapBucketBlacklist.isBlacklisted(stack)) {
                    return stack;
                }
                if (!simulate) {
                    int count = stack.getCount();
                    accumulateItemFill(count);
                    setChanged();
                }
                return ItemStack.EMPTY;
            }
            if (slot == FILTER_SLOT) {
                if (!(stack.getItem() instanceof com.simibubi.create.content.logistics.filter.FilterItem)) {
                    return stack;
                }
                if (getStackInSlot(FILTER_SLOT).isEmpty()) {
                    if (!simulate) {
                        ItemStack copy = stack.copy();
                        copy.setCount(1);
                        super.insertItem(FILTER_SLOT, copy, false);
                        stack.shrink(1);
                        if (filtering != null) {
                            filtering.setFilter(copy);
                        }
                        setChanged();
                    }
                    return stack.getCount() > 1 ? stack.copyWithCount(stack.getCount() - 1) : ItemStack.EMPTY;
                }
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == OUTPUT_SLOT) {
                return super.extractItem(slot, amount, simulate);
            }
            if (slot == FILTER_SLOT) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
    };

    // 流体处理器
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
            return !ScrapBucketBlacklist.isBlacklisted(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            if (ScrapBucketBlacklist.isBlacklisted(resource)) return 0;
            // 检查过滤器（FluidFilter支持）
            if (filtering != null && !filtering.test(resource)) {
                return 0;
            }
            if (!action.simulate()) {
                accumulateFluidFill(resource.getAmount());
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

    // 内部状态
    private int itemFill = 0;
    private int fluidFill = 0;
    private ItemStack producedStack = ItemStack.EMPTY;

    public BrassScrapBucketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BRASS_SCRAP_BUCKET.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // 添加过滤器行为
        filtering = new FilteringBehaviour(this, new BrassScrapBucketFilterSlotPositioning())
                .withCallback((stack) -> {
                    // 当过滤器变化时同步到itemHandler
                    ItemStack filterStack = stack.copy();
                    if (!filterStack.isEmpty()) {
                        filterStack.setCount(1);
                    }
                    // 更新itemHandler中的过滤槽
                    ItemStack currentFilter = itemHandler.getStackInSlot(FILTER_SLOT);
                    if (!ItemStack.matches(currentFilter, filterStack)) {
                        itemHandler.setStackInSlot(FILTER_SLOT, filterStack);
                        setChanged();
                    }
                });
        behaviours.add(filtering);
    }

    // 获取过滤槽物品
    public ItemStack getFilterSlot() {
        return itemHandler.getStackInSlot(FILTER_SLOT);
    }

    public static Item resolveProduceItem() {
        String id = CLRCoreConfig.SERVER.brassScrapBucketProduceItem.get();
        if (id != null && !id.isBlank()) {
            ResourceLocation rl = ResourceLocation.tryParse(id.trim());
            if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) {
                return BuiltInRegistries.ITEM.get(rl);
            }
        }
        return null;
    }

    private boolean canProduceAccumulate() {
        if (!CLRCoreConfig.SERVER.generateExperienceNuggets.get()) return false;
        Item produceItem = resolveProduceItem();
        if (produceItem == null) return false;
        return producedStack.isEmpty() || producedStack.is(produceItem);
    }

    private void produceOneUnit(Item produceItem) {
        if (producedStack.isEmpty()) {
            producedStack = new ItemStack(produceItem, 1);
        } else {
            producedStack.grow(1);
        }
    }

    private void accumulateItemFill(int count) {
        if (!canProduceAccumulate()) return;
        itemFill += count;
        tryProduce();
    }

    private void accumulateFluidFill(int mb) {
        if (!canProduceAccumulate()) return;
        fluidFill += mb;
        tryProduce();
    }

    private void tryProduce() {
        Item produceItem = resolveProduceItem();
        if (produceItem == null) return;

        int itemsPer = Math.max(1, CLRCoreConfig.SERVER.itemsPerNugget.get());
        int mbPer = Math.max(1, CLRCoreConfig.SERVER.mbPerNugget.get());

        while (itemFill >= itemsPer) {
            if (!tryAddToOutput(produceItem)) break;
            itemFill -= itemsPer;
        }

        while (fluidFill >= mbPer) {
            if (!tryAddToOutput(produceItem)) break;
            fluidFill -= mbPer;
        }

        if (itemFill >= itemsPer || fluidFill >= mbPer) {
            setChanged();
        }
    }

    private boolean tryAddToOutput(Item produceItem) {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(produceItem, 1));
            setChanged();
            return true;
        }
        if (outputStack.is(produceItem) && outputStack.getCount() < outputStack.getMaxStackSize()) {
            outputStack.grow(1);
            setChanged();
            return true;
        }
        if (producedStack.isEmpty()) {
            producedStack = new ItemStack(produceItem, 1);
            setChanged();
            return true;
        }
        if (producedStack.is(produceItem) && producedStack.getCount() < producedStack.getMaxStackSize()) {
            producedStack.grow(1);
            setChanged();
            return true;
        }
        return false;
    }

    private boolean isValidProducedItem(ItemStack stack) {
        Item produceItem = resolveProduceItem();
        if (produceItem == null) return false;
        return stack.is(produceItem);
    }

    public ItemStack takeAllProduced() {
        if (producedStack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = producedStack.copy();
        producedStack = ItemStack.EMPTY;
        setChanged();
        return result;
    }

    public void drops() {
        if (level == null) return;
        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        if (!input.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), input);
        }
        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (!output.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), output);
        }
        if (!producedStack.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), producedStack);
            producedStack = ItemStack.EMPTY;
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("itemFill", itemFill);
        tag.putInt("fluidFill", fluidFill);
        if (!producedStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            producedStack.save(stackTag);
            tag.put("producedStack", stackTag);
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        itemFill = tag.getInt("itemFill");
        fluidFill = tag.getInt("fluidFill");
        if (tag.contains("producedStack")) {
            CompoundTag stackTag = tag.getCompound("producedStack");
            producedStack = ItemStack.of(stackTag);
        } else {
            producedStack = ItemStack.EMPTY;
        }
        // 同步过滤器到FilteringBehaviour
        if (filtering != null) {
            ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
            filtering.setFilter(filterStack);
        }
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