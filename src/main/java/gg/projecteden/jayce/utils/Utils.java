package gg.projecteden.jayce.utils;

import gg.projecteden.jayce.Jayce;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Utils {

	public static List<String> labelsOf(String input) {
		return List.of(input.split("(?<!:) "));
	}

	public static int getIssueId(ICategorizableChannel channel) {
		final String channelName = channel.getName();
		final String channelPrefix = "(?i)^[" + Jayce.UNRESOLVED + Jayce.RESOLVED + "]-" + requireNonNull(channel.getParentCategory()).getName() + "-";
		if (!channelName.matches(channelPrefix + "\\d+$"))
			return -1;

		return Integer.parseInt(channelName.replaceAll(channelPrefix, ""));
	}

}
