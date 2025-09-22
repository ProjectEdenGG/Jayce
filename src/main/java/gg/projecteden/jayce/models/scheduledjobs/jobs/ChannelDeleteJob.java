package gg.projecteden.jayce.models.scheduledjobs.jobs;

import dev.morphia.annotations.Converters;
import gg.projecteden.api.mongodb.models.scheduledjobs.common.AbstractJob;
import gg.projecteden.api.mongodb.models.scheduledjobs.common.RetryIfInterrupted;
import gg.projecteden.api.mongodb.serializers.LocalDateTimeConverter;
import gg.projecteden.api.mongodb.serializers.UUIDConverter;
import gg.projecteden.jayce.Jayce;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.CompletableFuture;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RetryIfInterrupted
@Converters({UUIDConverter.class, LocalDateTimeConverter.class})
public class ChannelDeleteJob extends AbstractJob {
	private String guildId;
	private String channelId;

	public ChannelDeleteJob(TextChannel channel) {
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

		channel.delete().submit()
			.thenRun(() -> future.complete(JobStatus.COMPLETED))
			.exceptionally(ex -> {
				ex.printStackTrace();
				future.complete(JobStatus.ERRORED);
				return null;
			});

		return future;
	}

}
