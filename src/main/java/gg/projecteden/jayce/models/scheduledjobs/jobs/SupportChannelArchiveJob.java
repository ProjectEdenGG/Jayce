package gg.projecteden.jayce.models.scheduledjobs.jobs;

import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.utils.Utils;
import gg.projecteden.models.scheduledjobs.common.AbstractJob;
import gg.projecteden.models.scheduledjobs.common.RetryIfInterrupted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.time.LocalDateTime.now;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RetryIfInterrupted
public class SupportChannelArchiveJob extends AbstractJob {
	private String guildId;
	private String channelId;

	public SupportChannelArchiveJob(TextChannel channel) {
		this.guildId = channel.getGuild().getId();
		this.channelId = channel.getId();
	}

	@Override
	protected CompletableFuture<JobStatus> run() {
		final CompletableFuture<JobStatus> future = completable();

		final Guild guild = Jayce.JDA.getGuildById(guildId);
		if (guild == null)
			return completed();

		final TextChannel channel = guild.getTextChannelById(channelId);
		if (channel == null)
			return completed();

		final List<Category> categories = guild.getCategoriesByName("support-archive", true);
		if (categories.isEmpty())
			return completed();

		channel.getManager()
			.setParent(categories.iterator().next())
			.submit()
			.thenCompose($ -> Repos.repo(channel.getParent()).issues().close(Utils.getIssueId(channel)))
			.exceptionally(ex -> {
				ex.printStackTrace();
				future.complete(JobStatus.ERRORED);
				return null;
			})
			.thenRun(() -> {
				new SupportChannelDeleteJob(channel).schedule(now().plusDays(3));
				future.complete(JobStatus.COMPLETED);
			})
			.exceptionally(ex -> {
				ex.printStackTrace();
				future.complete(JobStatus.ERRORED);
				return null;
			});

		return future;
	}

}
