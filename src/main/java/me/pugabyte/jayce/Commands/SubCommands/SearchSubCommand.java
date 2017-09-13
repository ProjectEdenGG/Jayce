package me.pugabyte.jayce.Commands.SubCommands;

import me.pugabyte.jayce.Jayce;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchIssue;

import net.dv8tion.jda.core.EmbedBuilder;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.io.IOException;
import java.util.List;

public class SearchSubCommand {
	public static final String USAGE = "search <query>";

	public SearchSubCommand(CommandEvent event) {
		List<SearchIssue> results = search(event.getArgs().replaceFirst("search ", ""));

		StringBuilder body = new StringBuilder();
		String url = "https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues";

		for (SearchIssue issue : results != null ? results : null)
			body.append("#" + issue.getNumber() + ": " + "[" + issue.getTitle() + "]"
					+ "(" + url + "/" + issue.getNumber() + ") " + " - " + issue.getUser()
					+ System.lineSeparator() + System.lineSeparator());

		event.reply(new EmbedBuilder()
				.setAuthor("Found " + results.size() + " issue" + (results.size() != 1 ? "s" : ""), url, Jayce.CONFIG.iconUrl)
				.setDescription(body)
				.build());
	}

	private static List<SearchIssue> search(String query) {
		try {
			Repository repo = Jayce.SERVICES.repos.getRepository(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo);
			return Jayce.SERVICES.issues.searchIssues(repo, "open", query);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
