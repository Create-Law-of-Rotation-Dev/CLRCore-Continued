package com.onehumanawa.clrcore.gui.screen.network_manager;

import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.registry.item.network_manager.NetworkLabel;
import com.onehumanawa.clrcore.registry.item.network_manager.NetworkManagerItem;
import com.onehumanawa.clrcore.registry.item.network_manager.NetworkSelectedState;
import com.onehumanawa.clrcore.network.packets.network_manager.ClearNetworkSelectionPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.SaveNetworkManagerDataPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.SaveNetworkManagerSearchPacket;
import com.onehumanawa.clrcore.network.packets.network_manager.SetNetworkSelectionPacket;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;

import java.util.ArrayList;
import java.util.List;

public class NetworkManagerScreen extends AbstractSimiScreen {

    private static final ResourceLocation TEXTURE = CLRCore.rl("textures/gui/network_manager_categories.png");
    private static final int TEXTURE_W = 256;
    private static final int TEXTURE_H = 256;
    private static final int HEADER_SRC_X = 32;
    private static final int HEADER_SRC_Y = 0;
    private static final int BODY_SRC_X = 32;
    private static final int BODY_SRC_Y = 32;
    private static final int FOOTER_SRC_X = 32;
    private static final int FOOTER_SRC_Y = 79;
    private static final int ENTRY_SRC_X = 38;
    private static final int ENTRY_SRC_Y = 159;
    private static final int UP_SRC_X = 211;
    private static final int UP_SRC_Y = 160;
    private static final int DOWN_SRC_X = 211;
    private static final int DOWN_SRC_Y = 169;

    private static final int GUI_WIDTH = AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth();
    private static final int HEADER_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
    private static final int BODY_H = AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight();
    private static final int FOOTER_H = AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.getHeight();
    private static final int GUI_HEIGHT = HEADER_H + BODY_H * 4 + FOOTER_H;
    private static final int SCISSOR_Y2 = 19 + BODY_H * 4;
    private static final int SEARCH_Y = GUI_HEIGHT - FOOTER_H + 13;

    private final InteractionHand hand;
    private final List<NetworkLabel> labels;
    private final LerpedFloat scroll;
    private IconButton confirmButton;
    private EditBox searchBox;
    private String lastSyncedSearch = null;

    public NetworkManagerScreen(InteractionHand hand, List<NetworkLabel> labels) {
        super(Component.translatable("item.clrcore.network_manager"));
        this.hand = hand;
        this.labels = new ArrayList<>(labels);
        this.scroll = LerpedFloat.linear().startWithValue(0.0F);
    }

