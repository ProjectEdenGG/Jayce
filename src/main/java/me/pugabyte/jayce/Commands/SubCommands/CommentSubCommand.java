package me.pugabyte.jayce.Commands.SubCommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;

import java.util.Arrays;

public class CommentSubCommand {
	public static final String USAGE = "comment <id> <comment>";

	public CommentSubCommand(int id, String name, CommandEvent event) throws me.pugabyte.jayce.Utils.InvalidArgumentException {
		String comment = String.join(" ", Arrays.copyOfRange(event.getArgs().split(" "), 2, event.getArgs().split(" ").length));
		if (comment == null || comment.trim().length() < 1)
			throw new me.pugabyte.jayce.Utils.InvalidArgumentException("You need to supply a message for the comment");

		if (comment(id, comment, name))
			me.pugabyte.jayce.Utils.Utils.reply(event, "Successfully added comment to issue #" + id);
		else
			event.reply("Couldn't add comment");
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
