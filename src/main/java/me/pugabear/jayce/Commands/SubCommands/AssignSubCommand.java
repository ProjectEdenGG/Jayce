package me.pugabear.jayce.Commands.SubCommands;

import me.pugabear.jayce.Utils.InvalidArgumentException;

import static me.pugabear.jayce.Jayce.ALIASES;
import static me.pugabear.jayce.Jayce.CONFIG;
import static me.pugabear.jayce.Jayce.SERVICES;

import org.eclipse.egit.github.core.Issue;

import net.dv8tion.jda.core.entities.User;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.ArrayList;
import java.util.List;

public class AssignSubCommand
{
	public static final String USAGE = "assign <id> <@users>";
	
	public AssignSubCommand(int id, CommandEvent event) throws InvalidArgumentException 
	{
		if (event.getMessage().getMentionedUsers().size() == 0)
			throw new InvalidArgumentException("You didn't supply a user to assign to the issue");
		
		List<String> userIds = new ArrayList<String>();
		for (User user : event.getMessage().getMentionedUsers())
			userIds.add(user.getId());
		
		if (assign(id, userIds))
			event.reply(":thumbsup:");
		else
			event.reply("Couldn't assign users to issue");
	}
	
	public boolean assign(int id, List<String> userIds)
	{
		try 
		{
			Issue issue = SERVICES.issues.getIssue(CONFIG.githubUser, CONFIG.githubRepo, id);
			for (String userId : userIds)
				issue.setAssignee(SERVICES.users.getUser(ALIASES.aliases.get(userId)));
			SERVICES.issues.editIssue(CONFIG.githubUser, CONFIG.githubRepo, issue);

			return true;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return false;
		}
	}
}
