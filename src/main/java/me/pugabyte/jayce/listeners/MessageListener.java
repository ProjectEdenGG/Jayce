package me.pugabyte.jayce.listeners;

import me.pugabyte.jayce.services.Issues;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!event.getMessage().getContentDisplay().contains("git#"))
			return;

		String[] words = event.getMessage().getContentDisplay().split(" ");
		for (String word : words) {
			if (!word.matches("^git#\\d+"))
				continue;

			final int id = Integer.parseInt(word.replaceAll("git#", ""));
			final String url = Issues.repo().url(id).embed(false).get();
			event.getTextChannel().sendMessage(url).queue();
		}
	}

}
