package gg.projecteden.jayce.listeners;

import gg.projecteden.jayce.services.Repos;
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

			final int issueId = Integer.parseInt(word.replaceAll("git#", ""));
			final String url = Repos.main().issues().url(issueId).embed(false).get();
			event.getTextChannel().sendMessage(url).queue();
		}
	}

}
