package gg.projecteden.jayce.utils;

import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Utils {

	public static List<String> labelsOf(String input) {
		return List.of(input.split("(?<!:) "));
	}

	@Nullable
	public static int getIssueId(TextChannel channel) {
		final String channelName = channel.getName();
		final String channelPrefix = "(?i)" + requireNonNull(channel.getParent()).getName() + "-";
		if (!channelName.matches(channelPrefix + "\\d+"))
			return -1;

		return Integer.parseInt(channelName.replaceAll(channelPrefix, ""));
	}

}
