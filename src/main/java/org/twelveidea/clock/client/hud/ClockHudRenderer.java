/*
 * Twelve Idea Clock HUD
 * Copyright (C) 2026 Twelve Idea
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.twelveidea.clock.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.twelveidea.clock.Config;

/**
 * Renders the clock bar HUD element.
 * Ported from ClockHUD's GuiClock (MIT, Copyright (c) 2017 Sam Beckmann).
 */
public class ClockHudRenderer {
    // Runtime toggle for the whole clock HUD (not persisted in the config).
    public static volatile boolean guiActive = true;

    private static final float MIN_SCALE = 0.1F;

    private final Minecraft mc;

    public ClockHudRenderer(Minecraft mc) {
        this.mc = mc;
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (!guiActive) {
            return;
        }

        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        float scale = Math.max((float) Config.SCALE.getAsDouble(), MIN_SCALE);

        int xCoord;
        if (Config.CENTER_CLOCK.getAsBoolean()) {
            int totalWidth = HudConstants.BAR_LENGTH + HudConstants.SUN_WIDTH - HudConstants.DOT;
            xCoord = (int) ((guiGraphics.guiWidth() - totalWidth * scale) / (2 * scale));
        } else {
            xCoord = Config.X_COORD.getAsInt();
        }

        int yCoord = Config.Y_COORD.getAsInt();
        int startX = xCoord + HudConstants.SUN_WIDTH / 2 - HudConstants.DOT / 2;
        int startY = yCoord + HudConstants.ICON_HEIGHT / 2 - HudConstants.BAR_HEIGHT / 2;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);

        // Draw the progress bar.
        guiGraphics.blit(HudConstants.HUD_TEXTURE, startX, startY, 0, 0,
                HudConstants.BAR_LENGTH, HudConstants.BAR_HEIGHT,
                HudConstants.TEXTURE_WIDTH, HudConstants.TEXTURE_HEIGHT);

        int scaledTime = getScaledTime(level);
        if (isDay(getCurrentTime(level))) {
            // Draw the sun.
            guiGraphics.blit(HudConstants.HUD_TEXTURE, xCoord + scaledTime, yCoord, 0, HudConstants.BAR_HEIGHT,
                    HudConstants.SUN_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.TEXTURE_WIDTH, HudConstants.TEXTURE_HEIGHT);
        } else {
            // Draw the moon.
            guiGraphics.blit(HudConstants.HUD_TEXTURE,
                    xCoord + (HudConstants.SUN_WIDTH - HudConstants.MOON_WIDTH) / 2 + scaledTime, yCoord,
                    HudConstants.SUN_WIDTH, HudConstants.BAR_HEIGHT,
                    HudConstants.MOON_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.TEXTURE_WIDTH, HudConstants.TEXTURE_HEIGHT);
        }

        guiGraphics.pose().popPose();
    }

    /**
     * Scales the current time to the length of the bar.
     *
     * @return integer offset to be used when rendering the sun or moon.
     */
    private int getScaledTime(ClientLevel level) {
        int currentTime = getCurrentTime(level);

        if (isDay(currentTime)) {
            return currentTime * (HudConstants.BAR_LENGTH - HudConstants.DOT) / HudConstants.NEW_NIGHT_TICK;
        } else {
            return (currentTime - HudConstants.NEW_NIGHT_TICK) * (HudConstants.BAR_LENGTH - HudConstants.DOT)
                    / (HudConstants.DAY_TICKS - HudConstants.NEW_NIGHT_TICK);
        }
    }

    private boolean isDay(int currentTime) {
        return currentTime >= 0 && currentTime <= HudConstants.NEW_NIGHT_TICK;
    }

    private int getCurrentTime(ClientLevel level) {
        return (int) (level.getDayTime() % HudConstants.DAY_TICKS);
    }
}
