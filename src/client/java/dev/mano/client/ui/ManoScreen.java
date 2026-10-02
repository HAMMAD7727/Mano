package dev.mano.client.ui;

import dev.mano.Mano;
import dev.mano.client.ManoRuntime;
import dev.mano.client.destruct.SelfDestruct;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * Top navbar + one content panel. No modules — placeholders only.
 */
public final class ManoScreen extends Screen {
	private enum Tab {
		MODULE("Module"),
		FRIENDS("Friends"),
		CONFIG("Config"),
		DESTRUCT("Self destruct");

		final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	private static final int BAR_H = 40;
	private static final int PANEL_H = 168;
	private static final int ACCENT = 0xFF6EA8FF;
	private static final int TEXT = 0xFFE8ECF4;
	private static final int MUTED = 0xFF8B93A7;
	private static final int DANGER = 0xFFFF6B81;
	private static final int BAR_BG = 0xF20B0D12;
	private static final int PANEL_BG = 0xE612151E;
	private static final int HAIRLINE = 0x22FFFFFF;

	private Tab tab = Tab.MODULE;
	private float open = 0f;
	private boolean closing;
	private int confirmClicks;
	private long confirmUntil;

	public ManoScreen() {
		super(Text.literal(Mano.NAME));
	}

	public void beginClose() {
		closing = true;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	protected void init() {
		open = 0f;
		closing = false;
		confirmClicks = 0;
	}

	@Override
	public void tick() {
		if (!ManoRuntime.isAlive()) {
			this.client.setScreen(null);
			return;
		}

		float speed = 0.18f;
		if (closing) {
			open = Math.max(0f, open - speed);
			if (open <= 0.001f) {
				this.client.setScreen(null);
			}
		} else {
			open = Math.min(1f, open + speed);
		}

		if (confirmClicks > 0 && System.currentTimeMillis() > confirmUntil) {
			confirmClicks = 0;
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		// World stays visible under the chrome.
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		if (this.client == null) {
			return;
		}

		float t = easeOutCubic(open);
		int slide = Math.round((1f - t) * -(BAR_H + PANEL_H));

		int barTop = slide;
		int barBottom = slide + BAR_H;
		int panelTop = barBottom;
		int panelBottom = panelTop + PANEL_H;

		context.fill(0, barTop, this.width, barBottom, BAR_BG);
		context.fill(0, barBottom - 1, this.width, barBottom, ACCENT);
		context.fill(0, panelTop, this.width, panelBottom, PANEL_BG);
		context.fill(0, panelBottom, this.width, panelBottom + 1, HAIRLINE);

		int titleX = 18;
		int titleY = barTop + 14;
		context.drawText(this.textRenderer, Mano.NAME, titleX, titleY, TEXT, false);
		int nameW = this.textRenderer.getWidth(Mano.NAME);
		context.drawText(this.textRenderer, Mano.VERSION, titleX + nameW + 8, titleY, MUTED, false);

		int tabStart = Math.max(160, this.width / 3);
		int tabW = Math.min(110, Math.max(72, (this.width - tabStart - 16) / Tab.values().length));
		int i = 0;
		for (Tab candidate : Tab.values()) {
			int x = tabStart + i * tabW;
			boolean hover = mouseX >= x && mouseX < x + tabW && mouseY >= barTop && mouseY < barBottom;
			boolean active = candidate == tab;
			int color = active ? TEXT : (hover ? 0xFFD0D6E4 : MUTED);
			int labelW = this.textRenderer.getWidth(candidate.label);
			int lx = x + (tabW - labelW) / 2;
			context.drawText(this.textRenderer, candidate.label, lx, titleY, color, false);
			if (active) {
				int ux1 = x + 12;
				int ux2 = x + tabW - 12;
				context.fill(ux1, barBottom - 3, ux2, barBottom - 1, candidate == Tab.DESTRUCT ? DANGER : ACCENT);
			}
			i++;
		}

		renderPanel(context, panelTop, mouseX, mouseY);
	}

	private void renderPanel(DrawContext context, int panelTop, int mouseX, int mouseY) {
		int x = 22;
		int y = panelTop + 22;
		switch (tab) {
			case MODULE -> {
				heading(context, x, y, "Modules");
				body(context, x, y + 18, "Nothing here in 0.01 V. This tab is a placeholder.");
			}
			case FRIENDS -> {
				heading(context, x, y, "Friends");
				body(context, x, y + 18, "Friend list comes later. No entries yet.");
			}
			case CONFIG -> {
				heading(context, x, y, "Config");
				body(context, x, y + 18, "Menu  ·  Right Shift");
				body(context, x, y + 34, "Silent unload  ·  hold Right Ctrl + Backspace");
				body(context, x, y + 50, "No settings to save in this build.");
			}
			case DESTRUCT -> {
				heading(context, x, y, "Self destruct");
				body(context, x, y + 18, "Unloads Mano from this session. The jar on disk stays.");
				body(context, x, y + 34, "Restart Minecraft to load it again.");

				String label = confirmClicks == 0 ? "Self destruct" : "Click again to confirm";
				int bw = 168;
				int bh = 28;
				int bx = x;
				int by = y + 64;
				boolean hover = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
				int fill = hover ? 0xFFE2556A : 0xFFC43D54;
				context.fill(bx, by, bx + bw, by + bh, fill);
				context.fill(bx, by, bx + bw, by + 1, 0x33FFFFFF);
				int lw = this.textRenderer.getWidth(label);
				context.drawText(this.textRenderer, label, bx + (bw - lw) / 2, by + 10, 0xFFFFFFFF, false);
			}
		}

		String hint = "esc  close     rshift  toggle";
		context.drawText(this.textRenderer, hint.toUpperCase(Locale.ROOT), x, panelTop + PANEL_H - 22, 0xFF4E5568, false);
	}

	private void heading(DrawContext context, int x, int y, String s) {
		context.drawText(this.textRenderer, s, x, y, TEXT, false);
	}

	private void body(DrawContext context, int x, int y, String s) {
		context.drawText(this.textRenderer, s, x, y, MUTED, false);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (!ManoRuntime.isAlive()) {
			return false;
		}
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (button != 0) {
			return super.mouseClicked(click, doubled);
		}

		float t = easeOutCubic(open);
		int slide = Math.round((1f - t) * -(BAR_H + PANEL_H));
		int barTop = slide;
		int barBottom = slide + BAR_H;
		int panelTop = barBottom;

		int tabStart = Math.max(160, this.width / 3);
		int tabW = Math.min(110, Math.max(72, (this.width - tabStart - 16) / Tab.values().length));
		int i = 0;
		for (Tab candidate : Tab.values()) {
			int x = tabStart + i * tabW;
			if (mouseX >= x && mouseX < x + tabW && mouseY >= barTop && mouseY < barBottom) {
				tab = candidate;
				confirmClicks = 0;
				return true;
			}
			i++;
		}

		if (tab == Tab.DESTRUCT) {
			int bx = 22;
			int by = panelTop + 22 + 64;
			int bw = 168;
			int bh = 28;
			if (mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh) {
				onDestructClicked();
				return true;
			}
		}

		return super.mouseClicked(click, doubled);
	}

	private void onDestructClicked() {
		long now = System.currentTimeMillis();
		if (confirmClicks == 0 || now > confirmUntil) {
			confirmClicks = 1;
			confirmUntil = now + 2500L;
			return;
		}
		SelfDestruct.execute();
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			beginClose();
			return true;
		}
		if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
			beginClose();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void close() {
		beginClose();
	}

	private static float easeOutCubic(float x) {
		float inv = 1f - x;
		return 1f - inv * inv * inv;
	}
}
