package me.pugabear.gitkoda.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.commandclient.Command;
import me.pugabear.gitkoda.utils.IssueManager;

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
				String what = args[2]; 
				String content = event.getArgs();
				content = content.replace("edit", "");
				content = content.replace(id, "");
				content = content.replace(what, "");
				content = content.trim();
				
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
			default:
				event.reply("Invalid action");
		}
	}
}
