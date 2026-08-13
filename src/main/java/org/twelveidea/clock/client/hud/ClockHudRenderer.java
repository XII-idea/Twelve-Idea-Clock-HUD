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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.twelveidea.clock.Config;

/**
 * Renders the clock HUD: a dot track plus a sun/moon position indicator.
 * Ported from ClockHUD's GuiClock (MIT, Copyright (c) 2017 Sam Beckmann).
 *
 * The track dots are drawn as anti-aliased vector circles instead of sampling the
 * sheet, so they are perfectly round at any GUI scale.
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
        float trackCenterY = yCoord + HudConstants.ICON_HEIGHT / 2.0F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);

        // Track dots: large ones at both ends and the centre, small ones as ticks.
        for (float cx : HudConstants.DOT_LARGE_CENTER_X) {
            drawAaCircle(guiGraphics, xCoord + cx, trackCenterY,
                    HudConstants.DOT_LARGE_RADIUS, HudConstants.DOT_COLOR);
        }
        for (float cx : HudConstants.DOT_SMALL_CENTER_X) {
            drawAaCircle(guiGraphics, xCoord + cx, trackCenterY,
                    HudConstants.DOT_SMALL_RADIUS, HudConstants.DOT_COLOR);
        }

        // Position indicator: sun during the day, moon at night (sampled from the sheet).
        int scaledTime = getScaledTime(level);
        if (isDay(getCurrentTime(level))) {
            int sunX = xCoord + clampIconX(scaledTime, HudConstants.SUN_WIDTH);
            guiGraphics.blit(HudConstants.HUD_TEXTURE, sunX, yCoord,
                    HudConstants.SUN_WIDTH, HudConstants.ICON_HEIGHT,
                    0, HudConstants.BAR_HEIGHT, HudConstants.SUN_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.UV_TEXTURE_WIDTH, HudConstants.UV_TEXTURE_HEIGHT);
        } else {
            int moonX = xCoord + clampIconX(
                    scaledTime + (HudConstants.SUN_WIDTH - HudConstants.MOON_WIDTH) / 2,
                    HudConstants.MOON_WIDTH);
            guiGraphics.blit(HudConstants.HUD_TEXTURE, moonX, yCoord,
                    HudConstants.MOON_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.SUN_WIDTH, HudConstants.BAR_HEIGHT,
                    HudConstants.MOON_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.UV_TEXTURE_WIDTH, HudConstants.UV_TEXTURE_HEIGHT);
        }

        guiGraphics.pose().popPose();
    }

    /**
     * Draws a filled circle with a 1px anti-aliased edge using the current pose matrix.
     * A solid inner fan plus an alpha-fading outer ring keeps the outline smooth.
     */
    private static void drawAaCircle(GuiGraphics guiGraphics, float centerX, float centerY, float radius, int argb) {
        Matrix4f matrix = guiGraphics.pose().last().pose();
        int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        int segments = 20;
        float innerRadius = Math.max(radius - 0.5F, 0.0F);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        RenderSystem.enableBlend();
        try {
            // Solid inner circle.
            BufferBuilder fan = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            fan.addVertex(matrix, centerX, centerY, 0).setColor(red, green, blue, alpha);
            for (int i = 0; i <= segments; i++) {
                double angle = Math.PI * 2 * i / segments;
                fan.addVertex(matrix,
                        centerX + innerRadius * (float) Math.cos(angle),
                        centerY + innerRadius * (float) Math.sin(angle), 0)
                        .setColor(red, green, blue, alpha);
            }
            BufferUploader.drawWithShader(fan.buildOrThrow());

            // Anti-aliased edge ring (alpha fades from solid to transparent).
            float outerRadius = radius + 0.5F;
            BufferBuilder strip = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double angle = Math.PI * 2 * i / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                strip.addVertex(matrix, centerX + outerRadius * cos, centerY + outerRadius * sin, 0)
                        .setColor(red, green, blue, 0);
                strip.addVertex(matrix, centerX + innerRadius * cos, centerY + innerRadius * sin, 0)
                        .setColor(red, green, blue, alpha);
            }
            BufferUploader.drawWithShader(strip.buildOrThrow());
        } finally {
            // 恢复绘制前的混合状态，避免状态泄漏影响后续 HUD 渲染。
            if (!blendEnabled) {
                RenderSystem.disableBlend();
            }
        }
    }

    /**
     * 将指示图标的相对偏移限制在轨道范围内，使其右缘不越过轨道右端。
     *
     * @param relativeX 相对 xCoord 的偏移
     * @param iconWidth 指示图标宽度
     * @return 限制后的相对偏移
     */
    private static int clampIconX(int relativeX, int iconWidth) {
        int maxOffset = HudConstants.BAR_LENGTH - HudConstants.DOT - iconWidth;
        return Math.min(relativeX, maxOffset);
    }

    /**
     * Scales the current time to the length of the bar.
     *
     * @return integer offset to be used when rendering the sun or moon.
     */
    private int getScaledTime(ClientLevel level) {
        int currentTime = getCurrentTime(level);
        int maxOffset = HudConstants.BAR_LENGTH - HudConstants.DOT;

        if (isDay(currentTime)) {
            return currentTime * maxOffset / HudConstants.NEW_NIGHT_TICK;
        } else {
            return (currentTime - HudConstants.NEW_NIGHT_TICK) * maxOffset
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
