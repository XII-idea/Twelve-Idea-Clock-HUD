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
package org.twelveidea.clock.client.keybind;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.twelveidea.clock.client.hud.ClockHudRenderer;

/**
 * Handles the results of keyboard events.
 */
public class KeyInputHandler {
    @SubscribeEvent
    public void handleClientTick(ClientTickEvent.Pre event) {
        if (KeyBindings.TOGGLE.consumeClick()) {
            ClockHudRenderer.guiActive = !ClockHudRenderer.guiActive;
        }
    }
}
