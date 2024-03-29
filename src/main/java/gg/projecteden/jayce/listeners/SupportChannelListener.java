package gg.projecteden.jayce.listeners;

import com.spotify.github.v3.issues.ImmutableIssue;
import com.spotify.github.v3.issues.Issue;
import gg.projecteden.api.common.exceptions.EdenException;
import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.listeners.common.DiscordListener;
import gg.projecteden.jayce.models.scheduledjobs.jobs.MessageDeleteJob;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static gg.projecteden.api.common.utils.StringUtils.ellipsis;
import static java.time.LocalDateTime.now;

public class SupportChannelListener extends DiscordListener {
	private static final String TITLE_REGEX = "(?i)^title:( )?";

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		try {
			if (shouldIgnore(event))
				return;

			if (!(event.getChannel() instanceof TextChannel channel))
				return;

			final Category category = channel.getParentCategory();
			final Message message = event.getMessage();
			final Member member = event.getMember();

			if (member == null || category == null)
				return;

			if (!"Support".equalsIgnoreCase(category.getName()))
				return;

			if ("titan".equalsIgnoreCase(channel.getName()))
				return;

			final RepoContext repo = Repos.repo(channel);
			final RepoIssueContext issues = repo.issues();

			final String content = message.getContentDisplay();

			issues.create(getIssue(issues, member, content).build()).thenAccept(issue -> {
				final int number = getIssueNumber(issue);
				final Category repoCategory = getRepoCategory(message, repo);

				repoCategory.createTextChannel(Jayce.UNRESOLVED + "-" + repo.repo() + "-" + number).queue(newChannel -> {
					newChannel.sendMessage(member.getAsMention() + ": " + content.replaceFirst(TITLE_REGEX, "")).queue();
					newChannel.getManager().setTopic(issues.url(issue).build()).queue();
					message.delete().queue();
					channel.sendMessage(member.getAsMention() + " " + newChannel.getAsMention()).queue(reply ->
						new MessageDeleteJob(reply).schedule(now().plusMinutes(1)));
				}, ex -> handleException(event, ex));
			}).exceptionally(ex -> {
				handleException(event, ex);
				return null;
			});
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

	private ImmutableIssue.Builder getIssue(RepoIssueContext issues, Member member, String content) {
		String title = member.getEffectiveName();
		String body = content;

		if (content.matches(TITLE_REGEX) && content.contains("\n")) {
			String[] split = content.split("\n", 2);
			final String titleText = split[0].replaceFirst(TITLE_REGEX, "");
			title = ellipsis(titleText, 50);
			body = (titleText.equals(title) ? "" : titleText + System.lineSeparator()) + split[1];
		}

		return issues.of(member, title, body);
	}

	private int getIssueNumber(Issue issue) {
		if (issue.number() == null)
			throw new EdenException("Created issue does not have a number");
		return Objects.requireNonNull(issue.number());
	}

	private Category getRepoCategory(Message message, RepoContext repo) {
		final List<Category> categories = message.getGuild().getCategoriesByName(repo.repo(), true);
		if (categories.size() != 1)
			throw new EdenException("Could not determine category for " + repo.repo() + " (Found " + categories.size() + " results)");

		return categories.iterator().next();
	}

}
