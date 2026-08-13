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

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings for the mod.
 */
public final class KeyBindings {
    public static final KeyMapping TOGGLE = new KeyMapping(
            "keys.twelveideaclock.toggle",
            GLFW.GLFW_KEY_COMMA,
            "keys.twelveideaclock.category"
    );

    private KeyBindings() {
    }
}
