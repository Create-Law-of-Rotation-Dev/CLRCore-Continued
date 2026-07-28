package com.onehumanawa.clrcore.screen;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.network.SaveBrassScrapBucketConfigPacket;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class BrassScrapBucketScreen extends AbstractSimiContainerScreen<BrassScrapBucketMenu> {

    private static final ResourceLocation TEXTURE = CLRCore.rl("textures/gui/brass_scrap_bucket.png");
    private static final int GUI_WIDTH = 182;
    private static final int GUI_TOP_HEIGHT = 79;
    private static final int GUI_HEIGHT = 169;
    private static final int PLAYER_INV_RENDER_X = -1;
    private static final int PLAYER_INV_RENDER_Y = 83;
    private static final int CONFIRM_BUTTON_X = 149;
    private static final int CONFIRM_BUTTON_Y = 55;
    private static final int FILTER_ICON_X = 24;
    private static final int FILTER_ICON_Y = 24;
    private static final int ATTACH_ICON_X = 13;
    private static final int ATTACH_ICON_Y = 56;
    private static final int TITLE_Y = 4;
    private static final int INPUTS_BG_X = 44;
    private static final int INPUTS_BG_Y = 21;
    private static final int VALUE_INPUT_X = 48;
    private static final int VALUE_INPUT_Y = 23;
    private static final int VALUE_INPUT_W = 48;
    private static final int VALUE_INPUT_H = 18;
    private static final int MEASURE_INPUT_X = 100;
    private static final int MEASURE_INPUT_Y = 23;
    private static final int MEASURE_INPUT_W = 52;
    private static final int MEASURE_INPUT_H = 18;
    private static final int VALUE_TEXT_X = 53;
    private static final int VALUE_TEXT_Y = 28;
    private static final int MEASURE_TEXT_X = 105;
    private static final int MEASURE_TEXT_Y = 28;

    private final BlockPos pos;
    private final int attachType;
    private final int maxItems;
    private final int maxStacks;
    private int currentKeepAmount;
    private boolean currentKeepInStacks;
    private int currentAmount;
    private int currentStacks;

    private IconButton confirmButton;
    private ScrollInput valueInput;
    private SelectionScrollInput measureInput;

    public BrassScrapBucketScreen(BrassScrapBucketMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.pos = menu.pos;
        this.attachType = menu.attachType;
        this.currentKeepAmount = menu.keepAmount;
        this.currentKeepInStacks = menu.keepInStacks;
        this.maxItems = menu.maxItems;
        this.maxStacks = menu.maxStacks;
        this.currentAmount = menu.currentAmount;
        this.currentStacks = menu.currentStacks;
    }

    public int getGuiLeft() {
        return this.leftPos;
    }

    public int getGuiTop() {
        return this.topPos;
    }

    public void setFilterIcon(ItemStack stack) {
        ((BrassScrapBucketMenu) this.menu).ghostInventory.setStackInSlot(0, stack.copy());
    }

    public void updateCurrentAmounts(int newAmount, int newStacks) {
        this.currentAmount = newAmount;
        this.currentStacks = newStacks;
    }

    private int getItemsPerStack() {
        return this.maxStacks <= 0 ? 64 : Math.max(1, this.maxItems / this.maxStacks);
    }

    private int getMaxInCurrentUnit() {
        if (this.attachType == 1) {
            return this.currentKeepInStacks ? this.maxStacks : this.maxItems;
        }
        return this.maxItems;
    }

    private int toCurrentUnit(int amountInItems) {
        return this.attachType == 1 && this.currentKeepInStacks ? amountInItems / this.getItemsPerStack() : amountInItems;
    }

    private int toItems(int amountInCurrentUnit) {
        if (amountInCurrentUnit < 0) {
            return -1;
        }
        return this.attachType == 1 && this.currentKeepInStacks ? amountInCurrentUnit * this.getItemsPerStack() : amountInCurrentUnit;
    }

    private String getUnitString() {
        if (this.attachType == 2) {
            return Component.translatable("create.schedule.condition.threshold.buckets").getString();
        }
        return this.currentKeepInStacks ?
                Component.translatable("create.schedule.condition.threshold.stacks").getString() :
                Component.translatable("create.schedule.condition.threshold.items").getString();
    }

    @Override
    protected void init() {
        this.setWindowSize(GUI_WIDTH, GUI_HEIGHT);
        super.init();

        if (this.attachType == 1) {
            // 物品模式：显示单位切换
            this.measureInput = (SelectionScrollInput) new SelectionScrollInput(
                    this.leftPos + MEASURE_INPUT_X,
                    this.topPos + MEASURE_INPUT_Y,
                    MEASURE_INPUT_W,
                    MEASURE_INPUT_H
            )
                    .forOptions(List.of(
                            Component.translatable("create.schedule.condition.threshold.items"),
                            Component.translatable("create.schedule.condition.threshold.stacks")
                    ))
                    .titled(Component.translatable("create.schedule.condition.threshold.item_measure"))
                    .setState(this.currentKeepInStacks ? 1 : 0)
                    .calling(this::onMeasureChanged);

            boolean disabled = this.currentKeepAmount < 0;
            this.measureInput.active = !disabled;
            this.measureInput.visible = !disabled;
            this.addRenderableWidget(this.measureInput);

            int initMax = this.getMaxInCurrentUnit();
            int initValue = this.currentKeepAmount < 0 ? -1 : this.toCurrentUnit(this.currentKeepAmount);

            this.valueInput = new ScrollInput(
                    this.leftPos + VALUE_INPUT_X,
                    this.topPos + VALUE_INPUT_Y,
                    VALUE_INPUT_W,
                    VALUE_INPUT_H
            )
                    .withRange(-1, initMax + 1)
                    .titled(Component.translatable("create.gui.threshold_switch.upper_threshold"))
                    .calling(this::onValueChanged)
                    .withStepFunction((ctx) -> ctx.shift ? 10 : 1)
                    .setState(Math.max(-1, Math.min(initValue, initMax)));
            this.addRenderableWidget(this.valueInput);

        } else if (this.attachType == 2) {
            // 流体模式：只有数值输入
            int initValue = Math.max(-1, Math.min(this.currentKeepAmount, this.maxItems));
            this.valueInput = new ScrollInput(
                    this.leftPos + VALUE_INPUT_X,
                    this.topPos + VALUE_INPUT_Y,
                    VALUE_INPUT_W + MEASURE_INPUT_W,
                    VALUE_INPUT_H
            )
                    .withRange(-1, this.maxItems + 1)
                    .titled(Component.translatable("create.gui.threshold_switch.upper_threshold"))
                    .calling((val) -> this.currentKeepAmount = val)
                    .withStepFunction((ctx) -> ctx.shift ? 10 : 1)
                    .setState(initValue);
            this.addRenderableWidget(this.valueInput);
        }

        // 确认按钮
        this.confirmButton = new IconButton(
                this.leftPos + CONFIRM_BUTTON_X,
                this.topPos + CONFIRM_BUTTON_Y,
                18, 18,
                AllIcons.I_CONFIRM
        );
        this.confirmButton.withCallback(this::saveAndClose);
        this.addRenderableWidget(this.confirmButton);
    }

    private void onValueChanged(int val) {
        this.currentKeepAmount = this.toItems(val);
        if (this.measureInput != null) {
            boolean disabled = val < 0;
            this.measureInput.active = !disabled;
            this.measureInput.visible = !disabled;
        }
    }

    private void onMeasureChanged(int state) {
        if (this.valueInput != null) {
            boolean wasInStacks = this.currentKeepInStacks;
            this.currentKeepInStacks = state == 1;
            int oldDisplayValue = this.valueInput.getState();
            if (oldDisplayValue >= 0) {
                int amountInItems = wasInStacks ? oldDisplayValue * this.getItemsPerStack() : oldDisplayValue;
                int newDisplayValue = this.currentKeepInStacks ? amountInItems / this.getItemsPerStack() : amountInItems;
                int newMax = this.getMaxInCurrentUnit();
                newDisplayValue = Math.min(newDisplayValue, newMax);
                this.valueInput.withRange(-1, newMax + 1);
                this.valueInput.setState(newDisplayValue);
                this.currentKeepAmount = this.toItems(newDisplayValue);
            }
        }
    }

    private boolean isMouseOverFilterIconSlot(double mouseX, double mouseY) {
        int slotScreenX = this.leftPos + FILTER_ICON_X;
        int slotScreenY = this.topPos + FILTER_ICON_Y;
        return mouseX >= slotScreenX && mouseX < slotScreenX + 16 &&
                mouseY >= slotScreenY && mouseY < slotScreenY + 16;
    }

    private void saveAndClose() {
        CLRCore.CHANNEL.sendToServer(
                new SaveBrassScrapBucketConfigPacket(this.pos, this.currentKeepAmount, this.currentKeepInStacks)
        );
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 渲染主界面 (182x79)
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, GUI_WIDTH, GUI_TOP_HEIGHT, 256, 256);

        // 渲染玩家背包
        this.renderPlayerInventory(graphics, this.leftPos + PLAYER_INV_RENDER_X, this.topPos + PLAYER_INV_RENDER_Y);
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean onFilterSlot = this.isMouseOverFilterIconSlot(mouseX, mouseY);
        Slot savedSlot = this.hoveredSlot;
        if (onFilterSlot) {
            this.hoveredSlot = null;
        }

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        if (onFilterSlot) {
            this.hoveredSlot = savedSlot;
        }

        // 渲染幽灵槽物品
        ItemStack filterIcon = ((BrassScrapBucketMenu) this.menu).ghostInventory.getStackInSlot(0);
        if (!filterIcon.isEmpty()) {
            graphics.renderItem(filterIcon, this.leftPos + FILTER_ICON_X, this.topPos + FILTER_ICON_Y);
        }

        // 标题
        Component titleComp = Component.translatable("block.clrcore.brass_scrap_bucket");
        int titleX = this.leftPos + 91 - this.font.width(titleComp) / 2;
        graphics.drawString(this.font, titleComp, titleX, this.topPos + TITLE_Y, 5841956, false);

        // 输入背景
        if (this.attachType == 1) {
            AllGuiTextures.THRESHOLD_SWITCH_ITEMCOUNT_INPUTS.render(graphics, this.leftPos + INPUTS_BG_X, this.topPos + INPUTS_BG_Y);
        } else {
            AllGuiTextures.THRESHOLD_SWITCH_MISC_INPUTS.render(graphics, this.leftPos + INPUTS_BG_X, this.topPos + INPUTS_BG_Y);
        }

        // 上方容器图标
        ItemStack displayItem = this.attachType == 0 ? new ItemStack(Items.BARRIER) : this.getAboveBlockItem();
        graphics.renderItem(displayItem, this.leftPos + ATTACH_ICON_X, this.topPos + ATTACH_ICON_Y);

        // 显示当前数值
        if (this.attachType != 0 && this.valueInput != null) {
            int displayValue = this.valueInput.getState();
            String leftText;
            if (displayValue < 0) {
                leftText = Component.translatable("clrcore.gui.brass_scrap_bucket.disabled").getString();
            } else if (this.attachType == 2) {
                leftText = displayValue + " " + Component.translatable("create.schedule.condition.threshold.buckets").getString();
            } else {
                leftText = String.valueOf(displayValue);
            }
            graphics.drawString(this.font, Component.literal(leftText), this.leftPos + VALUE_TEXT_X, this.topPos + VALUE_TEXT_Y, 16777215, true);
        }

        if (this.attachType == 0) {
            graphics.drawString(this.font,
                    Component.translatable("clrcore.gui.brass_scrap_bucket.disabled"),
                    this.leftPos + VALUE_TEXT_X, this.topPos + VALUE_TEXT_Y, 16777215, true);
        }

        // 显示单位
        if (this.attachType == 1 && this.measureInput != null && this.measureInput.visible &&
                this.valueInput != null && this.valueInput.getState() >= 0) {
            String measureText = this.measureInput.getState() == 1 ?
                    Component.translatable("create.schedule.condition.threshold.stacks").getString() :
                    Component.translatable("create.schedule.condition.threshold.items").getString();
            graphics.drawString(this.font, Component.literal(measureText), this.leftPos + MEASURE_TEXT_X, this.topPos + MEASURE_TEXT_Y, 16777215, true);
        }

        // 悬停提示
        if (this.attachType != 0) {
            int iconScreenX = this.leftPos + ATTACH_ICON_X;
            int iconScreenY = this.topPos + ATTACH_ICON_Y;
            if (mouseX >= iconScreenX && mouseX < iconScreenX + 16 && mouseY >= iconScreenY && mouseY < iconScreenY + 16) {
                this.renderAttachTooltip(graphics, mouseX, mouseY);
            }
        }

        if (onFilterSlot) {
            this.renderFilterIconTooltip(graphics, mouseX, mouseY);
        }
    }

    private void renderFilterIconTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack filterIcon = ((BrassScrapBucketMenu) this.menu).ghostInventory.getStackInSlot(0);
        if (filterIcon.isEmpty()) {
            graphics.renderComponentTooltip(this.font,
                    List.of(Component.translatable("clrcore.gui.brass_scrap_bucket.filter_icon.empty").withStyle(ChatFormatting.GRAY)),
                    mouseX, mouseY);
        } else {
            List<Component> lines = new ArrayList<>();
            lines.add(filterIcon.getHoverName());
            lines.add(Component.translatable("clrcore.gui.brass_scrap_bucket.filter_icon.click_to_clear")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    private void renderAttachTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(this.getAboveBlockItem().getHoverName());
        String unit = this.getUnitString();
        int displayCurrent = this.attachType == 1 && this.currentKeepInStacks ? this.currentStacks : this.currentAmount;
        lines.add(Component.translatable("clrcore.gui.brass_scrap_bucket.tooltip.current",
                displayCurrent + " " + unit).withStyle(ChatFormatting.GREEN));
        int maxInUnit = this.getMaxInCurrentUnit();
        lines.add(Component.translatable("clrcore.gui.brass_scrap_bucket.tooltip.max",
                maxInUnit + " " + unit).withStyle(ChatFormatting.DARK_GRAY));
        graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
    }

    private ItemStack getAboveBlockItem() {
        if (this.minecraft != null && this.minecraft.level != null) {
            BlockPos above = this.pos.above();
            BlockState state = this.minecraft.level.getBlockState(above);
            return state.isAir() ? new ItemStack(Items.BARRIER) :
                    state.getBlock().getCloneItemStack(this.minecraft.level, above, state);
        }
        return new ItemStack(Items.BARRIER);
    }
}