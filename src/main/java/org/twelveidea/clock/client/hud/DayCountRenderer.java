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

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.twelveidea.clock.Config;

/**
 * Renders an animated "Day: N" text at the beginning of each new day.
 * Ported from ClockHUD's GuiDayCount (MIT, Copyright (c) 2017 Sam Beckmann).
 */
public class DayCountRenderer {
    private static final int ANIMATION_TIME = 3000; // 3 second animation

    private final Minecraft mc;
    private long endAnimationTime;
    private boolean isRunning;

    public DayCountRenderer(Minecraft mc) {
        this.mc = mc;
        this.isRunning = false;
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        if (Config.SHOW_DAY_COUNT.getAsBoolean() && (isRunning || isNewDay(level))) {
            long currentTime = Util.getMillis();

            if (isRunning && currentTime >= endAnimationTime) {
                isRunning = false;
                return;
            }

            if (!isRunning) {
                isRunning = true;
                endAnimationTime = currentTime + ANIMATION_TIME;
            }

            float percentRemaining = (endAnimationTime - currentTime) / (float) ANIMATION_TIME;
            float scaleFactor = getScaleFactor(percentRemaining);
            String dayString = formDayString(level);

            GuiGraphics guiGraphics = event.getGuiGraphics();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scaleFactor, scaleFactor, scaleFactor);

            int alpha = Math.max(getOpacityFactor(percentRemaining), 5);
            int color = (alpha << 24) | 0xffffff;
            float xPos = (guiGraphics.guiWidth() - mc.font.width(dayString) * scaleFactor) / (2 * scaleFactor);
            float yPos = guiGraphics.guiHeight() / 7 / scaleFactor;

            guiGraphics.drawString(mc.font, dayString, xPos, yPos, color, false);

            guiGraphics.pose().popPose();
        }
    }

    /**
     * Tests if it's a new day.
     *
     * @return if the dayTime is the specified time of a new day.
     */
    private boolean isNewDay(ClientLevel level) {
        return level.getDayTime() % HudConstants.DAY_TICKS == HudConstants.NEW_DAY_TICK;
    }

    /**
     * Creates the day string based on the world day time.
     *
     * <p>Uses getDayTime (not getGameTime): sleeping advances the day counter, while
     * gameTime only counts ticks actually run, which would keep the count stuck after
     * players sleep through the night.</p>
     *
     * @return a string of "Day: " + day number.
     */
    private String formDayString(ClientLevel level) {
        return Component.translatable("twelveideaclock.daycount",
                Math.floorDiv(level.getDayTime(), HudConstants.DAY_TICKS)).getString();
    }

    /**
     * Gets the factor that the text should be scaled by.
     * Ensures even scaling throughout the time of the animation.
     *
     * @param percentRemaining scaled value between 0-1 indicating percent of the animation remaining.
     * @return value evenly scaled between 2 and 2.5 based on input.
     */
    private float getScaleFactor(float percentRemaining) {
        return 2.5F - percentRemaining / 2;
    }

    /**
     * Gets the opacity at which the text should be displayed.
     * Handles fade in/out of text.
     *
     * @param percentRemaining scaled value between 0-1 indicating percent of the animation remaining.
     * @return value between 0 and 255, indicating alpha value.
     */
    private int getOpacityFactor(float percentRemaining) {
        if (percentRemaining > 0.8F) {
            return (int) (255 * (0.8F - scale(percentRemaining, 0.8F, 1F, 0F, 0.8F)));
        } else if (percentRemaining < 0.2F) {
            return (int) (255 * scale(percentRemaining, 0F, 0.2F, 0F, 0.8F));
        } else {
            return (int) (255 * 0.8F);
        }
    }

    /**
     * Scales a value from one range to another.
     */
    private float scale(float valueIn, float baseMin, float baseMax, float limitMin, float limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }
}
