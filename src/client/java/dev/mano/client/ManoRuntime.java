package dev.mano.client;

import net.minecraft.client.MinecraftClient;

/**
 * Session-level switch. Once flipped, every Mano hook no-ops until the game restarts.
 */
public final class ManoRuntime {
	private static volatile boolean alive = true;

	private ManoRuntime() {
	}

	public static boolean isAlive() {
		return alive;
	}

	public static void markDestroyed() {
		alive = false;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null && client.currentScreen instanceof dev.mano.client.ui.ManoScreen) {
			client.execute(() -> {
				if (client.currentScreen instanceof dev.mano.client.ui.ManoScreen) {
					client.setScreen(null);
				}
			});
		}
	}
}