    public static void open(InteractionHand hand, List<NetworkLabel> labels) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack stack = mc.player.getItemInHand(hand);
            NetworkSelectedState state = NetworkSelectedState.fromItemStack(stack);
            if (state != null) {
                CLRCore.CHANNEL.sendToServer(new ClearNetworkSelectionPacket(hand));
            }
        }
        ScreenOpener.open(new NetworkManagerScreen(hand, labels));
    }

    @Override
    protected void init() {
        this.setWindowSize(GUI_WIDTH, GUI_HEIGHT);
        super.init();

        this.confirmButton = new IconButton(this.guiLeft + GUI_WIDTH - 25, this.guiTop + GUI_HEIGHT - 25, 18, 18, AllIcons.I_CONFIRM);
        this.confirmButton.withCallback(this::onClose);
        this.addRenderableWidget(this.confirmButton);

        this.searchBox = new EditBox(this.font, this.guiLeft + 15, this.guiTop + SEARCH_Y, 130, 10, Component.empty());
        this.searchBox.setTextColor(15658734);
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(64);
        this.searchBox.setFocused(false);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack stack = mc.player.getItemInHand(this.hand);
            String saved = NetworkManagerItem.getSearch(stack);
            if (!saved.isEmpty()) {
                this.searchBox.setValue(saved);
            }
        }

        this.lastSyncedSearch = this.searchBox.getValue();
        this.addRenderableWidget(this.searchBox);
    }

    private List<NetworkLabel> getFilteredLabels() {
        String query = this.searchBox != null ? this.searchBox.getValue().trim() : "";
        if (query.isEmpty()) {
            return this.labels;
        }
        List<NetworkLabel> result = new ArrayList<>();
        for (NetworkLabel label : this.labels) {
            if (label.getName().contains(query)) {
                result.add(label);
            }
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        this.scroll.tickChaser();
    }

    private int getMaxScroll() {
        return Math.max(0, this.getFilteredLabels().size() * 20 + 5 - BODY_H * 4);
    }

    private void clampScroll() {
        float clamped = Mth.clamp(this.scroll.getChaseTarget(), 0.0F, (float) this.getMaxScroll());
        this.scroll.chase(clamped, 0.4, Chaser.EXP);
    }

    private void syncSearchToServer() {
        String current = this.searchBox.getValue();
        if (!current.equals(this.lastSyncedSearch)) {
            this.lastSyncedSearch = current;
            CLRCore.CHANNEL.sendToServer(new SaveNetworkManagerSearchPacket(this.hand, current));
        }
    }

    @Override
    public void onClose() {
        this.syncSearchToServer();
        super.onClose();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.getMaxScroll() > 0) {
            float newTarget = this.scroll.getChaseTarget() - (float) scrollY * 20.0F;
            newTarget = Mth.clamp(newTarget, 0.0F, (float) this.getMaxScroll());
            this.scroll.chase(newTarget, 0.4, Chaser.EXP);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX);
    }

    private void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void syncToServer() {
        CLRCore.CHANNEL.sendToServer(new SaveNetworkManagerDataPacket(this.hand, this.labels, false));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (this.searchBox != null && this.searchBox.isFocused()) {
                this.searchBox.setFocused(false);
                this.setFocused(null);
                return true;
            }
            this.onClose();
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && this.searchBox != null && this.searchBox.isFocused()) {
            this.searchBox.setFocused(false);
            this.setFocused(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.searchBox != null && this.searchBox.isMouseOver(mouseX, mouseY)) {
            if (!this.searchBox.isFocused()) {
                this.searchBox.setFocused(true);
                this.setFocused(this.searchBox);
                if (!this.searchBox.getValue().isEmpty()) {
                    this.searchBox.setHighlightPos(0);
                    this.searchBox.moveCursorToEnd();
                }
                return true;
            }
        } else if (this.searchBox != null && this.searchBox.isFocused()) {
            this.searchBox.setFocused(false);
            this.setFocused(null);
        }

        if (button == 0 || button == 1) {
            float scrollOff = this.scroll.getValue(1.0F);
            int relX = (int) mouseX - this.guiLeft - 7;
            int relYBase = (int) mouseY - this.guiTop - 24 + (int) scrollOff;
            List<NetworkLabel> filtered = this.getFilteredLabels();

            for (int i = 0; i < filtered.size(); i++) {
                int entryRelY = relYBase - i * 20;
                if (entryRelY > 0 && entryRelY <= 20) {
                    int entryScreenY = this.guiTop + 24 + i * 20 - (int) this.scroll.getValue(1.0F);
                    if (entryScreenY + 20 > this.guiTop + 16 && entryScreenY < this.guiTop + SCISSOR_Y2) {
                        NetworkLabel label = filtered.get(i);
                        int realIndex = this.labels.indexOf(label);
                        boolean isFiltering = !this.searchBox.getValue().trim().isEmpty();

                        if (button == 0) {
                            if (relX > 153 && relX <= 169) {
                                this.labels.remove(realIndex);
                                this.clampScroll();
                                this.syncToServer();
                                this.playClickSound();
                                return true;
                            }
                            if (!isFiltering && relX >= 172 && relX < 180 && entryRelY > 2 && entryRelY <= 10 && realIndex > 0) {
                                NetworkLabel entry = this.labels.remove(realIndex);
                                this.labels.add(hasShiftDown() ? 0 : realIndex - 1, entry);
                                this.syncToServer();
                                this.playClickSound();
                                return true;
                            }
                            if (!isFiltering && relX >= 172 && relX < 180 && entryRelY > 10 && entryRelY <= 18 && realIndex < this.labels.size() - 1) {
                                NetworkLabel entry = this.labels.remove(realIndex);
                                this.labels.add(hasShiftDown() ? this.labels.size() : realIndex + 1, entry);
                                this.syncToServer();
                                this.playClickSound();
                                return true;
                            }
                            if (relX > 0 && relX <= 153 && label.getNetworkId() != null) {
                                CLRCore.CHANNEL.sendToServer(new SetNetworkSelectionPacket(this.hand, label.getName(), label.getNetworkId()));
                                this.playClickSound();
                                this.onClose();
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int y = this.guiTop;

        graphics.blit(TEXTURE, this.guiLeft, y, HEADER_SRC_X, HEADER_SRC_Y, GUI_WIDTH, HEADER_H, TEXTURE_W, TEXTURE_H);
        y += HEADER_H;

        for (int i = 0; i < 4; i++) {
            graphics.blit(TEXTURE, this.guiLeft, y, BODY_SRC_X, BODY_SRC_Y, GUI_WIDTH, BODY_H, TEXTURE_W, TEXTURE_H);
            y += BODY_H;
        }

        graphics.blit(TEXTURE, this.guiLeft, y, FOOTER_SRC_X, FOOTER_SRC_Y, GUI_WIDTH, FOOTER_H, TEXTURE_W, TEXTURE_H);

        if (this.searchBox != null && this.searchBox.getValue().isEmpty() && !this.searchBox.isFocused()) {
            graphics.drawString(this.font, Component.translatable("clrcore.gui.network_manager.search_hint"),
                    this.guiLeft + 15, this.guiTop + SEARCH_Y, -867941308, false);
        }

        Component title = Component.translatable("item.clrcore.network_manager");
        int titleX = this.guiLeft + GUI_WIDTH / 2 - this.font.width(title) / 2;
        int titleY = this.guiTop + (HEADER_H - 9) / 2;
        graphics.drawString(this.font, title, titleX, titleY, 16777215, false);

        graphics.enableScissor(this.guiLeft + 3, this.guiTop + 16, this.guiLeft + 187, this.guiTop + SCISSOR_Y2);

        float scrollOff = this.scroll.getValue(partialTicks);
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, -scrollOff, 0);

        List<NetworkLabel> filtered = this.getFilteredLabels();
        boolean isFiltering = !this.searchBox.getValue().trim().isEmpty();

        for (int i = 0; i < filtered.size(); i++) {
            NetworkLabel label = filtered.get(i);
            int realIndex = this.labels.indexOf(label);
            int entryX = this.guiLeft + 7;
            int entryY = this.guiTop + 24 + i * 20;

            graphics.blit(TEXTURE, entryX, entryY, ENTRY_SRC_X, ENTRY_SRC_Y, GUI_WIDTH - 14, 20, TEXTURE_W, TEXTURE_H);

            if (!isFiltering && realIndex > 0) {
                graphics.blit(TEXTURE, entryX + 172, entryY + 2, UP_SRC_X, UP_SRC_Y, 8, 8, TEXTURE_W, TEXTURE_H);
            }
            if (!isFiltering && realIndex < this.labels.size() - 1) {
                graphics.blit(TEXTURE, entryX + 172, entryY + 10, DOWN_SRC_X, DOWN_SRC_Y, 8, 8, TEXTURE_W, TEXTURE_H);
            }

            ItemStack icon = label.getIcon().isEmpty() ? new ItemStack(Items.BARRIER) : label.getIcon();
            graphics.renderItem(icon, entryX + 14, entryY + 1);

            String text = label.getName();
            if (text.length() > 20) {
                text = text.substring(0, 20) + "...";
            }
            graphics.drawString(this.font, Component.literal(text), entryX + 35, entryY + 5, 6645093, false);
        }

        pose.popPose();
        graphics.disableScissor();

        float scrollOff2 = this.scroll.getValue(partialTicks);
        int relX = mouseX - this.guiLeft - 7;
        int relYBase = mouseY - this.guiTop - 24 + (int) scrollOff2;

        for (int i = 0; i < filtered.size(); i++) {
            int entryRelY = relYBase - i * 20;
            if (entryRelY > 0 && entryRelY <= 20) {
                int entryScreenY = this.guiTop + 24 + i * 20 - (int) scrollOff2;
                if (entryScreenY + 20 > this.guiTop + 16 && entryScreenY < this.guiTop + SCISSOR_Y2) {
                    NetworkLabel label = filtered.get(i);
                    int realIndex = this.labels.indexOf(label);
                    isFiltering = !this.searchBox.getValue().trim().isEmpty();

                    if (relX > 153 && relX <= 169) {
                        graphics.renderComponentTooltip(this.font,
                                List.of(Component.translatable("create.gui.stock_ticker.delete_category")),
                                mouseX, mouseY);
                        break;
                    }
                    if (!isFiltering && relX >= 172 && relX < 180 && entryRelY > 2 && entryRelY <= 10 && realIndex > 0) {
                        graphics.renderComponentTooltip(this.font,
                                List.of(CreateLang.translateDirect("gui.schedule.move_up"),
                                        CreateLang.translateDirect("gui.stock_ticker.shift_moves_top")
                                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)),
                                mouseX, mouseY);
                        break;
                    }
                    if (!isFiltering && relX >= 172 && relX < 180 && entryRelY > 10 && entryRelY <= 18 && realIndex < this.labels.size() - 1) {
                        graphics.renderComponentTooltip(this.font,
                                List.of(CreateLang.translateDirect("gui.schedule.move_down"),
                                        CreateLang.translateDirect("gui.stock_ticker.shift_moves_bottom")
                                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)),
                                mouseX, mouseY);
                        break;
                    }
                    if (relX > 0 && relX <= 153) {
                        List<Component> tooltip = new ArrayList<>();
                        tooltip.add(Component.literal(label.getName()).withStyle(ChatFormatting.WHITE));
                        if (label.getNetworkId() != null) {
                            tooltip.add(Component.translatable("clrcore.gui.network_manager.lmb_select")
                                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                        }
                        tooltip.add(Component.translatable("clrcore.gui.network_manager.rmb_edit")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                        graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}