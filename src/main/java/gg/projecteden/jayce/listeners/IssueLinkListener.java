package gg.projecteden.jayce.listeners;

import gg.projecteden.annotations.Environments;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.listeners.common.DiscordListener;
import gg.projecteden.utils.Env;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@Environments(Env.PROD)
public class IssueLinkListener extends DiscordListener {

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		try {
			final String content = event.getMessage().getContentDisplay();
			if (!content.contains("git#"))
				return;

			String[] words = content.split(" ");
			for (String word : words) {
				if (!word.matches("^git#\\d+"))
					continue;

				final int issueId = Integer.parseInt(word.replaceAll("git#", "").replaceAll("[^\\d]+", ""));
				final String url = Repos.main().issues().url(issueId).embed(false).build();
				event.getChannel().sendMessage(url).queue();
			}
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

}
