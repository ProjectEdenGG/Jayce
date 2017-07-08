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
				int i = message.indexOf(' ');
				String sub = message.substring(0, i);
				String rest = message.substring(i+1);
				switch (sub)
				{
				case "create":
				case "open":
				case "make":
					try {
						String[] content = rest.split("( \\| )");
						IssueManager.createIssue(content[0], content[1]);
					} catch (ArrayIndexOutOfBoundsException ex) {
						IssueManager.createIssue(rest, "");
					}
					break;

				case "edit":
				case "set":
					try {
						i = rest.indexOf(' ');
						String id = rest.substring(0, i);
						String edit = rest.substring(i+1);
						String[] content = edit.split("( \\| )");
						IssueManager.editIssue(id, content[0], content[1]);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					break;

				case "label":
				case "labels":
					try {
						i = rest.indexOf(' ');
						String action = rest.substring(0, i);
						rest = rest.substring(i+1);
						i = rest.indexOf(' ');
						String id = rest.substring(0, i);
						String labels = rest.substring(i+1);
						IssueManager.changeLabels(action, id, labels.split(" "));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					break;

				case "close": 
				case "delete": 
					IssueManager.closeIssue(rest);
					break;
					
				}
			}
		}
	}

}