package dev.mano.desktop;

import dev.mano.Mano;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Copies this jar into an instance {@code mods/} folder.
 * Fabric still loads mods at startup — a running game must be relaunched.
 */
public final class ModInstaller {
	public record Result(boolean ok, Path dest, String message) {
	}

	private ModInstaller() {
	}

	public static Path selfJar() throws IOException {
		try {
			URI uri = ManoLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			Path path = Path.of(uri);
			if (Files.isRegularFile(path) && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
				return path;
			}
			throw new IOException("Mano is not running from a jar (path: " + path + ")");
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("Could not locate Mano jar", e);
		}
	}

	public static Result installTo(Path gameDir) {
		if (gameDir == null || !Files.isDirectory(gameDir)) {
			return new Result(false, null, "No game folder for this process. Use Install to pick .minecraft.");
		}
		Path mods = gameDir.resolve("mods");
		try {
			Files.createDirectories(mods);
			Path self = selfJar();
			Path dest = mods.resolve("mano-0.0.1.jar");
			Files.copy(self, dest, StandardCopyOption.REPLACE_EXISTING);
			String extra = fabricHint(mods, gameDir);
			return new Result(true, dest,
				"Copied Mano " + Mano.VERSION + " to " + dest
					+ ". Close Minecraft and launch again — Fabric loads mods at startup."
					+ extra);
		} catch (IOException e) {
			return new Result(false, null, "Install failed: " + e.getMessage());
		}
	}

	private static String fabricHint(Path mods, Path gameDir) {
		boolean fabricApi = false;
		try (Stream<Path> stream = Files.list(mods)) {
			fabricApi = stream
				.map(p -> p.getFileName().toString().toLowerCase(Locale.ROOT))
				.anyMatch(n -> n.startsWith("fabric-api") && n.endsWith(".jar"));
		} catch (IOException ignored) {
		}
		boolean fabricDir = Files.isDirectory(gameDir.resolve(".fabric"));
		if (fabricApi || fabricDir) {
			return "";
		}
		return " Warning: this folder does not look like a Fabric instance. Install Fabric Loader + Fabric API first.";
	}

	public static Path defaultMinecraftDir() {
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		Path home = Path.of(System.getProperty("user.home"));
		if (os.contains("win")) {
			String appdata = System.getenv("APPDATA");
			if (appdata != null && !appdata.isBlank()) {
				return Path.of(appdata, ".minecraft");
			}
			return home.resolve("AppData").resolve("Roaming").resolve(".minecraft");
		}
		if (os.contains("mac")) {
			return home.resolve("Library").resolve("Application Support").resolve("minecraft");
		}
		return home.resolve(".minecraft");
	}
}
