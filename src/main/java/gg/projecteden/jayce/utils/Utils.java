package gg.projecteden.jayce.utils;

import gg.projecteden.jayce.Jayce;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Utils {

	public static List<String> labelsOf(String input) {
		return List.of(input.split("(?<!:) "));
	}

	public static int getIssueId(TextChannel channel) {
		final String channelName = channel.getName();
		final String channelPrefix = "(?i)^[" + Jayce.UNRESOLVED + Jayce.RESOLVED + "]-" + requireNonNull(channel.getParent()).getName() + "-";
		if (!channelName.matches(channelPrefix + "\\d+$"))
			return -1;

		return Integer.parseInt(channelName.replaceAll(channelPrefix, ""));
	}

}
