package me.pugabyte.jayce.utils;

import java.util.Map;

public class Config {

	public static Map<String, String> config = Utils.readConfig("config.json");

	public static String ENV = config.get("ENV");
	public static String GITHUB_TOKEN = config.get("GITHUB_TOKEN");
	public static String DISCORD_TOKEN = config.get("DISCORD_TOKEN");
	public static String GITHUB_USER = config.get("GITHUB_USER");
	public static String GITHUB_REPO = config.get("GITHUB_REPO");
	public static String ICON_URL = config.get("ICON_URL");
	public static String COMMAND_PREFIX = config.get("COMMAND_PREFIX");
	public static String WEBHOOK_CHANNEL_ID = config.get("WEBHOOK_CHANNEL_ID");
	public static String DATABASE_PASSWORD = config.get("DATABASE_PASSWORD");

}
