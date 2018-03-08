package me.pugabyte.jayce.commands.subcommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;
import net.dv8tion.jda.core.EmbedBuilder;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchIssue;

import java.io.IOException;
import java.util.List;

public class SearchSubCommand {
	public static final String USAGE = "search <query>";

	public SearchSubCommand(CommandEvent event) {
		String query = event.getArgs().replaceFirst("search ", "");
		boolean state = !event.getArgs().contains(" is:closed");
		List<SearchIssue> results = search(query, state);

		if (results != null) {
			StringBuilder body = new StringBuilder();
			String url = "https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues";

			for (SearchIssue issue : results) {
				body.append("#" + issue.getNumber() + ": " + "[" + issue.getTitle() + "]"
						+ "(" + url + "/" + issue.getNumber() + ") " + " - " + issue.getUser()
						+ System.lineSeparator() + System.lineSeparator());
			}

			event.reply(new EmbedBuilder()
					.setAuthor("Found " + results.size() + " issue" + (results.size() != 1 ? "s" : ""), url, Jayce.CONFIG.iconUrl)
					.setDescription(body)
					.build());
		} else {
			event.reply("No results found");
		}
	}

	private static List<SearchIssue> search(String query, boolean state) {
		try {
			Repository repo = Jayce.SERVICES.repos.getRepository(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo);
			return Jayce.SERVICES.issues.searchIssues(repo, (state ? "open" : "closed"), query);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
