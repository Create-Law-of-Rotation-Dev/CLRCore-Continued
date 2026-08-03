package com.onehumanawa.clrcore.gui.screen.labeled_redstone_link;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.ModItems;
import com.onehumanawa.clrcore.gui.widget.FrequencyEditBox;
import com.onehumanawa.clrcore.network.packets.labeled_redstone_link.OpenLabeledRedstoneLinkGuiPacket;
import com.onehumanawa.clrcore.network.packets.labeled_redstone_link.SaveLabeledRedstoneLinkConfigPacket;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BlockItem;

public class LabeledRedstoneLinkScreen extends AbstractSimiScreen {

    private static final int SUGGESTIONS_Y = -40;
    private static final int GUI_WIDTH = AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth();
    private static final int HEADER_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
    private static final int EDIT_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getHeight();
    private static final int FOOTER_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.getHeight();
    private static final int GUI_HEIGHT = HEADER_H + EDIT_H + FOOTER_H;

    private final BlockPos pos;
    private final String initialText;
    private FrequencyEditBox frequencyEditBox;
    private IconButton confirmButton;

    private LabeledRedstoneLinkScreen(BlockPos pos, String initialText) {
        super(CommonComponents.EMPTY);
        this.pos = pos;
        this.initialText = initialText;
    }

    public static void open(OpenLabeledRedstoneLinkGuiPacket packet) {
        Minecraft.getInstance().setScreen(new LabeledRedstoneLinkScreen(packet.getPos(), packet.getFrequencyText()));
    }

    @Override
    protected void init() {
        this.setWindowSize(GUI_WIDTH, GUI_HEIGHT);
        super.init();

        this.confirmButton = new IconButton(this.guiLeft + 167, this.guiTop + 64, AllIcons.I_CONFIRM);
        this.confirmButton.withCallback(this::onConfirm);
        this.addRenderableWidget(this.confirmButton);

        this.frequencyEditBox = new FrequencyEditBox(
                this,
                this.font,
                this.guiLeft + 47,
                this.guiTop + 33,
                124,
                10,
                this.initialText,
                this.guiTop + SUGGESTIONS_Y
        );
        this.frequencyEditBox.setTextColor(15658734);
        this.frequencyEditBox.setBordered(false);
        this.frequencyEditBox.setMaxLength(64);
        this.frequencyEditBox.setValue(this.initialText);
        this.frequencyEditBox.setOnDefocus(this::saveCurrentFrequency);
        this.addRenderableWidget(this.frequencyEditBox);
    }

    private void onConfirm() {
        if (this.frequencyEditBox.isFocused()) {
            this.frequencyEditBox.exitEditMode("默认红石频率");
        }
        this.saveCurrentFrequency();
        this.onClose();
    }

    private void saveCurrentFrequency() {
        String text = this.frequencyEditBox.getValue().isBlank() ? "默认红石频率" : this.frequencyEditBox.getValue();
        CLRCore.CHANNEL.sendToServer(new SaveLabeledRedstoneLinkConfigPacket(this.pos, text));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.frequencyEditBox != null) {
            this.frequencyEditBox.tick();
        }
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int y = this.guiTop;

        AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, this.guiLeft, y);
        y += HEADER_H;

        AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, this.guiLeft, y);
        y += EDIT_H;

        AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, this.guiLeft, y);

        FormattedCharSequence titleText = Component.translatable("clrcore.gui.labeled_redstone_link.title").getVisualOrderText();
        graphics.drawString(
                this.font,
                titleText,
                (int) ((float) this.guiLeft + (float) GUI_WIDTH / 2.0F - (float) this.font.width(titleText) / 2.0F),
                this.guiTop + 4,
                4013128,
                false
        );

        // 渲染方块图标
        graphics.renderItem(
                ((BlockItem) ModItems.LABELED_REDSTONE_LINK.get()).getDefaultInstance(),
                this.guiLeft + 16,
                this.guiTop + 29
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (this.frequencyEditBox.isFocused()) {
                this.frequencyEditBox.exitEditMode("默认红石频率");
            }
            this.onConfirm();
            return true;
        }

        if (this.frequencyEditBox.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == 257 || keyCode == 335) { // ENTER
            this.onConfirm();
            return true;
        }

        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onConfirm();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.confirmButton != null && this.confirmButton.isMouseOver(mouseX, mouseY)) {
            this.onConfirm();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}