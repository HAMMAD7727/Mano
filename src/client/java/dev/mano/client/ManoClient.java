package dev.mano.client;

import dev.mano.Mano;
import dev.mano.client.destruct.SelfDestruct;
import dev.mano.client.ui.ManoScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManoClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(Mano.ID);

	@Override
	public void onInitializeClient() {
		ManoKeys.register();

		ClientTickEvents.END_CLIENT_TICK.register(ManoClient::onEndTick);
		LOGGER.info("{} {} loaded", Mano.NAME, Mano.VERSION);
	}

	private static void onEndTick(MinecraftClient client) {
		if (!ManoRuntime.isAlive()) {
			return;
		}

		SelfDestruct.tick(client);

		if (!ManoRuntime.isAlive() || client.player == null) {
			return;
		}

		while (ManoKeys.OPEN.wasPressed()) {
			toggleMenu(client);
		}
	}

	private static void toggleMenu(MinecraftClient client) {
		if (client.currentScreen instanceof ManoScreen screen) {
			screen.beginClose();
			return;
		}
		if (client.currentScreen == null) {
			client.setScreen(new ManoScreen());
		}
	}
}
