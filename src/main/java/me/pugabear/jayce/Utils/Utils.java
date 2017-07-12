package me.pugabear.jayce.Utils;

import static me.pugabear.jayce.Jayce.CONFIG;

import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class Utils 
{
	// Method to hide messages in a channel with a webhook enabled
	public static void reply(CommandEvent event, String message)
	{
		if (!event.getChannel().getId().equals(CONFIG.webhookChannelId))
			event.reply(message);
	}
}
