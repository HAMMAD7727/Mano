package dev.mano.desktop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds running Minecraft Java processes and their --gameDir / --version.
 * Read-only: no attach, no injection into the JVM.
 */
public final class MinecraftFinder {
	private static final Pattern VERSION = Pattern.compile("(?:^|\\s)--version(?:=|\\s+)(\\S+)");
	private static final Pattern GAME_DIR = Pattern.compile("(?:^|\\s)--gameDir(?:=|\\s+)(\"[^\"]+\"|\\S+)");
	private static final Pattern VERSION_TOKEN = Pattern.compile("1\\.\\d{1,2}(?:\\.\\d+)?");

	public record McProcess(long pid, String version, String instanceName, Path gameDir, String display) {
	}

	private MinecraftFinder() {
	}

	public static List<McProcess> scan() {
		LinkedHashMap<Long, McProcess> found = new LinkedHashMap<>();
		fromProcessHandle(found);
		if (found.isEmpty()) {
			fromPs(found);
		}
		return new ArrayList<>(found.values());
	}

	private static void fromProcessHandle(LinkedHashMap<Long, McProcess> out) {
		try {
			ProcessHandle.allProcesses().forEach(ph -> {
				if (ph.pid() == ProcessHandle.current().pid()) {
					return;
				}
				ProcessHandle.Info info = ph.info();
				String cmd = info.command().orElse("");
				String line = info.commandLine().orElseGet(() -> {
					String[] args = info.arguments().orElse(null);
					if (args == null || args.length == 0) {
						return cmd;
					}
					return cmd + " " + String.join(" ", args);
				});
				accept(out, ph.pid(), cmd, line);
			});
		} catch (Exception ignored) {
		}
	}

	private static void fromPs(LinkedHashMap<Long, McProcess> out) {
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		try {
			Process process;
			if (os.contains("win")) {
				process = new ProcessBuilder("wmic", "process", "where",
					"name='javaw.exe' or name='java.exe'",
					"get", "ProcessId,CommandLine", "/FORMAT:LIST")
					.redirectErrorStream(true)
					.start();
				parseWmic(out, process);
			} else {
				process = new ProcessBuilder("ps", "-ww", "-eo", "pid=,args=")
					.redirectErrorStream(true)
					.start();
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String row;
					while ((row = reader.readLine()) != null) {
						row = row.trim();
						if (row.isEmpty()) {
							continue;
						}
						int space = row.indexOf(' ');
						if (space <= 0) {
							continue;
						}
						long pid;
						try {
							pid = Long.parseLong(row.substring(0, space).trim());
						} catch (NumberFormatException e) {
							continue;
						}
						String line = row.substring(space).trim();
						accept(out, pid, line, line);
					}
				}
				process.destroy();
			}
		} catch (Exception ignored) {
		}
	}

	private static void parseWmic(LinkedHashMap<Long, McProcess> out, Process process) throws Exception {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String commandLine = null;
			String row;
			while ((row = reader.readLine()) != null) {
				row = row.trim();
				if (row.isEmpty()) {
					if (commandLine != null) {
						commandLine = null;
					}
					continue;
				}
				if (row.startsWith("CommandLine=")) {
					commandLine = row.substring("CommandLine=".length()).trim();
				} else if (row.startsWith("ProcessId=") && commandLine != null) {
					try {
						long pid = Long.parseLong(row.substring("ProcessId=".length()).trim());
						accept(out, pid, commandLine, commandLine);
					} catch (NumberFormatException ignored) {
					}
					commandLine = null;
				}
			}
		}
		process.destroy();
	}

	private static void accept(LinkedHashMap<Long, McProcess> out, long pid, String command, String line) {
		if (line == null || line.isBlank()) {
			return;
		}
		String lower = line.toLowerCase(Locale.ROOT);
		if (lower.contains("dev.mano.desktop") || lower.contains("manolauncher")) {
			return;
		}
		if (!looksLikeMinecraft(lower, command)) {
			return;
		}
		String version = firstGroup(VERSION, line).orElseGet(() -> guessVersion(line));
		Path gameDir = firstGroup(GAME_DIR, line)
			.map(MinecraftFinder::unquote)
			.map(Path::of)
			.filter(Files::isDirectory)
			.orElse(null);
		String instance = instanceName(gameDir);
		String display = version + " - " + instance;
		out.putIfAbsent(pid, new McProcess(pid, version, instance, gameDir, display));
	}

	private static boolean looksLikeMinecraft(String lower, String command) {
		if (lower.contains("net.minecraft.client.main.main")) {
			return true;
		}
		if (lower.contains("fabricloader") || lower.contains("net.fabricmc.loader")) {
			return true;
		}
		if (lower.contains("--gameDir".toLowerCase(Locale.ROOT)) && lower.contains("minecraft")) {
			return true;
		}
		String cmd = command == null ? "" : command.toLowerCase(Locale.ROOT);
		return (cmd.contains("javaw") || cmd.contains("java"))
			&& lower.contains("minecraft")
			&& (lower.contains(".minecraft") || lower.contains("versions") || lower.contains("libraries"));
	}

	private static Optional<String> firstGroup(Pattern pattern, String line) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			return Optional.of(matcher.group(1));
		}
		return Optional.empty();
	}

	private static String guessVersion(String line) {
		Matcher matcher = VERSION_TOKEN.matcher(line);
		String last = "unknown";
		while (matcher.find()) {
			last = matcher.group();
		}
		return last;
	}

	private static String unquote(String value) {
		if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	private static String instanceName(Path gameDir) {
		if (gameDir == null) {
			return "Minecraft";
		}
		Path name = gameDir.getFileName();
		if (name != null && name.toString().equalsIgnoreCase("minecraft") && gameDir.getParent() != null) {
			return gameDir.getParent().getFileName().toString();
		}
		if (name != null && name.toString().equals(".minecraft") && gameDir.getParent() != null) {
			return gameDir.getParent().getFileName().toString();
		}
		return name == null ? "Minecraft" : name.toString();
	}
}
