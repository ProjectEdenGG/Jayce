package gg.projecteden.jayce.commands;

import com.spotify.github.v3.issues.ImmutableLabel;
import com.spotify.github.v3.issues.Issue;
import com.spotify.github.v3.issues.Label;
import com.spotify.github.v3.search.SearchIssue;
import gg.projecteden.api.common.utils.Env;
import gg.projecteden.api.common.utils.Nullables;
import gg.projecteden.api.common.utils.StringUtils;
import gg.projecteden.api.discord.appcommands.AppCommandEvent;
import gg.projecteden.api.discord.appcommands.annotations.Choices;
import gg.projecteden.api.discord.appcommands.annotations.Command;
import gg.projecteden.api.discord.appcommands.annotations.Desc;
import gg.projecteden.api.discord.appcommands.annotations.GuildCommand;
import gg.projecteden.api.discord.appcommands.annotations.RequiredRole;
import gg.projecteden.api.discord.appcommands.exceptions.AppCommandException;
import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.commands.common.JayceAppCommand;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static gg.projecteden.api.common.utils.StringUtils.ellipsis;
import static gg.projecteden.api.discord.appcommands.AppCommandRegistry.registerConverter;
import static gg.projecteden.api.discord.appcommands.AppCommandRegistry.supplyChoices;
import static java.util.Objects.requireNonNull;

@GuildCommand
@RequiredRole("Staff")
@Command("Interact with GitHub issues")
public class IssuesAppCommand extends JayceAppCommand {
	private final RepoContext repo = Repos.main();
	private final RepoIssueContext issues = repo.issues();

	public IssuesAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Command("Create an issue")
	void create(String title, String body) {
		issues.create(issues.of(member(), title, body).build()).thenAccept(result ->
			replyEphemeral(issues.url(result).embed(false).build()));
	}

	@Command("Open an existing issue")
	void open(@Desc("issue number") int id) {
		issues.open(id).thenRun(this::thumbsup);
	}

	@Command("Close an issue")
	void close(@Desc("issue number") int id) {
		issues.close(id).thenRun(this::thumbsup);
	}

	@Command("Add a user to an issue's assignees")
	void assign(@Desc("issue number") int id, Member user) {
		issues.assign(id, user).thenRun(this::thumbsup);
	}

	@Command("Remove a user from an issue's assignees")
	void unassign(@Desc("issue number") int id, Member user) {
		issues.unassign(id, user).thenRun(this::thumbsup);
	}

	@Command("Edit an issue's title")
	void edit_title(@Desc("issue number") int id, String text) {
		issues.edit(id, issue -> issue.withTitle(text)).thenRun(this::thumbsup);
	}

	@Command("Edit an issue's body")
	void edit_body(@Desc("issue number") int id, String text) {
		issues.edit(id, issue -> issue.withBody(Optional.of("**" + name() + "**: " + text))).thenRun(this::thumbsup);
	}

	@Command("Comment on an issue")
	void comment(@Desc("issue number") int id, String text) {
		issues.comment(id, "**" + name() + "**: " + text).thenRun(this::thumbsup);
	}

	@Command("Add a label to an issue")
	void labels_add(@Desc("issue number") int id, @Choices(Label.class) String label) {
		issues.addLabels(id, List.of(label)).thenRun(this::thumbsup);
	}

	@Command("Remove a label from an issue")
	void labels_remove(@Desc("issue number") int id, @Choices(Label.class) String label) {
		issues.removeLabels(id, List.of(label)).thenRun(this::thumbsup);
	}

	@Command("Search existing issues")
	void search(String query) {
		issues.search(query).thenAccept(items -> {
			if (Nullables.isNullOrEmpty(items)) {
				reply("No results found");
				return;
			}

			final String title = "Found " + items.size() + StringUtils.plural(" issue", items.size());
			final String url = issues.url().build();
			final StringBuilder body = new StringBuilder();

			for (SearchIssue issue : items.subList(0, Math.min(10, items.size())))
				body.append(String.format("#%d [%s](%s) - %s%s", issue.number(), ellipsis(issue.title(), 50),
					url + issue.number(), issue.user().login(), System.lineSeparator() + System.lineSeparator()));

			final EmbedBuilder embed = new EmbedBuilder()
				.setAuthor(title, url, Config.ICON_URL)
				.setDescription(body.toString());

			reply(embed);
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return null;
		});
	}

	@Command("Close all issues in a repository")
	void closeAll(String repo) {
		if (Jayce.get().getEnv() != Env.DEV)
			throw new AppCommandException("Development environment only command");

		final RepoIssueContext issues = Repos.repo(repo).issues();
		issues.listAll().thenAccept(allIssues -> {
			for (Issue issue : allIssues)
				issues.close(requireNonNull(issue.number()));
		});
	}

	static {
		registerConverter(Label.class, input -> ImmutableLabel.builder().name(input.getInput()).build());

		supplyChoices(Label.class, () -> {
			try {
				return Repos.main().listLabels().get().stream()
					.map(Label::name)
					.filter(Objects::nonNull)
					.map(String::toLowerCase)
					.map(label -> new Choice(label, label))
					.toList()
					.subList(0, 25);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		});
	}

}
