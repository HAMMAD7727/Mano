package dev.mano.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ManoKeys {
	public static final KeyBinding OPEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"key.mano.open",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_LEFT_SHIFT,
		KeyBinding.Category.MISC
	));

	private ManoKeys() {
	}

	public static void register() {
		// static init registers the bind
	}
}
