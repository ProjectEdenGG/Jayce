package me.pugabear.gitkoda.commands;

import me.pugabear.gitkoda.managers.IssueManager;
import me.pugabear.gitkoda.managers.LabelManager;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.commandclient.Command;

import java.util.Arrays;


public class IssueCommand extends Command 
{
	public IssueCommand() 
	{
		this.name = "issue";
		this.requiredRole = "Staff";
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

		event.getJDA().getSelfUser().getManager().setName("GitKoda").queue();
		switch (args[0].toLowerCase())
		{
			case "create":
			{
				String[] content = event.getArgs().split(" ", 2)[1].split("( \\| )", 2);
				int id = 0;
				try
				{
					id = IssueManager.createIssue(content[0], content[1], name);
				} 
				catch (ArrayIndexOutOfBoundsException ex) 
				{
					id = IssueManager.createIssue(content[0], "", event.getMember().getNickname());
				}
	
				if (id != 0) event.reply("https://github.com/PugaBear/GitKodaTest/issues/" + id);
				else event.reply("Issue creation failed");
				break;
			}
			
			case "edit":
			{
				String id = args[1];
				String what = args[2].toLowerCase(); 
				String content =  String.join(" ", Arrays.copyOfRange(args, 3, args.length));
				
				if (IssueManager.editIssue(id, what, content))
				{
					event.reply("Issue updated");
				}
				else 
				{
					event.reply("Could not edit issue");
				}
				break;
			}
			
			case "close":
			{
				if (IssueManager.closeIssue(args[1]))
				{
					event.reply("Closed issue");
				}
				else
				{
					event.reply("Could not close issue");
				}
				break;
			}
			
			case "open":
			{
				if (IssueManager.openIssue(args[1]))
				{
					event.reply("Opened issue");
				}
				else
				{
					event.reply("Could not open issue");
				}
				break;
			}
			
			case "label": case "labels":
			{
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
					event.reply("Successfully " + (action.equalsIgnoreCase("add") ? "added" : "removed") + " label" + (labels.length > 1 ? "s" : ""));
				}
				else
				{
					event.reply("Could not modify labels");
				}
				break;
			}
			
			case "assign":
			{
				String id = args[1];
				String user = args[2];
				if (IssueManager.assign(id, user))
				{
					event.reply("Successfully assigned " + user + " to issue #" + id);
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
				if (IssueManager.comment(id, comment, event.getMember().getNickname()))
				{
					event.reply("Successfully added comment to issue #" + id);
				}
				else
				{
					event.reply("Couldn't add comment");
				}
				break;
			}
			
			default:
				event.reply("Invalid action");
		}
	}
}
