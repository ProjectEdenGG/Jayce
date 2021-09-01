package gg.projecteden.jayce.commands;

import cloud.commandframework.annotations.CommandMethod;
import com.spotify.github.v3.search.SearchIssue;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.commands.common.AppCommand;
import gg.projecteden.jayce.commands.common.AppCommandEvent;
import gg.projecteden.jayce.config.Aliases;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.utils.StringUtils;
import gg.projecteden.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.managers.ChannelManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static gg.projecteden.utils.StringUtils.ellipsis;
import static gg.projecteden.utils.Utils.bash;
import static java.util.Objects.requireNonNull;

public class IssuesAppCommand extends AppCommand {
	private final RepoContext repo = Repos.main();
	private final RepoIssueContext issues = repo.issues();

	public IssuesAppCommand(AppCommandEvent event) {
		super(event);
	}

	@CommandMethod("create <input>")
	private void create(String input) {
		final String[] content = input.split("( \\|[ \n])", 2);

		final String title = content[0];
		final String body = content.length > 1 ? content[1] : null;

		issues.create(issues.of(member(), title, body).build()).thenAccept(result -> {
			if (!isWebhookChannel())
				reply(issues.url(result).embed(false).build());
		});
	}

	@CommandMethod("assign <id> <user1> [user2] [user3]")
	private void assign(int id, Member user1, Member user2, Member user3) {
		issues.assign(id, Aliases.githubOf(List.of(user1, user2, user3))).thenRun(this::thumbsup);
	}

	@CommandMethod("unassign <id> <user1> [user2] [user3]")
	private void unassign(int id, Member user1, Member user2, Member user3) {
		issues.unassign(id, Aliases.githubOf(List.of(user1, user2, user3))).thenRun(this::thumbsup);
	}

	@CommandMethod("open <id>")
	private void open(int id) {
		issues.open(id).thenRun(this::thumbsup);
	}

	@CommandMethod("close <id>")
	private void close(int id) {
		issues.close(id).thenRun(this::thumbsup);
	}

	@CommandMethod("edit title <id> <text>")
	private void edit_title(int id, String text) {
		issues.edit(id, issue -> issue.withTitle(text)).thenRun(this::thumbsup);
	}

	@CommandMethod("edit body <id> <text>")
	private void edit_body(int id, String text) {
		issues.edit(id, issue -> issue.withBody(Optional.of(text))).thenRun(this::thumbsup);
	}

	@CommandMethod("comment <id> <text>")
	private void comment(int id, String text) {
		issues.comment(id, "**" + name() + "**: " + text).thenRun(this::thumbsup);
	}

	@CommandMethod("labels add <id> <label1> [label2]")
	private void labels_add(int id, String label1, String label2) {
		issues.addLabels(id, List.of(label1, label2)).thenRun(this::thumbsup);
	}

	@CommandMethod("labels remove <id> <label1> [label2]")
	private void labels_remove(int id, String label1, String label2) {
		issues.removeLabels(id, List.of(label1, label2)).thenRun(this::thumbsup);
	}

	@CommandMethod("search <query>")
	private void search(String query) {
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

	// Uses `hub` since GitHub's REST API does not support transferring issues (only their GraphQL API does)
	@CommandMethod("transfer <repo>")
	private void transfer(String repo) {
		final String command = "./transfer-issue " + repo().repo() + " " + getIssueId() + " " + repo;
		final String result = bash(command);
		final String[] split = result.split("/");
		int newId = Integer.parseInt(split[split.length - 1]);

		final Category newCategory = category(repo);
		final String channelId = channel().getId();

		Supplier<ChannelManager> manager = () -> requireNonNull(guild().getTextChannelById(channelId)).getManager();
		manager.get().setName(repo.toLowerCase() + "-" + newId).queue(success ->
			manager.get().setParent(newCategory).queue(success2 ->
				manager.get().setTopic(Repos.repo(newCategory).issues().url(newId).build()).queue(success3 ->
					thumbsup())));
	}

	/*
	@CommandMethod("issuesissues closeall <repo>")
	private void closeAll(@Argument("repo") String repo) {
		if (Jayce.get().getEnv() != Env.DEV)
			throw new EdenException("Development environment only command");

		final RepoIssueContext issues = Repos.repo(repo).issues();
		issues.listAll().thenAccept(allIssues -> {
			for (Issue issue : allIssues)
				issues.close(Objects.requireNonNull(issue.number()));
		});
	}
	*/

}
