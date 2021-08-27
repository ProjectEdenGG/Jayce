package me.pugabyte.jayce.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.utils.StringUtils;
import gg.projecteden.utils.Utils;
import me.pugabyte.jayce.commands.common.CommandEvent;
import me.pugabyte.jayce.services.Issues;
import me.pugabyte.jayce.services.Issues.IssueField;
import me.pugabyte.jayce.services.Issues.IssueState;
import me.pugabyte.jayce.services.Labels;
import me.pugabyte.jayce.utils.Config;
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

		Issues.repo().create(issue).execute().thenAccept(result -> {
			if (!event.getChannel().getId().equals(Config.WEBHOOK_CHANNEL_ID))
				event.getChannel().sendMessage(Issues.repo().url(result.getNumber()).embed(false).execute()).queue();
		});
	}

	@CommandMethod("issue|issues assign <id> <user>")
	private void assign(CommandEvent event, @Argument("id") int id, @Argument("user") String user) {
		final List<User> mentionedUsers = event.getMessage().getMentionedUsers();
		if (mentionedUsers.size() == 0)
			throw new EdenException("You must mention the user to assign to the issue");

		Issues.repo().assign(id, mentionedUsers.iterator().next().getId()).execute();
		event.thumbsup();
	}

	@CommandMethod("issue|issues open <id>")
	private void open(CommandEvent event, @Argument("id") int id) {
		Issues.repo().edit(id, issue -> issue.setState(IssueState.OPEN.name())).execute();
		event.thumbsup();
	}

	@CommandMethod("issue|issues close <id>")
	private void close(CommandEvent event, @Argument("id") int id) {
		Issues.repo().edit(id, issue -> issue.setState(IssueState.CLOSED.name())).execute();
		event.thumbsup();
	}

	@CommandMethod("issue|issues edit <id> <field> <text>")
	private void edit(CommandEvent event, @Argument("id") int id, @Argument("field") IssueField field, @Argument("text") @Greedy String text) {
		Issues.repo().edit(id, issue -> field.edit(issue, text)).execute();
		event.thumbsup();
	}

	@CommandMethod("issue|issues comment <id> <text>")
	private void comment(CommandEvent event, @Argument("id") int id, @Argument("text") @Greedy String text) {
		Issues.repo().comment(id, "**" + event.getMember().getEffectiveName() + "**: " + text).execute();
		event.thumbsup();
	}

	@CommandMethod("issue|issues label|labels")
	private void labels(CommandEvent event) {
		Labels.repo().getAll().execute().thenAccept(labels -> {
			String names = labels.stream().map(Label::getName).collect(Collectors.joining(", "));
			event.getChannel().sendMessage("Valid labels: " + names).queue();
		});
	}

	@CommandMethod("issue|issues label|labels add <id> <labels>")
	private void labelsAdd(CommandEvent event, @Argument("id") int id, @Argument("labels") @Greedy String[] labels) {
		Labels.repo().add(id, List.of(labels)).execute();
		event.thumbsup();
	}

	@CommandMethod("issue|issues label|labels remove <id> <labels>")
	private void labelsRemove(CommandEvent event, @Argument("id") int id, @Argument("labels") @Greedy String[] labels) {
		Labels.repo().remove(id, List.of(labels)).execute();
		event.thumbsup();
	}

	@CommandMethod("issue|issues search <query>")
	private void search(CommandEvent event, @Argument("query") @Greedy String query) {
		Issues.repo().search(query).execute().thenAccept(results -> {
			if (Utils.isNullOrEmpty(results))
				throw new EdenException("No results found idiot");

			String body = "";
			String url = Issues.repo().url().execute();

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
