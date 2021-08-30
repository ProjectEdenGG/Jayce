package gg.projecteden.jayce.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import com.spotify.github.v3.issues.Label;
import com.spotify.github.v3.search.SearchIssue;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.commands.common.CommandEvent;
import gg.projecteden.jayce.config.Aliases;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.IssueField;
import gg.projecteden.jayce.github.Issues.IssueState;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static gg.projecteden.utils.StringUtils.ellipsis;
import static gg.projecteden.utils.Utils.isNullOrEmpty;
import static java.util.stream.Collectors.joining;

public class IssueCommand {
	private final RepoContext repo = Repos.main();
	private final RepoIssueContext issues = repo.issues();

	@CommandMethod("issue|issues create <input>")
	private void create(CommandEvent event, @Argument("input") @Greedy String input) {
		final String[] content = event.parseMentions(input).split("( \\|[ \n])", 2);

		final String title = content[0];
		final String body = content.length > 1 ? content[1] : null;

		issues.create(issues.of(event.getMember(), title, body).build()).thenAccept(result -> {
			if (!event.isWebhookChannel())
				event.reply(issues.url(result).embed(false).build());
		});
	}

	@CommandMethod("issue|issues assign <issueId> <user>")
	private void assign(CommandEvent event, @Argument("issueId") int issueId, @Argument("user") @Greedy String[] members) {
		issues.assign(issueId, Aliases.githubOf(event.getMessage().getMentionedMembers())).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues open <issueId>")
	private void open(CommandEvent event, @Argument("issueId") int issueId) {
		issues.edit(issueId, IssueState.OPEN::set).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues close <issueId>")
	private void close(CommandEvent event, @Argument("issueId") int issueId) {
		issues.edit(issueId, IssueState.CLOSED::set).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues edit <issueId> <field> <text>")
	private void edit(CommandEvent event, @Argument("issueId") int issueId, @Argument("field") IssueField field, @Argument("text") @Greedy String text) {
		issues.edit(issueId, issue -> field.edit(issue, event.parseMentions(text))).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues comment <issueId> <text>")
	private void comment(CommandEvent event, @Argument("issueId") int issueId, @Argument("text") @Greedy String text) {
		issues.comment(issueId, "**" + event.getMemberName() + "**: " + event.parseMentions(text)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues label|labels")
	private void labels(CommandEvent event) {
		repo.listLabels().thenAccept(labels -> event.reply("Available labels: " + labels.stream().map(Label::name).collect(joining(", "))));
	}

	@CommandMethod("issue|issues label|labels add <issueId> <labels>")
	private void labelsAdd(CommandEvent event, @Argument("issueId") int issueId, @Argument("labels") @Greedy String labels) {
		issues.addLabels(issueId, labelsOf(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues label|labels remove <issueId> <labels>")
	private void labelsRemove(CommandEvent event, @Argument("issueId") int issueId, @Argument("labels") @Greedy String labels) {
		issues.removeLabels(issueId, labelsOf(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues search <query>")
	private void search(CommandEvent event, @Argument("query") @Greedy String query) {
		issues.search(event.parseMentions(query)).thenAccept(items -> {
			if (isNullOrEmpty(items))
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

			event.reply(embed);
		});
	}

	private List<String> labelsOf(String input) {
		return List.of(input.split("(?<!:) "));
	}

}
