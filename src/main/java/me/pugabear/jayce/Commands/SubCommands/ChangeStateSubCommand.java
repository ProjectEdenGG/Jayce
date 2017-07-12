package me.pugabear.jayce.Commands.SubCommands;

import me.pugabear.jayce.Jayce;
import me.pugabear.jayce.Utils.InvalidArgumentException;
import me.pugabear.jayce.Utils.Utils;

import static me.pugabear.jayce.Jayce.CONFIG;
import static me.pugabear.jayce.Jayce.SERVICES;

import org.eclipse.egit.github.core.Issue;

import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class ChangeStateSubCommand
{
	public static final String USAGE = "<open|close> <id>";
	
	public ChangeStateSubCommand(int id, CommandEvent event) throws InvalidArgumentException 
	{
		String[] args = event.getArgs().split(" ");
		String state;
		try
		{
			state = args[0].toLowerCase();
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			throw new InvalidArgumentException(Jayce.USAGE + USAGE);
		}
		
		if (!(state.equals("open") || state.equals("close")))
			throw new InvalidArgumentException(Jayce.USAGE + USAGE);
		
		if (changeState(id, state))
		{
			Utils.reply(event, (state.equals("open") ? "Opened" : "Closed") + " issue: " 
					+ "<https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues/" + id + ">");
			return;
		}
		
		event.reply("Could not " + state + " issue");
	}
	
	public boolean changeState(int id, String state)
	{
		try 
		{
			Issue issue = SERVICES.issues.getIssue(CONFIG.githubUser, CONFIG.githubRepo, id);
			SERVICES.issues.editIssue(CONFIG.githubUser, CONFIG.githubRepo, issue.setState(state));
			return true;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return false;
		}
	}
}
