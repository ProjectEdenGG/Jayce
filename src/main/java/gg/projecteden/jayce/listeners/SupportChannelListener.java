package gg.projecteden.jayce.listeners;

import com.spotify.github.v3.issues.ImmutableIssue;
import com.spotify.github.v3.issues.ImmutableLabel;
import com.spotify.github.v3.issues.Issue;
import gg.projecteden.annotations.Environments;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.listeners.common.DiscordListener;
import gg.projecteden.utils.Env;
import gg.projecteden.utils.StringUtils;
import gg.projecteden.utils.Tasks;
import gg.projecteden.utils.TimeUtils.MillisTime;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static gg.projecteden.utils.StringUtils.ellipsis;

@Environments(Env.DEV)
public class SupportChannelListener extends DiscordListener {
	private static final String TITLE_REGEX = "(?i)^title:( )?";

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		try {
			if (shouldIgnore(event))
				return;
			
			final TextChannel channel = event.getChannel();
			final Category category = channel.getParent();
			final Message message = event.getMessage();
			final Member member = event.getMember();

			if (member == null || category == null)
				return;

			if (!"Support".equalsIgnoreCase(category.getName()))
				return;

			if (message.getContentRaw().startsWith(Config.COMMAND_PREFIX))
				return;

			final RepoContext repo = Repos.repo(channel);
			final RepoIssueContext issues = repo.issues();

			final String content = message.getContentDisplay();
			final ImmutableIssue.Builder builder = getIssue(issues, member, content)
				.addLabels(ImmutableLabel.builder().name(StringUtils.left(channel.getName(), channel.getName().length() - 1)).build());

			issues.create(builder.build()).thenAccept(issue -> {
				final int number = getIssueNumber(issue);
				final Category repoCategory = getRepoCategory(message, repo);

				repoCategory.createTextChannel(repo.repo() + "-" + number).queue(newChannel -> {
					newChannel.sendMessage(member.getAsMention() + ": " + content.replaceFirst(TITLE_REGEX, "")).queue();
					newChannel.getManager().setTopic(issues.url(issue).build()).queue();
					message.delete().queue();
					channel.sendMessage(member.getAsMention() + " " + newChannel.getAsMention()).queue(reply ->
						Tasks.wait(MillisTime.MINUTE, () -> reply.delete().queue()));
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
