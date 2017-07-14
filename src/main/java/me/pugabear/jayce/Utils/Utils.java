package me.pugabear.jayce.Utils;

import com.jagrosh.jdautilities.commandclient.CommandEvent;

import static me.pugabear.jayce.Jayce.CONFIG;

public class Utils {
	// Method to hide messages in a channel with a webhook enabled
	public static void reply(CommandEvent event, String message) {
		if (!event.getChannel().getId().equals(CONFIG.webhookChannelId))
			event.reply(message);
	}
}
