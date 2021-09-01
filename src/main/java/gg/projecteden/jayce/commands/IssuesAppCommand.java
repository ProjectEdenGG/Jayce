package gg.projecteden.jayce.commands;

import com.spotify.github.v3.issues.Issue;
import com.spotify.github.v3.search.SearchIssue;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.commands.common.AppCommand;
import gg.projecteden.jayce.commands.common.AppCommandEvent;
import gg.projecteden.jayce.commands.common.annotations.Desc;
import gg.projecteden.jayce.commands.common.annotations.Path;
import gg.projecteden.jayce.commands.common.annotations.Role;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.utils.Env;
import gg.projecteden.utils.StringUtils;
import gg.projecteden.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static gg.projecteden.utils.StringUtils.ellipsis;

@Role("Staff")
@Desc("Interact with GitHub issues")
public class IssuesAppCommand extends AppCommand {
	private final RepoContext repo = Repos.main();
	private final RepoIssueContext issues = repo.issues();

	public IssuesAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Desc("Create an issue")
	@Path("create <title> <body>")
	void create(@Desc("issue title") String title, @Desc("issue body") String body) {
		issues.create(issues.of(member(), title, body).build()).thenAccept(result -> {
			if (!isWebhookChannel())
				reply(issues.url(result).embed(false).build());
		});
	}

	@Desc("Open an existing issue")
	@Path("open <id>")
	void open(@Desc("issue number") int id) {
		issues.open(id).thenRun(this::thumbsup);
	}

	@Desc("Close an issue")
	@Path("close <id>")
	void close(@Desc("issue number") int id) {
		issues.close(id).thenRun(this::thumbsup);
	}

	@Desc("Add a user to an issue's assignees")
	@Path("assign <id> <user>")
	void assign(@Desc("issue number") int id, @Desc("user") Member user) {
		issues.assign(id, user).thenRun(this::thumbsup);
	}

	@Desc("Remove a user from an issue's assignees")
	@Path("unassign <id> <user>")
	void unassign(@Desc("issue number") int id, @Desc("user") Member user) {
		issues.unassign(id, user).thenRun(this::thumbsup);
	}

	@Desc("Edit an issue's title")
	@Path("edit title <id> <text>")
	void edit_title(@Desc("issue number") int id, @Desc("issue title") String text) {
		issues.edit(id, issue -> issue.withTitle(text)).thenRun(this::thumbsup);
	}

	@Desc("Edit an issue's body")
	@Path("edit body <id> <text>")
	void edit_body(@Desc("issue number") int id, @Desc("issue body") String text) {
		issues.edit(id, issue -> issue.withBody(Optional.of(text))).thenRun(this::thumbsup);
	}

	@Desc("Comment on an issue")
	@Path("comment <id> <text>")
	void comment(@Desc("issue number") int id, @Desc("comment body") String text) {
		issues.comment(id, "**" + name() + "**: " + text).thenRun(this::thumbsup);
	}

	@Desc("Add a label to an issue")
	@Path("labels add <id> <label>")
	void labels_add(@Desc("issue number") int id, @Desc("label") String label) {
		issues.addLabels(id, List.of(label)).thenRun(this::thumbsup);
	}

	@Desc("Remove a label from an issue")
	@Path("labels remove <id> <label>")
	void labels_remove(@Desc("issue number") int id, @Desc("label") String label) {
		issues.removeLabels(id, List.of(label)).thenRun(this::thumbsup);
	}

	@Desc("Search existing issues")
	@Path("search <query>")
	void search(@Desc("query") String query) {
		issues.search(query).thenAccept(items -> {
			if (Utils.isNullOrEmpty(items))
				throw new EdenException("No results found");

			final String title = "Found " + items.size() + StringUtils.plural(" issue", items.size());
			final String url = issues.url().build();
			final StringBuilder body = new StringBuilder();

			for (SearchIssue issue : items)
				body.append(String.format("#%d [%s](%s) - %s%s", issue.number(), ellipsis(issue.title(), 50),
					url + issue.number(), issue.user().login(), System.lineSeparator() + System.lineSeparator()));

			final EmbedBuilder embed = new EmbedBuilder()
				.setAuthor(title, url, Config.ICON_URL)
				.setDescription(body.toString());

			reply(embed);
		});
	}

	@Desc("Close all issues in a repository")
	@Path("closeall <repo>")
	void closeAll(@Desc("repo") String repo) {
		if (Jayce.get().getEnv() != Env.DEV)
			throw new EdenException("Development environment only command");

		final RepoIssueContext issues = Repos.repo(repo).issues();
		issues.listAll().thenAccept(allIssues -> {
			for (Issue issue : allIssues)
				issues.close(Objects.requireNonNull(issue.number()));
		});
	}

}
