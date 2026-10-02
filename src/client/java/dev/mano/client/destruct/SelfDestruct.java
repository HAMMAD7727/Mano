package dev.mano.client.destruct;

import dev.mano.Mano;
import dev.mano.client.ManoClient;
import dev.mano.client.ManoRuntime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Silent wipe of the in-memory client. Does not delete the jar on disk.
 *
 * Gesture: hold Right Ctrl + Backspace for HOLD_MS (ignored while typing in chat).
 */
public final class SelfDestruct {
	public static final long HOLD_MS = 1200L;

	private static long holdStartedAt = -1L;

	private SelfDestruct() {
	}

	public static void tick(MinecraftClient client) {
		if (!ManoRuntime.isAlive() || client.getWindow() == null) {
			return;
		}
		if (client.currentScreen instanceof ChatScreen) {
			holdStartedAt = -1L;
			return;
		}

		boolean combo = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL)
			&& InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_BACKSPACE);

		if (!combo) {
			holdStartedAt = -1L;
			return;
		}

		long now = System.currentTimeMillis();
		if (holdStartedAt < 0L) {
			holdStartedAt = now;
			return;
		}
		if (now - holdStartedAt >= HOLD_MS) {
			execute();
		}
	}

	public static void execute() {
		if (!ManoRuntime.isAlive()) {
			return;
		}
		holdStartedAt = -1L;
		ManoRuntime.markDestroyed();
		ManoClient.LOGGER.info("{} {} unloaded", Mano.NAME, Mano.VERSION);
	}
}
