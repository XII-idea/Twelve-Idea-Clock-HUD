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
            // 居中基准为滑轨轨道（含内边距），使圆点轨道相对屏幕水平居中。
            float totalWidth = 2 * HudConstants.TRACK_OFFSET + HudConstants.BAR_LENGTH;
            xCoord = (int) ((guiGraphics.guiWidth() - totalWidth * scale) / (2 * scale));
        } else {
            xCoord = Config.X_COORD.getAsInt();
        }

        int yCoord = Config.Y_COORD.getAsInt();
        float trackCenterY = yCoord + HudConstants.ICON_HEIGHT / 2.0F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);

        // Track dots: large ones at both ends and the centre, small ones as ticks.
        // 所有点合并为一次三角形列表绘制，避免每点多次 draw 调用。
        drawTrackDots(guiGraphics, xCoord + HudConstants.TRACK_OFFSET, trackCenterY);

        // Position indicator: sun during the day, moon at night (sampled from the sheet).
        // 太阳/月亮从 xCoord 起沿轨道滑动，不做端点限制；仅滑轨保留内边距偏移。
        int currentTime = getCurrentTime(level);
        int scaledTime = getScaledTime(currentTime);
        if (isDay(currentTime)) {
            int sunX = xCoord + scaledTime;
            guiGraphics.blit(HudConstants.HUD_TEXTURE, sunX, yCoord,
                    HudConstants.SUN_WIDTH, HudConstants.ICON_HEIGHT,
                    0, HudConstants.BAR_HEIGHT, HudConstants.SUN_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.UV_TEXTURE_WIDTH, HudConstants.UV_TEXTURE_HEIGHT);
        } else {
            int moonX = xCoord + (HudConstants.SUN_WIDTH - HudConstants.MOON_WIDTH) / 2 + scaledTime;
            guiGraphics.blit(HudConstants.HUD_TEXTURE, moonX, yCoord,
                    HudConstants.MOON_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.SUN_WIDTH, HudConstants.BAR_HEIGHT,
                    HudConstants.MOON_WIDTH, HudConstants.ICON_HEIGHT,
                    HudConstants.UV_TEXTURE_WIDTH, HudConstants.UV_TEXTURE_HEIGHT);
        }

        guiGraphics.pose().popPose();
    }

    /**
     * 将轨道上的所有点（两端与中心的大点、刻度小点）合并为一次三角形列表绘制。
     * 每个点由实心圆盘加抗锯齿外环组成，全部顶点写入同一个 BufferBuilder 后一次性上传，
     * 从而把每帧多次 draw 调用收敛为一次。
     */
    private static void drawTrackDots(GuiGraphics guiGraphics, float trackStartX, float trackCenterY) {
        Matrix4f matrix = guiGraphics.pose().last().pose();
        int alpha = (HudConstants.DOT_COLOR >> 24) & 0xFF;
        int red = (HudConstants.DOT_COLOR >> 16) & 0xFF;
        int green = (HudConstants.DOT_COLOR >> 8) & 0xFF;
        int blue = HudConstants.DOT_COLOR & 0xFF;
        int segments = 20;

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (float cx : HudConstants.DOT_LARGE_CENTER_X) {
            appendCircleTriangles(buffer, matrix, trackStartX + cx, trackCenterY,
                    HudConstants.DOT_LARGE_RADIUS, red, green, blue, alpha, segments);
        }
        for (float cx : HudConstants.DOT_SMALL_CENTER_X) {
            appendCircleTriangles(buffer, matrix, trackStartX + cx, trackCenterY,
                    HudConstants.DOT_SMALL_RADIUS, red, green, blue, alpha, segments);
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        RenderSystem.enableBlend();
        try {
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } finally {
            // 恢复绘制前的混合状态，避免状态泄漏影响后续 HUD 渲染。
            if (!blendEnabled) {
                RenderSystem.disableBlend();
            }
        }
    }

    /**
     * 向 buffer 追加单个点的三角形：实心内圆盘 + 外缘抗锯齿环（alpha 由内向外渐隐）。
     */
    private static void appendCircleTriangles(BufferBuilder buffer, Matrix4f matrix,
            float centerX, float centerY, float radius, int red, int green, int blue, int alpha, int segments) {
        float innerRadius = Math.max(radius - 0.5F, 0.0F);
        float outerRadius = radius + 0.5F;
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2 * i / segments;
            double a1 = Math.PI * 2 * (i + 1) / segments;
            float cos0 = (float) Math.cos(a0);
            float sin0 = (float) Math.sin(a0);
            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);

            // 实心圆盘：圆心与内圆边界组成的三角形。
            buffer.addVertex(matrix, centerX, centerY, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(matrix, centerX + innerRadius * cos0, centerY + innerRadius * sin0, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(matrix, centerX + innerRadius * cos1, centerY + innerRadius * sin1, 0).setColor(red, green, blue, alpha);

            // 抗锯齿外环：内圆(实色)到外圆(透明)渐变，两个三角形。
            buffer.addVertex(matrix, centerX + outerRadius * cos0, centerY + outerRadius * sin0, 0).setColor(red, green, blue, 0);
            buffer.addVertex(matrix, centerX + innerRadius * cos0, centerY + innerRadius * sin0, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(matrix, centerX + outerRadius * cos1, centerY + outerRadius * sin1, 0).setColor(red, green, blue, 0);

            buffer.addVertex(matrix, centerX + innerRadius * cos0, centerY + innerRadius * sin0, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(matrix, centerX + innerRadius * cos1, centerY + innerRadius * sin1, 0).setColor(red, green, blue, alpha);
            buffer.addVertex(matrix, centerX + outerRadius * cos1, centerY + outerRadius * sin1, 0).setColor(red, green, blue, 0);
        }
    }

    /**
     * Scales the current time to the length of the bar.
     *
     * @param currentTime the day time within a single day.
     * @return integer offset to be used when rendering the sun or moon.
     */
    private int getScaledTime(int currentTime) {
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
