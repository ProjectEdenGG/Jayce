package gg.projecteden.jayce.models.scheduledjobs.jobs;

import gg.projecteden.jayce.Jayce;
import gg.projecteden.models.scheduledjobs.common.AbstractJob;
import gg.projecteden.models.scheduledjobs.common.RetryIfInterrupted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RetryIfInterrupted
public class MessageDeleteJob extends AbstractJob {
	private String guildId;
	private String channelId;
	private String messageId;

	public MessageDeleteJob(Message message) {
		this.guildId = message.getGuild().getId();
		this.channelId = message.getTextChannel().getId();
		this.messageId = message.getId();
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

		channel.deleteMessageById(messageId).submit()
			.thenRun(() -> future.complete(JobStatus.COMPLETED))
			.exceptionally(ex -> {
				ex.printStackTrace();
				future.complete(JobStatus.ERRORED);
				return null;
			});

		return future;
	}

}
