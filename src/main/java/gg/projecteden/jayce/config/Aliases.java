package gg.projecteden.jayce.config;

import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.Map;

public class Aliases {

	public static Map<String, String> config = Config.read("aliases.json");

	public static List<String> githubOf(List<Member> members) {
		return members.stream().map(Member::getId).map(Aliases::githubOf).toList();
	}

	public static String githubOf(String discordId) {
		return config.get(discordId);
	}

	public static String discordOf(String githubName) {
		for (String discordId : config.keySet())
			if (config.get(discordId).equalsIgnoreCase(githubName))
				return discordId;
		return null;
	}

}
