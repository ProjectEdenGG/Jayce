package gg.projecteden.jayce.listeners;

import gg.projecteden.jayce.github.Repos;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class IssueLinkListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		if (!event.getMessage().getContentDisplay().contains("git#"))
			return;

		String[] words = event.getMessage().getContentDisplay().split(" ");
		for (String word : words) {
			if (!word.matches("^git#\\d+"))
				continue;

			final int issueId = Integer.parseInt(word.replaceAll("git#", "").replaceAll("[^\\d]+", ""));
			final String url = Repos.main().issues().url(issueId).embed(false).build();
			event.getChannel().sendMessage(url).queue();
		}
	}

}
