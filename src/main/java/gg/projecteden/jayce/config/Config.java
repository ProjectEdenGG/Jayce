package gg.projecteden.jayce.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gg.projecteden.jayce.Jayce;
import lombok.SneakyThrows;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Config {

	public static Map<String, String> config = Config.read("config.json");

	public static String ENV = config.get("ENV");
	public static String GITHUB_TOKEN = config.get("GITHUB_TOKEN");
	public static String DISCORD_TOKEN = config.get("DISCORD_TOKEN");
	public static String GITHUB_USER = config.get("GITHUB_USER");
	public static String GITHUB_REPO = config.get("GITHUB_REPO");
	public static String ICON_URL = config.get("ICON_URL");
	public static String COMMAND_PREFIX = config.get("COMMAND_PREFIX");
	public static String WEBHOOK_CHANNEL_ID = config.get("WEBHOOK_CHANNEL_ID");
	public static String DATABASE_PASSWORD = config.get("DATABASE_PASSWORD");

	@SneakyThrows
	public static Map<String, String> read(String file) {
		try {
			final String path = Jayce.class.getSimpleName() + FileSystems.getDefault().getSeparator() + file;
			String json = String.join("", Files.readAllLines(Paths.get(path)));
			return new Gson().fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
		} catch (Exception ex) {
			ex.printStackTrace();
			return new HashMap<>();
		}
	}

}
