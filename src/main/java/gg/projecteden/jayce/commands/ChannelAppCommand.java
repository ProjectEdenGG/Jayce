package gg.projecteden.jayce.commands;

import com.spotify.github.v3.issues.ImmutableIssue;
import com.spotify.github.v3.issues.Issue;
import gg.projecteden.jayce.commands.common.AppCommand;
import gg.projecteden.jayce.commands.common.AppCommandEvent;
import gg.projecteden.jayce.commands.common.annotations.Command;
import gg.projecteden.jayce.commands.common.annotations.Desc;
import gg.projecteden.jayce.commands.common.annotations.Role;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.models.scheduledjobs.jobs.SupportChannelArchiveJob;
import gg.projecteden.models.scheduledjobs.ScheduledJobsService;
import gg.projecteden.models.scheduledjobs.common.AbstractJob.JobStatus;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static gg.projecteden.utils.StringUtils.camelCase;
import static java.time.LocalDateTime.now;

@Role("Staff")
@Command("Manage support channels")
public class ChannelAppCommand extends AppCommand {

	public ChannelAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Command("Mark this channel as resolved")
	void resolve() {
		issues().assign(getIssueId(), member()).thenRun(() -> {
			new SupportChannelArchiveJob(channel()).schedule(now().plusDays(1));
			reply("This channel has been marked as **resolved** and will be archived in 24 hours");
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return null;
		});
	}

	@Command("Mark this channel as unresolved and cancel archival")
	void unresolve() {
		new ScheduledJobsService().editApp(jobs -> {
			final List<SupportChannelArchiveJob> archiveJobs = jobs
				.get(JobStatus.PENDING).stream()
				.map(job -> job instanceof SupportChannelArchiveJob archiveJob ? archiveJob : null)
				.filter(Objects::nonNull)
				.filter(job -> job.getGuildId().equals(guild().getId()))
				.filter(job -> job.getChannelId().equals(channel().getId()))
				.toList();

			new ArrayList<>(archiveJobs).forEach(job -> job.setStatus(JobStatus.CANCELLED));
		});

		CompletableFuture<Issue> unassign = issues().edit(getIssueId(), ImmutableIssue::withAssignees);

		unassign.thenRun(() -> {
			reply("This channel has been marked as **unresolved**, archival cancelled");
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return null;
		});
	}

	@Command("Transfer this issue to another repository")
	private void transfer(@Desc("destination repository") String repo) {
		final int newId = issues().transfer(getIssueId(), repo);

		final Category newCategory = category(repo);
		final String newUrl = Repos.repo(newCategory).issues().url(newId).build();

		final String channelId = channel().getId();
		final Supplier<TextChannel> channel = () -> guild().getTextChannelById(channelId);
		final Supplier<ChannelManager> manager = () -> channel.get().getManager();

		manager.get()
			.setName(repo.toLowerCase() + "-" + newId)
			.setParent(newCategory)
			.setTopic(newUrl)
			.sync()
			.submit()
			.thenRun(() -> reply("Transferred to **" + camelCase(repo) + "**"))
			.exceptionally(ex -> {
				ex.printStackTrace();
				reply("Could not automatically update channel, please complete manually");
				return null;
			});
	}

}
