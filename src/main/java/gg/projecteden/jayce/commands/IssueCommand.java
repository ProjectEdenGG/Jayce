package gg.projecteden.jayce.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import com.spotify.github.v3.issues.ImmutableIssue;
import com.spotify.github.v3.issues.ImmutableIssue.Builder;
import com.spotify.github.v3.issues.Label;
import com.spotify.github.v3.search.SearchIssue;
import com.spotify.github.v3.search.SearchIssues;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.commands.common.CommandEvent;
import gg.projecteden.jayce.services.Issues.IssueField;
import gg.projecteden.jayce.services.Issues.IssueState;
import gg.projecteden.jayce.services.Repos;
import gg.projecteden.jayce.utils.Aliases;
import gg.projecteden.jayce.utils.Config;
import gg.projecteden.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Arrays;
import java.util.Optional;

import static gg.projecteden.utils.StringUtils.ellipsis;
import static gg.projecteden.utils.Utils.isNullOrEmpty;
import static java.util.stream.Collectors.joining;

public class IssueCommand {

	@CommandMethod("issue|issues create <input>")
	private void create(CommandEvent event, @Argument("input") @Greedy String input) {
		final String[] content = input.split("( \\|[ \n])", 2);

		final Builder builder = ImmutableIssue.builder()
			.title(content[0]);

		if (content.length > 1)
			builder.body(Optional.of("**" + event.getName() + "**: " + content[1]));
		else
			builder.body(Optional.of("Submitted by **" + event.getName() + "**"));

		Repos.main().issues().create(builder.build()).thenAccept(result -> {
			if (!event.isWebhookChannel())
				event.reply(Repos.main().issues().url(result).embed(false).get());
		});
	}

	@CommandMethod("issue|issues assign <issueId> <user>")
	private void assign(CommandEvent event, @Argument("issueId") int issueId, @Argument("user") @Greedy String[] members) {
		Repos.main().issues().assign(issueId, Aliases.githubOf(event.getMessage().getMentionedMembers())).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues open <issueId>")
	private void open(CommandEvent event, @Argument("issueId") int issueId) {
		Repos.main().issues().edit(issueId, IssueState.OPEN::set).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues close <issueId>")
	private void close(CommandEvent event, @Argument("issueId") int issueId) {
		Repos.main().issues().edit(issueId, IssueState.CLOSED::set).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues edit <issueId> <field> <text>")
	private void edit(CommandEvent event, @Argument("issueId") int issueId, @Argument("field") IssueField field, @Argument("text") @Greedy String text) {
		Repos.main().issues().edit(issueId, issue -> field.edit(issue, text)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues comment <issueId> <text>")
	private void comment(CommandEvent event, @Argument("issueId") int issueId, @Argument("text") @Greedy String text) {
		Repos.main().issues().comment(issueId, "**" + event.getName() + "**: " + text).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues label|labels")
	private void labels(CommandEvent event) {
		Repos.main().listLabels().thenAccept(labels ->
			event.reply("Available labels: " + labels.stream().map(Label::name).collect(joining(", "))));
	}

	@CommandMethod("issue|issues label|labels add <issueId> <labels>")
	private void labelsAdd(CommandEvent event, @Argument("issueId") int issueId, @Argument("labels") @Greedy String[] labels) {
		Repos.main().issues().addLabels(issueId, Arrays.asList(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues label|labels remove <issueId> <labels>")
	private void labelsRemove(CommandEvent event, @Argument("issueId") int issueId, @Argument("labels") @Greedy String[] labels) {
		Repos.main().issues().removeLabels(issueId, Arrays.asList(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues search <query>")
	private void search(CommandEvent event, @Argument("query") @Greedy String query) {
		Repos.main().issues().search(query).thenApply(SearchIssues::items).thenAccept(items -> {
			if (isNullOrEmpty(items))
				throw new EdenException("No results found");

			final String title = "Found " + items.size() + StringUtils.plural(" issue", items.size());
			final String url = Repos.main().issues().url().get();
			final StringBuilder body = new StringBuilder();

			for (SearchIssue issue : items) {
				body.append(String.format("#%d [%s](%s) - %s%s", issue.number(), ellipsis(issue.title(), 50),
					url + issue.number(), issue.user().login(), System.lineSeparator() + System.lineSeparator()));
			}

			final EmbedBuilder embed = new EmbedBuilder()
				.setAuthor(title, url, Config.ICON_URL)
				.setDescription(body.toString());

			event.reply(embed);
		});
	}

}
