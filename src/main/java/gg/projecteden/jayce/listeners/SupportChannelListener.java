package gg.projecteden.jayce.listeners;

import com.spotify.github.v3.issues.Issue;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.listeners.common.DiscordListener;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class SupportChannelListener extends DiscordListener {

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		try {
			final TextChannel channel = event.getChannel();
			final Category category = channel.getParent();
			final Message message = event.getMessage();
			final Member member = event.getMember();

			if (member == null || category == null)
				return;

			if (member.getUser().isBot())
				return;

			if (shouldIgnore(event))
				return;

			if (!"Support".equalsIgnoreCase(category.getName()))
				return;

			final RepoContext repo = Repos.repo(channel.getName());
			final RepoIssueContext issues = repo.issues();
			issues.create(member, member.getEffectiveName(), message.getContentDisplay()).thenAccept(issue -> {
				final int number = getIssueNumber(issue);
				final Category repoCategory = getRepoCategory(message, repo);

				repoCategory.createTextChannel(repo.repo() + "-" + number).queue(newChannel -> {
					newChannel.sendMessage("**" + member.getEffectiveName() + "**: " + message.getContentDisplay()).queue();
					newChannel.getManager().setTopic(issues.url(issue).build()).queue();
					message.delete().queue();
					channel.sendMessage(member.getAsMention() + " " + newChannel.getAsMention()).queue();
				});
			}).exceptionally(ex -> {
				handleException(event, ex);
				return null;
			});
		} catch (Exception ex) {
			handleException(event, ex);
		}
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
