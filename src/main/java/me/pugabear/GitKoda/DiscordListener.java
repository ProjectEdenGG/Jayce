package me.pugabear.GitKoda;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.events.Event;
import org.apache.commons.lang3.StringUtils;

public class DiscordListener extends ListenerAdapter
{

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event)
	{
		System.out.println(event.getMessage().getContent());

		if (event.getChannel().getId().equals("241774576822910976"))
		{
			if (event.getMessage().getRawContent().startsWith("!issue"))
			{
				String message = event.getMessage().getRawContent();
				message = message.replaceFirst("!issue ", "");
				List<String> args = Arrays.asList(message.split(" "));
				int i = message.indexOf(' ');
				String sub = message.substring(0, i);
				String rest = message.substring(i+1);
				switch (sub)
				{
				case "create":
					try {
						String[] content = rest.split("( \\| )");
						Utils.createIssue(content[0], content[1]);
					} catch (ArrayIndexOutOfBoundsException ex) {
						Utils.createIssue(rest, "");
					}
					break;
					
				case "close": 
					Utils.closeIssue(rest);
					break;
					
				}
			}
		}
	}

}