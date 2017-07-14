package me.pugabear.jayce.Commands.SubCommands;

import me.pugabear.jayce.Utils.InvalidArgumentException;
import me.pugabear.jayce.Utils.Utils;

import static me.pugabear.jayce.Jayce.CONFIG;
import static me.pugabear.jayce.Jayce.SERVICES;

import java.util.Arrays;

import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class CommentSubCommand
{
	public static final String USAGE = "comment <id> <comment>";

	public CommentSubCommand(int id, String name, CommandEvent event) throws InvalidArgumentException
	{
		String comment =  String.join(" ", Arrays.copyOfRange(event.getArgs().split(" "), 2, event.getArgs().split(" ").length));
		if (comment == null || comment.trim().length() < 1)
			throw new InvalidArgumentException("You need to supply a message for the comment");
		
		if (comment(id, comment, name))
			Utils.reply(event, "Successfully added comment to issue #" + id);
		else		
			event.reply("Couldn't add comment");
	}

	private static boolean comment(int id, String comment, String name)
	{
		try {
			SERVICES.issues.createComment(CONFIG.githubUser, CONFIG.githubRepo, id, "**" + name + "**: " + comment);
			return true;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return false;
		}
	}
}
