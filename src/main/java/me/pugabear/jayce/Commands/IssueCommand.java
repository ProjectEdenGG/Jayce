package me.pugabear.jayce.Commands;

import me.pugabear.jayce.Commands.SubCommands.*;
import me.pugabear.jayce.Utils.InvalidArgumentException;

import static me.pugabear.jayce.Jayce.CONFIG;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.Arrays;

public class IssueCommand extends Command 
{
	public IssueCommand() 
	{
		this.name = CONFIG.commandName;
		this.aliases = CONFIG.commandAliases;
		if (!CONFIG.requiredRole.isEmpty())
			this.requiredRole = CONFIG.requiredRole;
	}

	protected void execute(CommandEvent event)
	{
		String[] args = event.getArgs().split(" ");

		String name = null;
		try
		{
			if (event.getMember().getNickname().equals("null"))
				name = event.getAuthor().getName();
			else
				name = event.getMember().getNickname();
		}
		catch (NullPointerException ex)
		{
			name = event.getAuthor().getName();
		}

		System.out.println("Processing command by " + name + ": " + event.getArgs());

		try
		{

			int id = 0;
			switch (args[0].toLowerCase())
			{
				case "edit":
				case "close":
				case "open":
				case "assign":
				case "comment":
				{
					try 
					{
						id = Integer.parseInt(args[1]);
					}
					catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) 
					{
						throw new InvalidArgumentException("You didn't supply a valid issue ID!");
					}
					
					if (id < 1)
						throw new InvalidArgumentException("You didn't supply a valid issue ID!");
					
					break;
				}
				case "label":
				case "labels":
				{
					try 
					{
						id = Integer.parseInt(args[1]);
					}
					catch (ArrayIndexOutOfBoundsException ex) {}
					break;
				}
			}
			
			switch (args[0].toLowerCase())
			{
				case "create":
					new CreateSubCommand(name, event);
					break;
		
				case "search":
					new SearchSubCommand(event);
					break;
		
				case "edit":
					new EditSubCommand(id, event);
					break;
		
				case "close":
				case "open":
					new ChangeStateSubCommand(id, event);
					break;
		
				case "assign":
					new AssignSubCommand(id, event);
					break;
		
				case "comment":
					new CommentSubCommand(id, name, event);
					break;
		
				case "label": 
				case "labels":
					String action = null;
					String[] labels = null;
					if (id == 0)
						action = "get";
					else
						try
						{
							action = args[2].toLowerCase();
							labels = Arrays.copyOfRange(args, 3, args.length);
						}
						catch (ArrayIndexOutOfBoundsException ex) {}
					new LabelSubCommand(id, action, labels, event);
					break;
	
				default:
					if (args[0].trim().length() < 1)
						event.reply("<https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues>");
					else
						event.reply("Invalid action");
					break;
			}
		}
		catch (InvalidArgumentException ex)
		{
			event.reply(ex.getMessage());
			return;
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			ex.printStackTrace();
			event.reply("Error trying to parse arguments");
		}
	}
}
