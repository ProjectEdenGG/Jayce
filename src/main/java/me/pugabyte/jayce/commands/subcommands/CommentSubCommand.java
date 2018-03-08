package me.pugabyte.jayce.commands.subcommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;
import me.pugabyte.jayce.utils.InvalidArgumentException;
import me.pugabyte.jayce.utils.Utils;

import java.util.Arrays;

public class CommentSubCommand {
	public static final String USAGE = "comment <id> <comment>";

	public CommentSubCommand(int id, String name, CommandEvent event) throws InvalidArgumentException {
		String[] args = event.getArgs().split(" ");
		String comment = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		if (comment == null || comment.trim().length() < 1) {
			throw new InvalidArgumentException("You need to supply a message for the comment");
		}

		if (comment(id, comment, name)) {
			Utils.reply(event, "Successfully added comment to issue #" + id);
		} else {
			event.reply("Couldn't add comment");
		}
	}

	private static boolean comment(int id, String comment, String name) {
		try {
			Jayce.SERVICES.issues.createComment(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo, id, "**" + name + "**: " + comment);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
