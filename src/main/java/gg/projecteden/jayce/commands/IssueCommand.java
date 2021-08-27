package gg.projecteden.jayce.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.commands.common.CommandEvent;
import gg.projecteden.jayce.services.Issues.IssueField;
import gg.projecteden.jayce.services.Issues.IssueState;
import gg.projecteden.jayce.services.Repos;
import gg.projecteden.jayce.utils.Config;
import gg.projecteden.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.SearchIssue;

import java.util.List;

import static gg.projecteden.utils.StringUtils.ellipsis;
import static gg.projecteden.utils.Utils.isNullOrEmpty;
import static java.util.stream.Collectors.joining;

public class IssueCommand {

	@CommandMethod("issue|issues create <input>")
	private void create(CommandEvent event, @Argument("input") @Greedy String input) {
		final String[] content = input.split("( \\|[ \n])", 2);

		final Issue issue = new Issue().setTitle(content[0]);
		if (content.length > 1)
			issue.setBody("**" + event.getName() + "**: " + content[1]);
		else
			issue.setBody("Submitted by **" + event.getName() + "**");

		Repos.main().issues().create(issue).thenAccept(result -> {
			if (!event.isWebhookChannel())
				event.reply(Repos.main().issues().url(result).embed(false).get());
		});
	}

	@CommandMethod("issue|issues assign <issueId> <user>")
	private void assign(CommandEvent event, @Argument("issueId") int issueId, @Argument("user") String user) {
		final List<User> mentionedUsers = event.getMessage().getMentionedUsers();
		if (mentionedUsers.size() == 0)
			throw new EdenException("You must mention the user to assign to the issue");

		Repos.main().issues().assign(issueId, mentionedUsers.iterator().next().getId()).thenRun(event::thumbsup);
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
		Repos.main().labels().getAll().thenAccept(labels ->
			event.reply("Available labels: " + labels.stream().map(Label::getName).collect(joining(", "))));
	}

	@CommandMethod("issue|issues label|labels add <issueId> <labels>")
	private void labelsAdd(CommandEvent event, @Argument("issueId") int issueId, @Argument("labels") @Greedy String[] labels) {
		Repos.main().labels().add(issueId, List.of(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues label|labels remove <issueId> <labels>")
	private void labelsRemove(CommandEvent event, @Argument("issueId") int issueId, @Argument("labels") @Greedy String[] labels) {
		Repos.main().labels().remove(issueId, List.of(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues search <query>")
	private void search(CommandEvent event, @Argument("query") @Greedy String query) {
		Repos.main().issues().search(query).thenAccept(results -> {
			if (isNullOrEmpty(results))
				throw new EdenException("No results found");

			final String title = "Found " + results.size() + StringUtils.plural(" issue", results.size());
			final String url = Repos.main().issues().url().get();
			final StringBuilder body = new StringBuilder();

			for (SearchIssue issue : results)
				body.append(String.format("#%d [%s](%s) - %s%s", issue.getNumber(), ellipsis(issue.getTitle(), 50),
					url + issue.getNumber(), issue.getUser(), System.lineSeparator() + System.lineSeparator()));

			final EmbedBuilder embed = new EmbedBuilder()
				.setAuthor(title, url, Config.ICON_URL)
				.setDescription(body.toString());

			event.reply(embed);
		});
	}

}
