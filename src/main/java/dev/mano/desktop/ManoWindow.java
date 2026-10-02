package dev.mano.desktop;

import dev.mano.Mano;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Dark desktop chrome matching the requested injector layout.
 * Branding is Mano 0.01 V — no third-party names.
 */
public final class ManoWindow extends JFrame {
	private static final Color BG = new Color(0x1A1A1A);
	private static final Color BAR = new Color(0x161616);
	private static final Color ACCENT = new Color(0x0078D7);
	private static final Color TEXT = Color.WHITE;
	private static final Color MUTED = new Color(0xB0B0B0);
	private static final Color LINK = new Color(0x4A9EFF);
	private static final Font UI = new Font("Segoe UI", Font.PLAIN, 15);
	private static final Font UI_SMALL = new Font("Segoe UI", Font.PLAIN, 13);
	private static final Font UI_BTN = new Font("Segoe UI", Font.BOLD, 18);
	private static final Font UI_TINY = new Font("Segoe UI", Font.PLAIN, 12);

	private enum Tab {
		INJECT("Inject"),
		ADVANCED("Advanced inject"),
		INSTALL("Install"),
		UPDATES("Updates"),
		SETTINGS("Settings");

		final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	private Tab tab = Tab.INJECT;
	private final CardLayout cards = new CardLayout();
	private final JPanel body = new JPanel(cards);
	private final JLabel processLabel = new JLabel("No Minecraft process found");
	private final JLabel statusLabel = new JLabel(" ");
	private MinecraftFinder.McProcess selected;
	private TabStrip tabStrip;
	private int dragX;
	private int dragY;
	private boolean dragging;

