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
package org.twelveidea.clock;

import net.neoforged.neoforge.common.ModConfigSpec;

// Configuration for Twelve Idea Clock HUD, loaded as a CLIENT type config (client-only mod).
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_DAY_COUNT = BUILDER
            .comment("Display the day count at the beginning of each day", "Default: true")
            .define("showDayCount", true);

    public static final ModConfigSpec.BooleanValue CENTER_CLOCK = BUILDER
            .comment("If true, ignore xCoord and always lock the clock to the center of the screen", "Default: false")
            .define("centeredClock", false);

    public static final ModConfigSpec.BooleanValue HIDE_IN_DEBUG = BUILDER
            .comment("Hide the clock HUD while the F3 debug screen is open", "Default: true")
            .define("hideInDebug", true);

    public static final ModConfigSpec.IntValue X_COORD = BUILDER
            .comment("Starting x coordinate of the clock")
            .defineInRange("xCoord", 2, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue Y_COORD = BUILDER
            .comment("Starting y coordinate of the clock")
            .defineInRange("yCoord", 2, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue SCALE = BUILDER
            .comment("Scale of the clock")
            .defineInRange("scale", 0.7, 0.0, 3.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
