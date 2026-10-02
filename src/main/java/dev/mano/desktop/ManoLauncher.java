package dev.mano.desktop;

import dev.mano.Mano;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

/**
 * Double-click entry for {@code java -jar mano-*.jar}.
 * Opens the desktop installer UI. This is not a JVM attach agent.
 */
public final class ManoLauncher {
	public static void main(String[] args) {
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ignored) {
			}
			UIManager.put("Panel.background", new Color(0x1A1A1A));
			UIManager.put("OptionPane.background", new Color(0x1A1A1A));
			UIManager.put("OptionPane.messageForeground", Color.WHITE);
			new ManoWindow().setVisible(true);
		});
	}

	private ManoLauncher() {
	}

	static String title() {
		return Mano.NAME + " " + Mano.VERSION;
	}
}
