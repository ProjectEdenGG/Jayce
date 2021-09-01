package gg.projecteden.jayce.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import gg.projecteden.jayce.commands.common.AppCommand;
import gg.projecteden.jayce.commands.common.AppCommandEvent;
import gg.projecteden.jayce.github.Issues.IssueState;
import gg.projecteden.jayce.listeners.RepoChannelListener.CommentMeta;
import gg.projecteden.jayce.models.scheduledjobs.jobs.SupportChannelDeleteJob;

import java.util.concurrent.CompletableFuture;

import static gg.projecteden.utils.StringUtils.isNullOrEmpty;
import static java.time.LocalDateTime.now;

@CommandPermission("staff")
public class ChannelAppCommand extends AppCommand {

	public ChannelAppCommand(AppCommandEvent event) {
		super(event);
	}

	@CommandMethod("close [comment]")
	void close(String comment) {
		final CompletableFuture<?> first;
		if (!isNullOrEmpty(comment))
			first = issues().comment(getIssueId(), CommentMeta.asComment(event.getEvent().getId(), member(), comment));
		else
			first = CompletableFuture.completedFuture(null);

		Runnable assign = () -> issues().edit(getIssueId(), issue ->
			issues().addAssignees(issue, member()).withState(IssueState.CLOSED.name()));

		Runnable move = () -> channel().getManager().setParent(category("support-archive")).submit().thenRun(() -> {
			new SupportChannelDeleteJob(channel()).schedule(now().plusDays(3));
			thumbsup();
		});

		first
			.thenRun(assign)
			.thenRun(move)
			.exceptionally(ex -> {
				ex.printStackTrace();
				return null;
			});
	}

}
