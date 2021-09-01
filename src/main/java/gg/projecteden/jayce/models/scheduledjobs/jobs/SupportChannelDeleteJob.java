package gg.projecteden.jayce.models.scheduledjobs.jobs;

import gg.projecteden.jayce.Jayce;
import gg.projecteden.models.scheduledjobs.common.AbstractJob;
import gg.projecteden.models.scheduledjobs.common.RetryIfInterrupted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RetryIfInterrupted
public class SupportChannelDeleteJob extends AbstractJob {
	private String guildId;
	private String channelId;

	public SupportChannelDeleteJob(TextChannel channel) {
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

		channel.delete().queue(success -> future.complete(JobStatus.COMPLETED), ex -> {
			ex.printStackTrace();
			future.complete(JobStatus.ERRORED);
		});

		return future;
	}

}