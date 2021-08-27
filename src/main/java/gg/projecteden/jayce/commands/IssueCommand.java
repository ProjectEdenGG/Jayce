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
import gg.projecteden.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.SearchIssue;

import java.util.List;
import java.util.stream.Collectors;

public class IssueCommand {

	@CommandMethod("issue|issues create <input>")
	private void create(CommandEvent event, @Argument("input") @Greedy String input) {
		final String[] content = input.split("( \\|[ \n])", 2);

		final Issue issue = new Issue().setTitle(content[0]);
		if (content.length > 1)
			issue.setBody("**" + event.getMember().getEffectiveName() + "**: " + content[1]);
		else
			issue.setBody("Submitted by **" + event.getMember().getEffectiveName() + "**");

		Repos.main().issues().create(issue).thenAccept(result -> {
			if (!event.getChannel().getId().equals(Config.WEBHOOK_CHANNEL_ID))
				event.getChannel().sendMessage(Repos.main().issues().url(result.getNumber()).embed(false).get()).queue();
		});
	}

	@CommandMethod("issue|issues assign <id> <user>")
	private void assign(CommandEvent event, @Argument("id") int id, @Argument("user") String user) {
		final List<User> mentionedUsers = event.getMessage().getMentionedUsers();
		if (mentionedUsers.size() == 0)
			throw new EdenException("You must mention the user to assign to the issue");

		Repos.main().issues().assign(id, mentionedUsers.iterator().next().getId()).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues open <id>")
	private void open(CommandEvent event, @Argument("id") int id) {
		Repos.main().issues().edit(id, issue -> issue.setState(IssueState.OPEN.name())).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues close <id>")
	private void close(CommandEvent event, @Argument("id") int id) {
		Repos.main().issues().edit(id, issue -> issue.setState(IssueState.CLOSED.name())).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues edit <id> <field> <text>")
	private void edit(CommandEvent event, @Argument("id") int id, @Argument("field") IssueField field, @Argument("text") @Greedy String text) {
		Repos.main().issues().edit(id, issue -> field.edit(issue, text)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues comment <id> <text>")
	private void comment(CommandEvent event, @Argument("id") int id, @Argument("text") @Greedy String text) {
		Repos.main().issues().comment(id, "**" + event.getMember().getEffectiveName() + "**: " + text).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues label|labels")
	private void labels(CommandEvent event) {
		Repos.main().labels().getAll().thenAccept(labels -> {
			String names = labels.stream().map(Label::getName).collect(Collectors.joining(", "));
			event.getChannel().sendMessage("Valid labels: " + names).queue();
		});
	}

	@CommandMethod("issue|issues label|labels add <id> <labels>")
	private void labelsAdd(CommandEvent event, @Argument("id") int id, @Argument("labels") @Greedy String[] labels) {
		Repos.main().labels().add(id, List.of(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues label|labels remove <id> <labels>")
	private void labelsRemove(CommandEvent event, @Argument("id") int id, @Argument("labels") @Greedy String[] labels) {
		Repos.main().labels().remove(id, List.of(labels)).thenRun(event::thumbsup);
	}

	@CommandMethod("issue|issues search <query>")
	private void search(CommandEvent event, @Argument("query") @Greedy String query) {
		Repos.main().issues().search(query).thenAccept(results -> {
			if (Utils.isNullOrEmpty(results))
				throw new EdenException("No results found idiot");

			String body = "";
			String url = Repos.main().issues().url().get();

			for (SearchIssue issue : results)
				body += "#" + issue.getNumber() + ": " + "[" + issue.getTitle() + "]"
					+ "(" + url + issue.getNumber() + ") " + " - " + issue.getUser()
					+ System.lineSeparator() + System.lineSeparator();

			final String title = "Found " + results.size() + StringUtils.plural(" issue", results.size());

			final EmbedBuilder embed = new EmbedBuilder()
				.setAuthor(title, url, Config.ICON_URL)
				.setDescription(body);

			event.reply(embed);
		});
	}

}
