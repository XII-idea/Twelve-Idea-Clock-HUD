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

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.twelveidea.clock.client.hud.ClockHudRenderer;
import org.twelveidea.clock.client.hud.DayCountRenderer;
import org.twelveidea.clock.client.keybind.KeyBindings;
import org.twelveidea.clock.client.keybind.KeyInputHandler;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TwelveIdeaClockHUD.MODID, dist = Dist.CLIENT)
// Register static @SubscribeEvent methods on the mod event bus.
@EventBusSubscriber(modid = TwelveIdeaClockHUD.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class TwelveIdeaClockHUDClient {
    public TwelveIdeaClockHUDClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Minecraft mc = Minecraft.getInstance();
        // Register HUD renderers and the key handler on the game event bus.
        NeoForge.EVENT_BUS.register(new ClockHudRenderer(mc));
        NeoForge.EVENT_BUS.register(new DayCountRenderer(mc));
        NeoForge.EVENT_BUS.register(new KeyInputHandler());
    }

    @SubscribeEvent
    static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.TOGGLE);
    }
}
