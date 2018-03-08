package me.pugabyte.jayce.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;
import me.pugabyte.jayce.commands.subcommands.*;
import me.pugabyte.jayce.utils.InvalidArgumentException;

import java.util.Arrays;

public class IssueCommand extends Command {
	public IssueCommand() {
		this.name = Jayce.CONFIG.commandName;
		this.aliases = Jayce.CONFIG.commandAliases;
		if (!Jayce.CONFIG.requiredRole.isEmpty()) {
			this.requiredRole = Jayce.CONFIG.requiredRole;
		}
	}

	private static String getName(CommandEvent event) {
		try {
			if (event.getMember().getNickname().equals("null")) {
				return event.getAuthor().getName();
			} else {
				return event.getMember().getNickname();
			}
		} catch (NullPointerException ex) {
			return event.getAuthor().getName();
		}
	}

	protected void execute(CommandEvent event) {
		String[] args = event.getArgs().split(" ");
		String name = getName(event);
		int id = 0;

		System.out.println("Processing command by " + name + ": " + event.getArgs());

		try {
			if (args[0].toLowerCase().contains("label")) {
				if (args.length >= 2) {
					try {
						id = Integer.parseInt(args[1]);
					} catch (NumberFormatException ex) {
						throw new InvalidArgumentException("You didn't supply a valid issue ID!");
					}
				}
			} else {
				try {
					id = Integer.parseInt(args[1]);
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
					throw new InvalidArgumentException("You didn't supply a valid issue ID!");
				}

				if (id < 1) {
					throw new InvalidArgumentException("You didn't supply a valid issue ID!");
				}
			}

			switch (args[0].toLowerCase()) {
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
					if (id == 0) {
						action = "get";
					} else {
						if (args.length >= 3) {
							action = args[2].toLowerCase();
							labels = Arrays.copyOfRange(args, 3, args.length);
						}
					}
					new LabelSubCommand(id, action, labels, event);
					break;

				default:
					if (args[0].trim().length() < 1) {
						event.reply("<https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues>");
					} else {
						event.reply("Invalid action");
					}
					break;
			}
		} catch (InvalidArgumentException ex) {
			event.reply(ex.getMessage());
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
			event.reply("Error trying to parse arguments");
		}
	}
}
