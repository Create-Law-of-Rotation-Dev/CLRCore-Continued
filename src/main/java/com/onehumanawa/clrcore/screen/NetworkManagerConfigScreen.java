package com.onehumanawa.clrcore.screen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.onehumanawa.clrcore.CLRCore;
import com.onehumanawa.clrcore.network.ApplyNetworkPacket;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.UIRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class NetworkManagerConfigScreen extends AbstractSimiScreen {

    private static final AllIcons ICON_SINGLE = AllIcons.I_CONFIRM;
    private static final AllIcons ICON_NETWORK = AllIcons.I_PRIORITY_VERY_HIGH;
    private static final int MILESTONE_SIZE = 8;
    private static final int MAX_VALUE = 1;
    private static final int ROW_HEIGHT = 11;

    private final InteractionHand hand;
    private final BlockPos targetPos;
    private final Vec3 clickLocation;
    private int ticksOpen = 0;
    private int currentValue = 0;
    private int soundCoolDown = 0;
    private int valueBarWidth;
    private int maxLabelWidth;

    public NetworkManagerConfigScreen(InteractionHand hand, BlockPos targetPos, Vec3 clickLocation) {
        this.hand = hand;
        this.targetPos = targetPos;
        this.clickLocation = clickLocation;
    }

    public static void open(InteractionHand hand, BlockPos targetPos, Vec3 clickLocation) {
        ScreenOpener.open(new NetworkManagerConfigScreen(hand, targetPos, clickLocation));
    }

    @Override
    protected void init() {
        this.maxLabelWidth = -18;
        int milestoneCount = 2;
        int scale = 2;
        this.valueBarWidth = 2 * scale + 1 + milestoneCount * 8;
        int w = this.maxLabelWidth + 14 + this.valueBarWidth + 10;
        int h = 11;
        this.setWindowSize(w, h);
        super.init();

        Vec2 initCoord = this.getCoordinateOfValue(0);
        this.setCursor(initCoord);
    }

    private void setCursor(Vec2 coord) {
        Window window = this.minecraft.getWindow();
        double guiScale = window.getGuiScale();
        GLFW.glfwSetCursorPos(window.getWindow(), coord.x * guiScale, coord.y * guiScale);
    }

    private Vec2 getCoordinateOfValue(int value) {
        int scale = 2;
        float xOut = this.guiLeft + (Math.max(1, value) - 1) / 1 * 8 + value * scale + 1.5F;
        xOut += this.maxLabelWidth + 14 + 4;
        if (value % 1 == 0) {
            xOut += 4.0F;
        }
        if (value > 0) {
            xOut += 8.0F;
        }
        float yOut = this.guiTop + 5.5F - 0.5F;
        return new Vec2(xOut, yOut);
    }

    private int getClosestValue(int mouseX, int mouseY) {
        double bestDiff = Double.MAX_VALUE;
        int best = 0;
        for (int v = 0; v <= MAX_VALUE; v++) {
            Vec2 coord = this.getCoordinateOfValue(v);
            double diff = Math.abs(coord.x - (float) mouseX);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = v;
            }
        }
        return best;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = this.guiLeft;
        int y = this.guiTop;

        Component title = Component.translatable("clrcore.gui.network_manager.config_network");
        Component tip = Component.translatable("clrcore.gui.network_manager.config_release");
        double fadeIn = Math.pow(Mth.clamp((this.ticksOpen + partialTicks) / 4.0F, 0.0, 1.0), 1.0);

        Component optSingle = Component.translatable("clrcore.gui.network_manager.config_single");
        Component optNetwork = Component.translatable("clrcore.gui.network_manager.config_whole_network");

        int fattestLabel = Math.max(this.font.width(tip), this.font.width(title));
        fattestLabel = Math.max(fattestLabel, this.font.width(optSingle));
        fattestLabel = Math.max(fattestLabel, this.font.width(optNetwork));

        int fatTipOffset = Math.max(0, fattestLabel + 10 - (this.windowWidth + 13)) / 2;
        int bgWidth = Math.max(this.windowWidth + 13, fattestLabel + 10);
        int fadeInWidth = (int) (bgWidth * fadeIn);
        int fadeInStart = (bgWidth - fadeInWidth) / 2 - fatTipOffset;
        int additionalHeight = 46;
        int zLevel = 0;

        UIRenderHelper.drawStretched(graphics, x - 11 + fadeInStart, y - 17, fadeInWidth, this.windowHeight + additionalHeight, zLevel, AllGuiTextures.VALUE_SETTINGS_OUTER_BG);
        UIRenderHelper.drawStretched(graphics, x - 10 + fadeInStart, y - 18, fadeInWidth - 2, 1, zLevel, AllGuiTextures.VALUE_SETTINGS_OUTER_BG);
        UIRenderHelper.drawStretched(graphics, x - 10 + fadeInStart, y - 17 + this.windowHeight + additionalHeight, zLevel, fadeInWidth - 2, 1, AllGuiTextures.VALUE_SETTINGS_OUTER_BG);

        if (fadeInWidth > fattestLabel) {
            int textX = x - 11 - fatTipOffset + bgWidth / 2;
            graphics.drawString(this.font, title, textX - this.font.width(title) / 2, y - 14, 14540253, false);
            graphics.drawString(this.font, tip, textX - this.font.width(tip) / 2, y + this.windowHeight + additionalHeight - 27, 14540253, false);
        }

        this.renderBrassFrame(graphics, x + this.maxLabelWidth + 14, y - 3, this.valueBarWidth + 8, 16);
        UIRenderHelper.drawStretched(graphics, x + this.maxLabelWidth + 17, y, this.valueBarWidth + 2, 10, zLevel, AllGuiTextures.VALUE_SETTINGS_BAR_BG);

        int valueBarX = x + this.maxLabelWidth + 14 + 4;
        int milestoneCount = 2;
        int scale = 2;
        int milestoneX = valueBarX;
        for (int milestone = 0; milestone < milestoneCount; milestone++) {
            AllGuiTextures.VALUE_SETTINGS_WIDE_MILESTONE.render(graphics, milestoneX, y + 1);
            milestoneX += 8 + 1 * scale;
        }

        if (this.ticksOpen >= 1) {
            int closest = this.getClosestValue(mouseX, mouseY);
            if (closest != this.currentValue) {
                this.currentValue = closest;
                if (this.soundCoolDown == 0) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(),
                            Mth.lerp((float) closest / MAX_VALUE, 1.15F, 1.5F), 0.25F));
                    this.soundCoolDown = 1;
                }
            }

            Vec2 coordinate = this.getCoordinateOfValue(closest);
            AllIcons cursorIcon = closest == 0 ? ICON_SINGLE : ICON_NETWORK;
            Component cursorText = closest == 0 ? optSingle : optNetwork;
            int cursorWidth = 19;
            int cursorX = (int) coordinate.x - cursorWidth / 2;
            int cursorY = (int) coordinate.y - 7;

            AllGuiTextures.VALUE_SETTINGS_CURSOR_ICON.render(graphics, cursorX - 2, cursorY - 3);
            RenderSystem.setShaderColor(0.265625F, 0.125F, 0.0F, 1.0F);
            cursorIcon.render(graphics, cursorX + 1, cursorY - 1);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (fadeInWidth > fattestLabel) {
                int textX = x - 11 - fatTipOffset + bgWidth / 2;
                graphics.drawString(this.font, cursorText, textX - this.font.width(cursorText) / 2,
                        y + this.windowHeight + additionalHeight - 40, 16495700, false);
            }
        }
    }

    private void renderBrassFrame(GuiGraphics graphics, int x, int y, int w, int h) {
        AllGuiTextures.BRASS_FRAME_TL.render(graphics, x, y);
        AllGuiTextures.BRASS_FRAME_TR.render(graphics, x + w - 4, y);
        AllGuiTextures.BRASS_FRAME_BL.render(graphics, x, y + h - 4);
        AllGuiTextures.BRASS_FRAME_BR.render(graphics, x + w - 4, y + h - 4);
        int zLevel = 0;
        if (h > 8) {
            UIRenderHelper.drawStretched(graphics, x, y + 4, 3, h - 8, zLevel, AllGuiTextures.BRASS_FRAME_LEFT);
            UIRenderHelper.drawStretched(graphics, x + w - 3, y + 4, 3, h - 8, zLevel, AllGuiTextures.BRASS_FRAME_RIGHT);
        }
        if (w > 8) {
            UIRenderHelper.drawCropped(graphics, x + 4, y, w - 8, 3, zLevel, AllGuiTextures.BRASS_FRAME_TOP);
            UIRenderHelper.drawCropped(graphics, x + 4, y + h - 3, w - 8, 3, zLevel, AllGuiTextures.BRASS_FRAME_BOTTOM);
        }
    }

    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int a = (int) (80.0F * Math.min(1.0F, (this.ticksOpen + AnimationTickHolder.getPartialTicks()) / 20.0F)) << 24;
        graphics.fillGradient(0, 0, this.width, this.height, 1052688 | a, 1052688 | a);
    }

    @Override
    public void tick() {
        this.ticksOpen++;
        if (this.soundCoolDown > 0) {
            this.soundCoolDown--;
        }
        super.tick();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int next = Mth.clamp(this.currentValue - (int) Math.signum(scrollY), 0, MAX_VALUE);
        if (next == this.currentValue) {
            return false;
        }
        this.setCursor(this.getCoordinateOfValue(next));
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft.options.keyUse.matches(keyCode, scanCode)) {
            Window window = this.minecraft.getWindow();
            double mx = this.minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
            double my = this.minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
            this.saveAndClose((int) mx, (int) my);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) {
            this.saveAndClose((int) mouseX, (int) mouseY);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void saveAndClose(int mouseX, int mouseY) {
        int chosen = this.getClosestValue(mouseX, mouseY);
        boolean wholeNetwork = chosen == 1;
        CLRCore.CHANNEL.sendToServer(new ApplyNetworkPacket(this.hand, this.targetPos, this.clickLocation, wholeNetwork));
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}