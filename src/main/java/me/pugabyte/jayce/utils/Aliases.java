package me.pugabyte.jayce.utils;

import me.pugabyte.jayce.services.Users;
import org.eclipse.egit.github.core.User;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Aliases {

	public static Map<String, String> config = Utils.readConfig("aliases.json");

	public static CompletableFuture<User> githubOf(String discordId) {
		return Users.get(config.get(discordId));
	}

	public static String discordOf(String githubName) {
		for (String discordId : config.keySet())
			if (config.get(discordId).equalsIgnoreCase(githubName))
				return discordId;
		return null;
	}

}
