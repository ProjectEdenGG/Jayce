package me.pugabear.gitkoda.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.Arrays;

import com.jagrosh.jdautilities.commandclient.Command;
import me.pugabear.gitkoda.managers.IssueManager;
import me.pugabear.gitkoda.managers.LabelManager;

public class IssueCommand extends Command 
{
	public IssueCommand() 
	{
		this.name = "issue";
	}

	protected void execute(CommandEvent event)
	{
		String[] args = event.getArgs().split(" ");
		if (args.length == 1)
		{
			event.reply("Missing arguments");
			return;
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
					id = IssueManager.createIssue(content[0], content[1]);
				} 
				catch (ArrayIndexOutOfBoundsException ex) 
				{
					id = IssueManager.createIssue(content[0], "");
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
			
			case "label": case "labels":
			{
				String action = args[1].toLowerCase();
				String id = args[2];
				String[] labels = Arrays.copyOfRange(args, 3, args.length);
				if (action.equals("add"))
				{
					LabelManager.addLabels(id, labels);
				}
				else if (action.equals("remove"))
				{
					LabelManager.removeLabels(id, labels);
				}
				break;
			}
			default:
				event.reply("Invalid action");
		}
	}
}
