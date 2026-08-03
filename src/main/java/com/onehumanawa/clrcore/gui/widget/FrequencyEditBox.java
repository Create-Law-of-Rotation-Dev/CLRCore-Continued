package com.onehumanawa.clrcore.gui.widget;

import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import net.createmod.catnip.data.IntAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FrequencyEditBox extends EditBox {

    private final DestinationSuggestions destinationSuggestions;
    private Runnable onDefocus;
    private static Field previousField;
    private static Field activeField;

    static {
        try {
            previousField = DestinationSuggestions.class.getDeclaredField("previous");
            previousField.setAccessible(true);
            activeField = DestinationSuggestions.class.getDeclaredField("active");
            activeField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // 如果字段名不同，尝试其他名称
            try {
                previousField = DestinationSuggestions.class.getDeclaredField("previousSuggestion");
                previousField.setAccessible(true);
                activeField = DestinationSuggestions.class.getDeclaredField("isActive");
                activeField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                // 忽略，运行时检测
            }
        }
    }

    public FrequencyEditBox(Screen screen, Font font, int x, int y, int w, int h, String localFrequency, int suggestionsYOffset) {
        super(font, x, y, w, h, Component.empty());
        Minecraft mc = Minecraft.getInstance();
        List<IntAttached<String>> options = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();

        if (mc.player != null) {
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                appendFrequencies(options, alreadyAdded, mc.player.getInventory().getItem(i));
            }
        }

        this.destinationSuggestions = new DestinationSuggestions(mc, screen, this, mc.font, options, true, suggestionsYOffset);
        this.destinationSuggestions.setAllowSuggestions(true);
        this.destinationSuggestions.updateCommandInfo();
    }

    private static void appendFrequencies(List<IntAttached<String>> options, Set<String> alreadyAdded, ItemStack stack) {
        if (stack.isEmpty()) return;

        List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(stack);
        if (pages == null) return;

        for (List<ClipboardEntry> page : pages) {
            for (ClipboardEntry entry : page) {
                String string = entry.text.getString();
                if (string.startsWith("@") && string.length() > 1) {
                    String frequency = string.substring(1).trim();
                    if (!frequency.isBlank() && !alreadyAdded.contains(frequency)) {
                        alreadyAdded.add(frequency);
                        options.add(IntAttached.withZero(frequency));
                    }
                }
            }
        }
    }

    public void setOnDefocus(Runnable callback) {
        this.onDefocus = callback;
    }

    private void setPrevious(String value) {
        if (previousField != null) {
            try {
                previousField.set(this.destinationSuggestions, value);
            } catch (IllegalAccessException ignored) {}
        }
    }

    private void setActive(boolean value) {
        if (activeField != null) {
            try {
                activeField.set(this.destinationSuggestions, value);
            } catch (IllegalAccessException ignored) {}
        }
    }

    public void enterEditMode() {
        this.setFocused(true);
        this.setHighlightPos(0);
        this.moveCursorToEnd();
        this.setPrevious("\u0000");
        this.setActive(true);
        this.destinationSuggestions.updateCommandInfo();
    }

    public void exitEditMode(String defaultFrequency) {
        this.moveCursorToEnd();
        this.setSuggestion("");
        if (this.getValue().isBlank()) {
            this.setValue(defaultFrequency);
        }
        this.setFocused(false);
        this.setActive(false);
        this.setPrevious("\u0000");
        this.destinationSuggestions.updateCommandInfo();
        if (this.onDefocus != null) {
            this.onDefocus.run();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) {
            return false;
        }

        if (keyCode == 257 || keyCode == 335 || keyCode == 256) {
            this.exitEditMode(getDefaultFrequency());
            return true;
        }

        if (keyCode == 258) { // TAB
            if (this.destinationSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
                this.exitEditMode(getDefaultFrequency());
                return true;
            }
            return false;
        }

        if (keyCode == 264 || keyCode == 265) { // UP/DOWN
            return this.destinationSuggestions.keyPressed(keyCode, scanCode, modifiers);
        }

        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        if (result) {
            this.setPrevious("\u0000");
            this.destinationSuggestions.updateCommandInfo();
        }
        return result;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (!this.isFocused()) {
            return false;
        }
        boolean result = super.charTyped(c, modifiers);
        if (result) {
            this.setPrevious("\u0000");
            this.destinationSuggestions.updateCommandInfo();
        }
        return result;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (!this.isFocused()) {
            if (this.isMouseOver(mouseX, mouseY)) {
                super.mouseClicked(mouseX, mouseY, button);
                this.enterEditMode();
                return true;
            }
            return false;
        }

        if (this.destinationSuggestions.mouseClicked((int) mouseX, (int) mouseY, button)) {
            this.exitEditMode(getDefaultFrequency());
            return true;
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        this.exitEditMode(getDefaultFrequency());
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.destinationSuggestions.mouseScrolled(Mth.clamp(scrollY, -1.0, 1.0));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        this.destinationSuggestions.render(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    public void tick() {
        this.destinationSuggestions.tick();
    }

    private String getDefaultFrequency() {
        return "默认红石频率";
    }
}