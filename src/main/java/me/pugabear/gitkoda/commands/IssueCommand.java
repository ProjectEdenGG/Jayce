package me.pugabear.gitkoda.commands;

import me.pugabear.gitkoda.managers.IssueManager;
import me.pugabear.gitkoda.managers.LabelManager;
import me.pugabear.gitkoda.utils.Utils;

import static me.pugabear.gitkoda.GitKoda.CONFIG;

import org.eclipse.egit.github.core.SearchIssue;

import net.dv8tion.jda.core.EmbedBuilder;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.commandclient.Command;

import java.util.Arrays;
import java.util.List;

public class IssueCommand extends Command 
{
	public IssueCommand() 
	{
		this.name = CONFIG.commandName;
		if (!CONFIG.requiredRole.isEmpty())
		{
			this.requiredRole = CONFIG.requiredRole;
		}
		this.aliases = CONFIG.commandAliases;
	}

	protected void execute(CommandEvent event)
	{
		String[] args = event.getArgs().split(" ");
		if (args.length == 1)
		{
			event.reply("Missing arguments");
			return;
		}

		String name = null;
		try
		{
			if (event.getMember().getNickname().equals("null"))
			{
				name = event.getAuthor().getName();
			}
			else
			{
				name = event.getMember().getNickname();
			}
		}
		catch (NullPointerException ex)
		{
			name = event.getAuthor().getName();
		}

		System.out.println(name + ": " + event.getArgs());

		switch (args[0].toLowerCase())
		{
			case "create":
			{
				// TODO Allow setting more options on creation (assignees, labels...)
				String[] content = event.getArgs().split(" ", 2)[1].split("( \\| )", 2);
				int id = 0;
				try
				{
					id = IssueManager.createIssue(content[0], content[1], name);
				} 
				catch (ArrayIndexOutOfBoundsException ex) 
				{
					id = IssueManager.createIssue(content[0], "", name);
				}
	
				if (id != 0)
				{
					Utils.reply(event, "https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues/" + id);
				}
				else
				{
					event.reply("Issue creation failed");
				}
	
				break;
			}
	
			case "edit":
			{
				String id = args[1];
				String what = args[2].toLowerCase(); 
				String content =  String.join(" ", Arrays.copyOfRange(args, 3, args.length));
	
				if (IssueManager.editIssue(id, what, content))
				{
					event.reply(":thumbsup:");
				}
				else 
				{
					event.reply("Could not edit issue");
				}
	
				break;
			}
	
			case "close":
			{
				if (IssueManager.changeState(args[1], "closed"))
				{
					Utils.reply(event, "Closed issue: <https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues/" + args[1] + ">");
				}
				else
				{
					event.reply("Could not close issue");
				}
	
				break;
			}
	
			case "open":
			{
				if (IssueManager.changeState(args[1], "open"))
				{
					Utils.reply(event, "Opened issue: <https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues/" + args[1] + ">");
				}
				else
				{
					event.reply("Could not open issue");
				}
	
				break;
			}
	
			case "label": case "labels":
			{
				// TODO Allow adding multiple labels at once
				String id = args[1];
				String action = args[2].toLowerCase();
				String[] labels = Arrays.copyOfRange(args, 3, args.length);
				boolean result = false;
				if (action.equals("add"))
				{
					result = LabelManager.addLabels(id, labels);
				}
				else if (action.equals("remove"))
				{
					result = LabelManager.removeLabels(id, labels);
				} 
				else 
				{
					event.reply("Unknown action");
				}
	
				if (result)
				{
					event.reply(":thumbsup:");
				}
				else
				{
					event.reply("Could not modify labels");
				}
	
				break;
			}
	
			case "assign":
			{
				// TODO Allow assigning multiple people at once
				String id = args[1];
				String user = args[2];
				if (IssueManager.assign(id, user))
				{
					event.reply(":thumbsup:");
				}
				else
				{
					event.reply("Couldn't assign user to issue");
				}
	
				break;
			}
	
			case "comment":
			{
				String id = args[1];
				String comment =  String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				if (IssueManager.comment(id, comment, name))
				{
					Utils.reply(event, "Successfully added comment to issue #" + id);
				}
				else
				{
					event.reply("Couldn't add comment");
				}
	
				break;
			}
	
			case "search":
			{
				String state = "open";
				int i = 1;
				if (args[1].equalsIgnoreCase("open") || args[1].equalsIgnoreCase("closed")) 
				{
					state = args[1];
					i = 2;	
				}
	
				String query =  String.join(" ", Arrays.copyOfRange(args, i, args.length));
				List<SearchIssue> results = IssueManager.search(state, query);
				
				String body = "";
				for (SearchIssue issue : results) {
					body += "#" + issue.getNumber() + ": " + "[" + issue.getTitle() + "](https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues/" + issue.getNumber() + ") " + " - " + issue.getUser();
					body += System.lineSeparator() + System.lineSeparator();
				}
	
				event.reply(new EmbedBuilder()
						.setAuthor("Found " + results.size() + " issue" + (results.size() != 1 ? "s" : ""), "https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues", CONFIG.iconUrl)
						.setDescription(body)
						.build());
	
				break;
			}

			default:
				event.reply("Invalid action");
		}
	}
}
