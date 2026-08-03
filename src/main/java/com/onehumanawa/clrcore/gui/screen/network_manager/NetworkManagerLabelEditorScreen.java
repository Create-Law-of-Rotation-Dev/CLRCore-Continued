package com.onehumanawa.clrcore.gui.screen.network_manager;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.registry.item.network_manager.NetworkLabel;
import com.onehumanawa.clrcore.network.packets.network_manager.SaveNetworkManagerDataPacket;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class NetworkManagerLabelEditorScreen extends AbstractSimiContainerScreen<NetworkManagerLabelEditorMenu> {

    private static final int GUI_WIDTH = AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth();
    private static final int HEADER_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
    private static final int EDIT_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getHeight();
    private static final int FOOTER_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.getHeight();
    private static final int GUI_HEIGHT = HEADER_H + EDIT_H + FOOTER_H + 76 + 14;

    private EditBox nameEditBox;
    private IconButton confirmButton;

    public NetworkManagerLabelEditorScreen(NetworkManagerLabelEditorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        this.setWindowSize(GUI_WIDTH, GUI_HEIGHT);
        super.init();

        int editorTop = this.topPos;

        this.confirmButton = new IconButton(this.leftPos + 167, editorTop + 64, AllIcons.I_CONFIRM);
        this.confirmButton.withCallback(this::onConfirm);
        this.addRenderableWidget(this.confirmButton);

        this.nameEditBox = new EditBox(this.font, this.leftPos + 47, editorTop + 33, 124, 10, Component.empty());
        this.nameEditBox.setTextColor(15658734);
        this.nameEditBox.setBordered(false);
        this.nameEditBox.setFocused(false);
        this.nameEditBox.setMaxLength(28);
        this.nameEditBox.setValue(this.computeDefaultName());
        this.addRenderableWidget(this.nameEditBox);
    }

    private String computeDefaultName() {
        String prefix = Component.translatable("clrcore.gui.network_manager.label_prefix").getString();
        List<Integer> usedNumbers = new ArrayList<>();

        for (NetworkLabel label : ((NetworkManagerLabelEditorMenu) this.menu).existingLabels) {
            if (label.getName().startsWith(prefix)) {
                try {
                    usedNumbers.add(Integer.parseInt(label.getName().substring(prefix.length()).trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        int n = 1;
        while (usedNumbers.contains(n)) {
            n++;
        }
        return prefix + n;
    }

    private void onConfirm() {
        ItemStack icon = ((NetworkManagerLabelEditorMenu) this.menu).ghostInventory.getStackInSlot(0).copy();
        if (!icon.isEmpty()) {
            icon.setCount(1);
        }

        String name = this.nameEditBox.getValue().isBlank() ?
                this.computeDefaultName() :
                this.nameEditBox.getValue();

        List<NetworkLabel> newLabels = new ArrayList<>(((NetworkManagerLabelEditorMenu) this.menu).existingLabels);
        newLabels.add(new NetworkLabel(name, icon, ((NetworkManagerLabelEditorMenu) this.menu).targetNetworkId.orElse(null)));

        CLRCore.CHANNEL.sendToServer(new SaveNetworkManagerDataPacket(
                ((NetworkManagerLabelEditorMenu) this.menu).hand,
                newLabels,
                true
        ));
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);

        int editorTop = this.topPos;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, this.leftPos, editorTop);

        int y = editorTop + HEADER_H;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, this.leftPos, y);

        y += EDIT_H;
        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, this.leftPos, y);

        this.renderPlayerInventory(graphics, this.leftPos + 10, this.topPos + 98);
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean onIconSlot = this.isMouseOverIconSlot(mouseX, mouseY);
        Slot savedSlot = this.hoveredSlot;
        if (onIconSlot) {
            this.hoveredSlot = null;
        }

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        if (onIconSlot) {
            this.hoveredSlot = savedSlot;
        }

        int editorTop = this.topPos;
        FormattedCharSequence titleText = Component.translatable("clrcore.gui.network_manager.add_label_title").getVisualOrderText();
        graphics.drawString(this.font, titleText,
                (int) ((float) this.leftPos + (float) GUI_WIDTH / 2.0F - (float) this.font.width(titleText) / 2.0F),
                editorTop + 4, 4013128, false);

        if (this.nameEditBox != null && this.nameEditBox.isHovered() && !this.nameEditBox.isFocused()) {
            graphics.renderComponentTooltip(this.font,
                    List.of(
                            Component.translatable("create.gui.stock_ticker.category_name").withStyle(ChatFormatting.GOLD),
                            Component.translatable("create.gui.schedule.lmb_edit")
                                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                    ),
                    mouseX, mouseY);
        }
    }

    private boolean isMouseOverIconSlot(int mouseX, int mouseY) {
        int slotScreenX = this.leftPos + 16;
        int slotScreenY = this.topPos + 29;
        return mouseX >= slotScreenX && mouseX < slotScreenX + 16 &&
                mouseY >= slotScreenY && mouseY < slotScreenY + 16;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && this.getFocused() instanceof EditBox) {
            this.onConfirm();
            return true;
        }
        if (this.getFocused() == null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirmButton != null && this.confirmButton.isMouseOver(mouseX, mouseY)) {
            this.onConfirm();
            return true;
        }

        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (this.nameEditBox != null && this.nameEditBox.isMouseOver(mouseX, mouseY) && button == 0) {
            this.nameEditBox.moveCursorToEnd();
            this.nameEditBox.setHighlightPos(0);
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}