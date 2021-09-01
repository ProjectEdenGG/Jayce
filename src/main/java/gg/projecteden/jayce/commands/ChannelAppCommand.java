package gg.projecteden.jayce.commands;

import gg.projecteden.jayce.commands.common.AppCommand;
import gg.projecteden.jayce.commands.common.AppCommandEvent;
import gg.projecteden.jayce.commands.common.annotations.Desc;
import gg.projecteden.jayce.commands.common.annotations.Path;
import gg.projecteden.jayce.commands.common.annotations.Role;
import gg.projecteden.jayce.github.Issues.IssueState;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.listeners.RepoChannelListener.CommentMeta;
import gg.projecteden.jayce.models.scheduledjobs.jobs.SupportChannelDeleteJob;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.managers.ChannelManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static gg.projecteden.utils.StringUtils.isNullOrEmpty;
import static gg.projecteden.utils.Utils.bash;
import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;

@Role("Staff")
@Desc("Manage support channels")
public class ChannelAppCommand extends AppCommand {

	public ChannelAppCommand(AppCommandEvent event) {
		super(event);
	}

	// TODO
	@Desc("Mark this channel as resolved")
	@Path("resolve [comment]")
	void resolve(@Desc("Resolution") String comment) {
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

	// Uses `hub` since GitHub's REST API does not support transferring issues (only their GraphQL API does)
	@Desc("Transfer this issue to another repository")
	@Path("transfer <repo>")
	private void transfer(@Desc("Destination repository") String repo) {
		final String command = "./transfer-issue " + repo().repo() + " " + getIssueId() + " " + repo;
		final String result = bash(command);
		final String[] split = result.split("/");
		int newId = Integer.parseInt(split[split.length - 1]);

		final Category newCategory = category(repo);
		final String channelId = channel().getId();

		Supplier<ChannelManager> manager = () -> requireNonNull(guild().getTextChannelById(channelId)).getManager();
		manager.get().setName(repo.toLowerCase() + "-" + newId).queue(success ->
			manager.get().setParent(newCategory).queue(success2 ->
				manager.get().setTopic(Repos.repo(newCategory).issues().url(newId).build()).queue(success3 ->
					thumbsup())));
	}

}
