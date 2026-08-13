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

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TwelveIdeaClockHUD.MODID)
public class TwelveIdeaClockHUD {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "twelveideaclock";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TwelveIdeaClockHUD(ModContainer modContainer) {
        // This is a client-only mod: register the config as CLIENT type so it only takes effect on the client.
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
