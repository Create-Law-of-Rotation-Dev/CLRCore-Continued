package com.onehumanawa.clrcore.block.brass_scrap_bucket;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModBlockEntityTypes;
import com.onehumanawa.clrcore.config.CLRCoreConfig;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassScrapBucketBlockEntity extends SmartBlockEntity {

    public static final int ATTACH_NONE = 0;
    public static final int ATTACH_ITEM = 1;
    public static final int ATTACH_FLUID = 2;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FILTER_SLOT = 2;
    private static final int SLOT_COUNT = 3;

    public FilteringBehaviour filtering;

    public int keepAmount = -1;
    public boolean keepInStacks = false;

    private int itemFill = 0;
    private int fluidFill = 0;
    private ItemStack producedStack = ItemStack.EMPTY;

    private int itemTickCounter = 0;
    private int fluidTickCounter = 0;

    private int currentAmount = 0;
    private int currentStacks = 0;

    // ==================== ItemHandler ====================
    public final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return false;
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
                if (filtering != null && !filtering.test(stack)) {
                    return stack;
                }
                if (!simulate) {
                    accumulateItemFill(stack.getCount());
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
                ItemStack outputStack = getStackInSlot(OUTPUT_SLOT);
                if (!outputStack.isEmpty()) {
                    ItemStack extracted = super.extractItem(OUTPUT_SLOT, amount, simulate);
                    if (!extracted.isEmpty()) {
                        return extracted;
                    }
                }

                if (!producedStack.isEmpty()) {
                    int extractedCount = Math.min(amount, producedStack.getCount());
                    if (extractedCount <= 0) return ItemStack.EMPTY;

                    ItemStack result = producedStack.copyWithCount(extractedCount);
                    if (!simulate) {
                        producedStack.shrink(extractedCount);
                        if (producedStack.isEmpty()) {
                            producedStack = ItemStack.EMPTY;
                        }
                        setChanged();
                    }
                    return result;
                }
                return ItemStack.EMPTY;
            }

            if (slot == FILTER_SLOT) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
    };

    // ==================== FluidHandler ====================
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
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
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

    // ==================== 构造器 ====================
    public BrassScrapBucketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BRASS_SCRAP_BUCKET.get(), pos, state);
    }

    // ==================== Behaviours ====================
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new FilteringBehaviour(this, new BrassScrapBucketFilterSlotPositioning())
                .withCallback((stack) -> {
                    ItemStack filterStack = stack.copy();
                    if (!filterStack.isEmpty()) {
                        filterStack.setCount(1);
                    }
                    ItemStack currentFilter = itemHandler.getStackInSlot(FILTER_SLOT);
                    if (!ItemStack.matches(currentFilter, filterStack)) {
                        itemHandler.setStackInSlot(FILTER_SLOT, filterStack);
                        setChanged();
                    }
                });
        behaviours.add(filtering);
    }

    // ==================== 获取过滤槽物品 ====================
    public ItemStack getFilterSlot() {
        return itemHandler.getStackInSlot(FILTER_SLOT);
    }

    // ==================== 检测上方容器类型 ====================
    public int getAttachType() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();

        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(TagKey.create(Registries.BLOCK, CLRCore.rl("scrap_bucket")))) {
            return 0;
        }

        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;

        var itemCap = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (itemCap != null && itemCap.isPresent()) {
            return ATTACH_ITEM;
        }

        var fluidCap = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (fluidCap != null && fluidCap.isPresent()) {
            return ATTACH_FLUID;
        }

        return 0;
    }

    // ==================== 上方容器查询方法 ====================
    public int getAboveMaxItems() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IItemHandler h = handler.orElse(null);
        if (h == null) return 0;
        int total = 0;
        for (int i = 0; i < h.getSlots(); i++) {
            total += Math.min(64, h.getSlotLimit(i));
        }
        return total;
    }

    public int getAboveMaxStacks() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IItemHandler h = handler.orElse(null);
        return h == null ? 0 : h.getSlots();
    }

    public int getAboveMaxFluids() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IFluidHandler h = handler.orElse(null);
        if (h == null) return 0;
        int total = 0;
        for (int i = 0; i < h.getTanks(); i++) {
            total += h.getTankCapacity(i);
        }
        return total / 1000;
    }

    public int getAboveCurrentItems() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IItemHandler h = handler.orElse(null);
        if (h == null) return 0;
        int total = 0;
        for (int i = 0; i < h.getSlots(); i++) {
            total += h.getStackInSlot(i).getCount();
        }
        return total;
    }

    public int getAboveCurrentStacks() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IItemHandler h = handler.orElse(null);
        if (h == null) return 0;
        int occupied = 0;
        for (int i = 0; i < h.getSlots(); i++) {
            if (!h.getStackInSlot(i).isEmpty()) occupied++;
        }
        return occupied;
    }

    public int getAboveCurrentFluids() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IFluidHandler h = handler.orElse(null);
        if (h == null) return 0;
        int total = 0;
        for (int i = 0; i < h.getTanks(); i++) {
            total += h.getFluidInTank(i).getAmount();
        }
        return total / 1000;
    }

    private int getAboveCurrentFluidsMb() {
        if (level == null) return 0;
        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IFluidHandler h = handler.orElse(null);
        if (h == null) return 0;
        int total = 0;
        for (int i = 0; i < h.getTanks(); i++) {
            total += h.getFluidInTank(i).getAmount();
        }
        return total;
    }

    // ==================== 带过滤器的查询 ====================
    public int getFilteredCurrentItems() {
        if (level == null) return 0;
        ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
        if (filterStack.isEmpty()) return getAboveCurrentItems();

        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IItemHandler h = handler.orElse(null);
        if (h == null) return 0;

        FilterItemStack fis = FilterItemStack.of(filterStack);
        int total = 0;
        for (int i = 0; i < h.getSlots(); i++) {
            ItemStack stack = h.getStackInSlot(i);
            if (!stack.isEmpty() && fis.test(level, stack, false)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public int getFilteredCurrentStacks() {
        if (level == null) return 0;
        ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
        if (filterStack.isEmpty()) return getAboveCurrentStacks();

        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IItemHandler h = handler.orElse(null);
        if (h == null) return 0;

        FilterItemStack fis = FilterItemStack.of(filterStack);
        int occupied = 0;
        for (int i = 0; i < h.getSlots(); i++) {
            ItemStack stack = h.getStackInSlot(i);
            if (!stack.isEmpty() && fis.test(level, stack, false)) {
                occupied++;
            }
        }
        return occupied;
    }

    public int getFilteredCurrentFluids() {
        if (level == null) return 0;
        ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
        if (filterStack.isEmpty()) return getAboveCurrentFluids();

        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IFluidHandler h = handler.orElse(null);
        if (h == null) return 0;

        FilterItemStack fis = FilterItemStack.of(filterStack);
        int total = 0;
        for (int i = 0; i < h.getTanks(); i++) {
            FluidStack fs = h.getFluidInTank(i);
            if (!fs.isEmpty() && fis.test(level, fs, false)) {
                total += fs.getAmount();
            }
        }
        return total / 1000;
    }

    private int getFilteredCurrentFluidsMb() {
        if (level == null) return 0;
        ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
        if (filterStack.isEmpty()) return getAboveCurrentFluidsMb();

        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return 0;
        var handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (handler == null || !handler.isPresent()) return 0;
        IFluidHandler h = handler.orElse(null);
        if (h == null) return 0;

        FilterItemStack fis = FilterItemStack.of(filterStack);
        int total = 0;
        for (int i = 0; i < h.getTanks(); i++) {
            FluidStack fs = h.getFluidInTank(i);
            if (!fs.isEmpty() && fis.test(level, fs, false)) {
                total += fs.getAmount();
            }
        }
        return total;
    }

    // ==================== 产物生成逻辑 ====================
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

    /**
     * 尝试添加到输出系统（输出槽 + producedStack 后备）
     * 输出槽满时自动转入 producedStack 后备存储
     */
    private boolean tryAddToOutput(Item produceItem) {
        // 1. 先尝试放入输出槽
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

        // 2. 输出槽满了或无法堆叠，尝试放入 producedStack 后备存储
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

        // 3. 都满了，无法继续生成
        return false;
    }

    private boolean isValidProducedItem(ItemStack stack) {
        Item produceItem = resolveProduceItem();
        if (produceItem == null) return false;
        return stack.is(produceItem);
    }

    /**
     * 手动取出全部产物（输出槽 + producedStack 后备存储）
     */
    public ItemStack takeAllProduced() {
        // 1. 先取出输出槽
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        ItemStack result = ItemStack.EMPTY;

        if (!outputStack.isEmpty()) {
            result = outputStack.copy();
            itemHandler.setStackInSlot(OUTPUT_SLOT, ItemStack.EMPTY);
        }

        // 2. 再取出 producedStack 后备存储
        if (!producedStack.isEmpty()) {
            if (result.isEmpty()) {
                result = producedStack.copy();
            } else {
                // 合并
                result = ItemHandlerHelper.copyStackWithSize(result, result.getCount() + producedStack.getCount());
            }
            producedStack = ItemStack.EMPTY;
        }

        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    public int getProducedCount() {
        int count = producedStack.getCount();
        count += itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();
        return count;
    }

    // ==================== 当前数量更新（供GUI使用） ====================
    public void updateCurrentAmounts(int amount, int stacks) {
        this.currentAmount = amount;
        this.currentStacks = stacks;
    }

    public int getCurrentAmount() { return currentAmount; }
    public int getCurrentStacks() { return currentStacks; }

    // ==================== Tick 逻辑 ====================
    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        if (keepAmount < 0) return;

        int attachType = getAttachType();

        if (attachType == ATTACH_ITEM) {
            itemTickCounter++;
            if (itemTickCounter >= CLRCoreConfig.SERVER.itemTransferInterval.get()) {
                itemTickCounter = 0;
                tickItemDrain();
            }
        } else if (attachType == ATTACH_FLUID) {
            fluidTickCounter++;
            if (fluidTickCounter >= CLRCoreConfig.SERVER.fluidTransferInterval.get()) {
                fluidTickCounter = 0;
                tickFluidDrain();
            }
        }
    }

    private void tickItemDrain() {
        if (level == null) return;

        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return;

        var handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (handler == null || !handler.isPresent()) return;
        IItemHandler h = handler.orElse(null);
        if (h == null) return;

        ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
        FilterItemStack fis = filterStack.isEmpty() ? null : FilterItemStack.of(filterStack);

        int transferLimit = CLRCoreConfig.SERVER.itemTransferAmount.get();
        int destroyed = 0;

        if (keepInStacks) {
            int occupiedSlots = fis != null ? getFilteredCurrentStacks() : getAboveCurrentStacks();
            int itemsPerStack = Math.max(1, getAboveMaxItems() / Math.max(1, getAboveMaxStacks()));
            int limitStacks = keepAmount / itemsPerStack;

            if (occupiedSlots <= limitStacks) return;

            int slotsStillToRemove = occupiedSlots - limitStacks;

            for (int i = 0; i < h.getSlots() && slotsStillToRemove > 0 && destroyed < transferLimit; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                if (fis != null && !fis.test(level, stack, false)) continue;

                int canTake = Math.min(stack.getCount(), transferLimit - destroyed);
                if (canTake <= 0) break;

                ItemStack extracted = h.extractItem(i, canTake, false);
                if (extracted.getCount() >= stack.getCount()) slotsStillToRemove--;
                destroyed += extracted.getCount();
                accumulateItemFill(extracted.getCount());
                setChanged();
            }
        } else {
            for (int i = 0; i < h.getSlots() && destroyed < transferLimit; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                if (fis != null && !fis.test(level, stack, false)) continue;

                int recalcFiltered = fis != null ? getFilteredCurrentItems() : getAboveCurrentItems();
                if (recalcFiltered <= keepAmount) break;

                int excess = recalcFiltered - keepAmount;
                int canTake = Math.min(Math.min(stack.getCount(), excess), transferLimit - destroyed);
                if (canTake <= 0) break;

                ItemStack extracted = h.extractItem(i, canTake, false);
                destroyed += extracted.getCount();
                accumulateItemFill(extracted.getCount());
                setChanged();
            }
        }
    }

    private void tickFluidDrain() {
        if (level == null) return;

        BlockPos above = worldPosition.above();
        BlockEntity be = level.getBlockEntity(above);
        if (be == null) return;

        var handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
        if (handler == null || !handler.isPresent()) return;
        IFluidHandler h = handler.orElse(null);
        if (h == null) return;

        ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
        FilterItemStack fis = filterStack.isEmpty() ? null : FilterItemStack.of(filterStack);

        int currentMb = fis != null ? getFilteredCurrentFluidsMb() : getAboveCurrentFluidsMb();
        int limitMb = keepAmount * 1000;

        if (currentMb > limitMb) {
            int toDestroy = Math.min(currentMb - limitMb, CLRCoreConfig.SERVER.fluidTransferAmount.get());
            int remaining = toDestroy;

            for (int i = 0; i < h.getTanks() && remaining > 0; i++) {
                FluidStack inTank = h.getFluidInTank(i);
                if (inTank.isEmpty()) continue;
                if (fis != null && !fis.test(level, inTank, false)) continue;

                FluidStack toDrain = new FluidStack(inTank.getFluid(), remaining);
                FluidStack drained = h.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                int drainedAmount = drained.getAmount();
                remaining -= drainedAmount;
                accumulateFluidFill(drainedAmount);
                setChanged();
            }
        }
    }

    // ==================== 重置保留配置 ====================
    public void resetKeepConfig() {
        keepAmount = -1;
        keepInStacks = false;
        setChanged();
    }

    // ==================== 掉落物品 ====================
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

    // ==================== NBT ====================
    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("itemFill", itemFill);
        tag.putInt("fluidFill", fluidFill);
        tag.putInt("keepAmount", keepAmount);
        tag.putBoolean("keepInStacks", keepInStacks);
        tag.putInt("currentAmount", currentAmount);
        tag.putInt("currentStacks", currentStacks);
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
        keepAmount = tag.contains("keepAmount") ? tag.getInt("keepAmount") : -1;
        keepInStacks = tag.getBoolean("keepInStacks");
        currentAmount = tag.getInt("currentAmount");
        currentStacks = tag.getInt("currentStacks");
        if (tag.contains("producedStack")) {
            CompoundTag stackTag = tag.getCompound("producedStack");
            producedStack = ItemStack.of(stackTag);
        } else {
            producedStack = ItemStack.EMPTY;
        }
        if (filtering != null) {
            ItemStack filterStack = itemHandler.getStackInSlot(FILTER_SLOT);
            filtering.setFilter(filterStack);
        }
    }

    // ==================== Capability ====================
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