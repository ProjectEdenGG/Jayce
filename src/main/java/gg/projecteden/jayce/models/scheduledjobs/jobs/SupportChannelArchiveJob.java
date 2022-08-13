package gg.projecteden.jayce.models.scheduledjobs.jobs;

import dev.morphia.annotations.Converters;
import gg.projecteden.api.mongodb.models.scheduledjobs.common.AbstractJob;
import gg.projecteden.api.mongodb.models.scheduledjobs.common.RetryIfInterrupted;
import gg.projecteden.api.mongodb.serializers.LocalDateTimeConverter;
import gg.projecteden.api.mongodb.serializers.UUIDConverter;
import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.utils.Utils;
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
import static java.util.concurrent.CompletableFuture.completedFuture;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RetryIfInterrupted
@Converters({UUIDConverter.class, LocalDateTimeConverter.class})
public class SupportChannelArchiveJob extends AbstractJob {
	private String guildId;
	private String channelId;
	private boolean closeIssue;

	public SupportChannelArchiveJob(TextChannel channel, boolean closeIssue) {
		this.guildId = channel.getGuild().getId();
		this.channelId = channel.getId();
		this.closeIssue = closeIssue;
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

		final List<Category> categories = guild.getCategoriesByName("support archive", true);
		if (categories.isEmpty())
			return completed();

		channel.getManager()
			.setParent(categories.iterator().next())
			.submit()
			.thenCompose($ -> {
				if (!closeIssue)
					return completedFuture(null);
				return Repos.repo(channel.getParentCategory()).issues().close(Utils.getIssueId(channel));
			})
			.exceptionally(ex -> {
				ex.printStackTrace();
				future.complete(JobStatus.ERRORED);
				return null;
			})
			.thenRun(() -> {
				new ChannelDeleteJob(channel).schedule(now().plusDays(3));
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
