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

import net.minecraft.resources.ResourceLocation;
import org.twelveidea.clock.TwelveIdeaClockHUD;

/**
 * Constants shared by the HUD renderers.
 */
public final class HudConstants {
    public static final ResourceLocation HUD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TwelveIdeaClockHUD.MODID, "textures/gui/gui_clock.png");

    // Texture is authored at 2x scale on a 512x512 sheet.
    public static final int TEXTURE_SCALE = 2;
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    // The original mod used drawTexturedModalRect, which samples the sheet as a
    // 256x256 texture. UV coordinates are therefore given in the 256-unit system;
    // passing 256 as the texture size makes blit sample the 512 sheet at 2x,
    // exactly like the original renderer.
    public static final int UV_TEXTURE_WIDTH = 256;
    public static final int UV_TEXTURE_HEIGHT = 256;

    public static final int SUN_WIDTH = 48 / TEXTURE_SCALE;
    public static final int MOON_WIDTH = 32 / TEXTURE_SCALE;
    public static final int ICON_HEIGHT = 50 / TEXTURE_SCALE;
    public static final int BAR_LENGTH = 400 / TEXTURE_SCALE;
    public static final int BAR_HEIGHT = 10 / TEXTURE_SCALE;
    public static final int DOT = 10 / TEXTURE_SCALE;

    // The track dots are drawn programmatically (vector circles) at the original
    // 2:1 display scale, so they stay perfectly round regardless of GUI filtering.
    public static final int DOT_COLOR = 0xFFD3D3D3;      // 211,211,211 grey like the sheet
    public static final float DOT_LARGE_RADIUS = 2.0F;   // ~5px visual diameter
    public static final float DOT_SMALL_RADIUS = 1.25F;  // ~3.5px visual diameter
    public static final float[] DOT_LARGE_CENTER_X = {2.25F, 99.75F, 197F};
    public static final float[] DOT_SMALL_CENTER_X = {25F, 50F, 75F, 125F, 150F, 175F};

    // Time constants.
    public static final int DAY_TICKS = 24000;
    public static final int NEW_DAY_TICK = 50;
    public static final int NEW_NIGHT_TICK = 13000;

    private HudConstants() {
    }
}
