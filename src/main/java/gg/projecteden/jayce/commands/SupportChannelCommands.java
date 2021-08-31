package gg.projecteden.jayce.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import gg.projecteden.jayce.commands.common.CommandEvent;
import gg.projecteden.jayce.github.Issues.IssueState;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.listeners.RepoChannelListener.CommentMeta;
import gg.projecteden.jayce.models.scheduledjobs.jobs.SupportChannelDeleteJob;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static gg.projecteden.utils.StringUtils.isNullOrEmpty;
import static gg.projecteden.utils.Utils.bash;
import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;

@CommandPermission("staff")
public class SupportChannelCommands {

	@CommandMethod("close [comment]")
	private void close(CommandEvent event, @Argument("comment") String comment) {
		try {
			final CompletableFuture<?> first;
			if (!isNullOrEmpty(comment))
				first = event.issues().comment(event.getIssueId(), CommentMeta.asComment(event.getMessage()));
			else
				first = CompletableFuture.completedFuture(null);

			first
				.thenRun(() ->
					event.issues().edit(event.getIssueId(), issue ->
						event.issues().addAssignees(issue, event.getMember()).withState(IssueState.CLOSED.name())
					))
				.thenRun(() ->
					event.getTextChannel().getManager().setParent(getCategory(event.getGuild(), "support-archive"))
						.queue(success -> {
							new SupportChannelDeleteJob(event.getTextChannel()).schedule(now().plusDays(3));
							event.thumbsup();
						}, Throwable::printStackTrace)).exceptionally(ex -> {
				ex.printStackTrace();
				return null;
			});
		} catch (Exception ex) {
			event.handleException(ex);
		}
	}

	// Uses `hub` since GitHub's REST API does not support transferring issues (only their GraphQL API does)
	@CommandMethod("transfer|move <repo>")
	private void transfer(CommandEvent event, @Argument("repo") String repo) {
		try {
			final String command = "./transfer-issue " + event.repo().repo() + " " + event.getIssueId() + " " + repo;
			final String result = bash(command);
			final String[] split = result.split("/");
			int newId = Integer.parseInt(split[split.length - 1]);

			final Category newCategory = getCategory(event.getGuild(), repo);
			final TextChannel channel = event.getTextChannel();
			final String channelId = channel.getId();

			Supplier<ChannelManager> manager = () -> requireNonNull(event.getGuild().getTextChannelById(channelId)).getManager();
			manager.get().setName(repo.toLowerCase() + "-" + newId).queue(success ->
				manager.get().setParent(newCategory).queue(success2 ->
					manager.get().setTopic(Repos.repo(newCategory).issues().url(newId).build()).queue(success3 ->
						event.thumbsup())));
		} catch (Exception ex) {
			event.handleException(ex);
		}
	}

	private Category getCategory(Guild guild, String name) {
		return guild.getCategoriesByName(name, true).iterator().next();
	}

}