	public ManoWindow() {
		super(ManoLauncher.title());
		setUndecorated(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(900, 580);
		setLocationRelativeTo(null);
		getContentPane().setBackground(BG);
		setLayout(new BorderLayout());

		tabStrip = new TabStrip();
		add(tabStrip, BorderLayout.NORTH);

		body.setOpaque(true);
		body.setBackground(BG);
		body.add(injectPage(), Tab.INJECT.name());
		body.add(advancedPage(), Tab.ADVANCED.name());
		body.add(installPage(), Tab.INSTALL.name());
		body.add(updatesPage(), Tab.UPDATES.name());
		body.add(settingsPage(), Tab.SETTINGS.name());
		add(body, BorderLayout.CENTER);

		add(footer(), BorderLayout.SOUTH);

		new Timer(2500, e -> refreshProcesses()).start();
		refreshProcesses();
	}

	private JPanel injectPage() {
		JPanel page = new JPanel(null);
		page.setBackground(BG);

		JLabel heading = new JLabel("Found minecraft processes");
		heading.setForeground(TEXT);
		heading.setFont(UI);
		heading.setBounds(40, 48, 500, 24);
		page.add(heading);

		processLabel.setForeground(TEXT);
		processLabel.setFont(UI_SMALL);
		processLabel.setBounds(40, 80, 820, 22);
		page.add(processLabel);

		JButton inject = pillButton("Inject");
		inject.setBounds(330, 210, 240, 56);
		inject.addActionListener(e -> onInject());
		page.add(inject);

		statusLabel.setForeground(MUTED);
		statusLabel.setFont(UI_SMALL);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setBounds(40, 280, 820, 40);
		page.add(statusLabel);

		return page;
	}

	private JPanel advancedPage() {
		JPanel page = paddedPage();
		page.add(muted(
			"<html><div style='width:640px'>"
				+ "<b style='color:#ffffff'>Advanced inject</b><br><br>"
				+ "Mano is a Fabric mod. Java cannot attach extra bytecode into a Minecraft JVM that is already running "
				+ "without a debug/attach agent — that is process injection, and this client does not do it.<br><br>"
				+ "Inject copies Mano into the detected instance <code>mods</code> folder. Close the game, then launch again.<br><br>"
				+ "In-game menu keybind: <b>Shift</b>."
				+ "</div></html>"));
		return page;
	}

	private JPanel installPage() {
		JPanel page = paddedPage();
		page.add(muted(
			"<html><div style='width:640px'>"
				+ "<b style='color:#ffffff'>Install</b><br><br>"
				+ "Pick a Minecraft / Prism / Modrinth instance folder (the one that contains <code>mods</code>).<br>"
				+ "Needs Fabric Loader 0.18.4+ and Fabric API on 1.21.11."
				+ "</div></html>"));
		JButton pick = pillButton("Choose folder");
		pick.setPreferredSize(new Dimension(240, 48));
		pick.addActionListener(e -> onPickFolder());
		JPanel wrap = new JPanel();
		wrap.setOpaque(false);
		wrap.add(pick);
		page.add(wrap);
		return page;
	}

	private JPanel updatesPage() {
		JPanel page = paddedPage();
		page.add(muted(
			"<html><div style='width:640px'>"
				+ "<b style='color:#ffffff'>Updates</b><br><br>"
				+ Mano.NAME + " " + Mano.VERSION + " — Fabric 1.21.11 base.<br>"
				+ "No gameplay cheats in this build. Source: github.com/HAMMAD7727/Mano"
				+ "</div></html>"));
		return page;
	}

	private JPanel settingsPage() {
		JPanel page = paddedPage();
		page.add(muted(
			"<html><div style='width:640px'>"
				+ "<b style='color:#ffffff'>Settings</b><br><br>"
				+ "In-game GUI: <b>Shift</b><br>"
				+ "Close GUI: Esc<br>"
				+ "Self-destruct: hold Right Ctrl + Backspace ~1.2s<br><br>"
				+ "Navbar: Module · Friends · Config · Self destruct"
				+ "</div></html>"));
		return page;
	}

	private JPanel paddedPage() {
		JPanel page = new JPanel();
		page.setBackground(BG);
		page.setBorder(BorderFactory.createEmptyBorder(48, 40, 24, 40));
		page.setLayout(new javax.swing.BoxLayout(page, javax.swing.BoxLayout.Y_AXIS));
		return page;
	}

	private JLabel muted(String html) {
		JLabel label = new JLabel(html);
		label.setForeground(MUTED);
		label.setFont(UI_SMALL);
		label.setAlignmentX(LEFT_ALIGNMENT);
		return label;
	}

	private JPanel footer() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(BG);
		bar.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

		JLabel site = new JLabel("github.com/HAMMAD7727/Mano");
		site.setForeground(LINK);
		site.setFont(UI_SMALL);
		site.setHorizontalAlignment(SwingConstants.CENTER);
		site.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		site.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://github.com/HAMMAD7727/Mano"));
				} catch (Exception ignored) {
				}
			}
		});
		bar.add(site, BorderLayout.CENTER);

		JLabel ver = new JLabel(Mano.VERSION);
		ver.setForeground(TEXT);
		ver.setFont(UI_TINY);
		ver.setHorizontalAlignment(SwingConstants.RIGHT);
		bar.add(ver, BorderLayout.EAST);
		return bar;
	}

	private JButton pillButton(String text) {
		JButton button = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(getModel().isPressed() ? ACCENT.darker() : ACCENT);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				g2.setColor(TEXT);
				g2.setFont(UI_BTN);
				int tw = g2.getFontMetrics().stringWidth(getText());
				int th = g2.getFontMetrics().getAscent();
				g2.drawString(getText(), (getWidth() - tw) / 2, (getHeight() + th) / 2 - 4);
				g2.dispose();
			}
		};
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setForeground(TEXT);
		return button;
	}

	private void showTab(Tab next) {
		tab = next;
		cards.show(body, next.name());
		tabStrip.repaint();
	}

	private void refreshProcesses() {
		List<MinecraftFinder.McProcess> list = MinecraftFinder.scan();
		if (list.isEmpty()) {
			selected = null;
			processLabel.setText("No Minecraft process found");
			return;
		}
		selected = list.get(0);
		processLabel.setText(selected.display());
	}

	private void onInject() {
		if (selected == null || selected.gameDir() == null) {
			statusLabel.setText("Launch Minecraft first, or use Install to pick a folder.");
			if (selected == null) {
				JOptionPane.showMessageDialog(this,
					"No running Minecraft process. Start the game (Fabric 1.21.11), then click Inject.\n"
						+ "Inject copies Mano into mods/ — you still need to restart the game.",
					ManoLauncher.title(),
					JOptionPane.INFORMATION_MESSAGE);
			}
			return;
		}
		ModInstaller.Result result = ModInstaller.installTo(selected.gameDir());
		statusLabel.setText(result.message());
		JOptionPane.showMessageDialog(this, result.message(), ManoLauncher.title(),
			result.ok() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
	}

	private void onPickFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select Minecraft instance folder");
		Path fallback = ModInstaller.defaultMinecraftDir();
		if (Files.isDirectory(fallback)) {
			chooser.setCurrentDirectory(fallback.toFile());
		}
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Path dir = chooser.getSelectedFile().toPath();
		ModInstaller.Result result = ModInstaller.installTo(dir);
		JOptionPane.showMessageDialog(this, result.message(), ManoLauncher.title(),
			result.ok() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
	}

	private final class TabStrip extends JPanel {
		private static final int H = 48;
		private final String[] labels = {"Inject", "Advanced inject", "Install", "Updates"};
		private final Tab[] tabs = {Tab.INJECT, Tab.ADVANCED, Tab.INSTALL, Tab.UPDATES};

		TabStrip() {
			setPreferredSize(new Dimension(900, H));
			setBackground(BAR);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					dragX = e.getXOnScreen() - ManoWindow.this.getX();
					dragY = e.getYOnScreen() - ManoWindow.this.getY();
					dragging = false;
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (dragging) {
						dragging = false;
						return;
					}
					int x = e.getX();
					if (x >= getWidth() - 36) {
						System.exit(0);
						return;
					}
					if (x >= getWidth() - 72) {
						showTab(Tab.SETTINGS);
						return;
					}
					int start = 28;
					for (int i = 0; i < labels.length; i++) {
						int w = tabWidth(labels[i]);
						if (x >= start && x < start + w) {
							showTab(tabs[i]);
							return;
						}
						start += w;
					}
				}
			});
			addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					dragging = true;
					ManoWindow.this.setLocation(e.getXOnScreen() - dragX, e.getYOnScreen() - dragY);
				}
			});
		}

		private int tabWidth(String label) {
			return 28 + getFontMetrics(UI).stringWidth(label) + 28;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setColor(BAR);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setFont(UI);
			int x = 28;
			for (int i = 0; i < labels.length; i++) {
				boolean active = tab == tabs[i];
				g2.setColor(TEXT);
				int tw = g2.getFontMetrics().stringWidth(labels[i]);
				int w = tabWidth(labels[i]);
				g2.drawString(labels[i], x + (w - tw) / 2, 30);
				if (active) {
					g2.setColor(ACCENT);
					g2.fillRect(x + 8, getHeight() - 3, w - 16, 3);
				}
				x += w;
			}
			g2.setColor(tab == Tab.SETTINGS ? ACCENT : TEXT);
			int gx = getWidth() - 62;
			int gy = 16;
			g2.drawOval(gx, gy, 16, 16);
			g2.drawOval(gx + 5, gy + 5, 6, 6);
			g2.setColor(TEXT);
			int cx = getWidth() - 28;
			g2.drawLine(cx, 18, cx + 12, 30);
			g2.drawLine(cx + 12, 18, cx, 30);
			g2.dispose();
		}
	}
}
