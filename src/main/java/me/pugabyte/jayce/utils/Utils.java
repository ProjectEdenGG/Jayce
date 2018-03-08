package me.pugabyte.jayce.utils;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;

public class Utils {
	// Method to hide messages in a channel with a webhook enabled
	public static void reply(CommandEvent event, String message) {
		if (!event.getChannel().getId().equals(Jayce.CONFIG.webhookChannelId))
			event.reply(message);
	}
}
