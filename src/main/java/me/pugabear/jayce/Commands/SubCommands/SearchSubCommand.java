package me.pugabear.jayce.Commands.SubCommands;

import me.pugabear.jayce.Utils.InvalidArgumentException;

import static me.pugabear.jayce.Jayce.CONFIG;
import static me.pugabear.jayce.Jayce.SERVICES;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchIssue;

import net.dv8tion.jda.core.EmbedBuilder;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.io.IOException;
import java.util.List;

public class SearchSubCommand
{
	public static final String USAGE = "search <query>";

	public SearchSubCommand(CommandEvent event) throws InvalidArgumentException 
	{
		List<SearchIssue> results = search(event.getArgs().replaceFirst("search ", ""));
		
		String body = "";
		String url = "https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues/";
		
		for (SearchIssue issue : results)
			body += "#" + issue.getNumber() + ": " + "[" + issue.getTitle() + "]" 
						+ "(" + url + issue.getNumber() + ") " + " - " + issue.getUser()
						+ System.lineSeparator() + System.lineSeparator();

		event.reply(new EmbedBuilder()
				.setAuthor("Found " + results.size() + " issue" + (results.size() != 1 ? "s" : ""), 
						"https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues", CONFIG.iconUrl)
				.setDescription(body)
				.build());
	}

	public static List<SearchIssue> search(String query)
	{
		try
		{
			Repository repo = SERVICES.repos.getRepository(CONFIG.githubUser, CONFIG.githubRepo);
			return SERVICES.issues.searchIssues(repo, "open", query);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
