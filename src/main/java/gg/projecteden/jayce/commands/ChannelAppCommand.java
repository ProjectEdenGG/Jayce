package gg.projecteden.jayce.commands;

import com.spotify.github.v3.issues.ImmutableIssue;
import gg.projecteden.discord.appcommands.AppCommandEvent;
import gg.projecteden.discord.appcommands.annotations.Command;
import gg.projecteden.discord.appcommands.annotations.Desc;
import gg.projecteden.discord.appcommands.annotations.GuildCommand;
import gg.projecteden.discord.appcommands.annotations.Optional;
import gg.projecteden.discord.appcommands.annotations.Role;
import gg.projecteden.jayce.commands.common.JayceAppCommand;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.models.scheduledjobs.jobs.SupportChannelArchiveJob;
import gg.projecteden.models.scheduledjobs.ScheduledJobsService;
import gg.projecteden.models.scheduledjobs.common.AbstractJob.JobStatus;
import gg.projecteden.utils.CompletableFutures;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static gg.projecteden.jayce.Jayce.PROJECT_EDEN_GUILD_ID;
import static gg.projecteden.utils.StringUtils.camelCase;
import static java.time.LocalDateTime.now;

@Role("Staff")
@Command("Manage support channels")
@GuildCommand(exclude = PROJECT_EDEN_GUILD_ID)
public class ChannelAppCommand extends JayceAppCommand {

	public ChannelAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Command("Mark this channel as resolved")
	void resolve(@Desc("Close issue") @Optional Boolean close) {
		cancelExistingArchivalJobs();

		boolean closeIssue = close == null || close;
		issues().assign(getIssueId(), member()).thenRun(() -> {
			new SupportChannelArchiveJob(channel(), closeIssue).schedule(now().plusDays(1));
			reply("This channel has been marked as **resolved** and will be archived in 24 hours. " +
				"The related issue will " + (closeIssue ? "" : "not ") + "be closed.");
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return null;
		});
	}

	@Command("Mark this channel as unresolved and cancel archival")
	void unresolve() {
		cancelExistingArchivalJobs();

		issues().edit(getIssueId(), ImmutableIssue::withAssignees).thenRun(() -> {
			reply("This channel has been marked as **unresolved**, archival cancelled");
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return null;
		});
	}

	private void cancelExistingArchivalJobs() {
		new ScheduledJobsService().editApp(jobs -> jobs
			.get(JobStatus.PENDING, SupportChannelArchiveJob.class).stream()
			.filter(job -> job.getGuildId().equals(guild().getId()))
			.filter(job -> job.getChannelId().equals(channel().getId()))
			.forEach(job -> job.setStatus(JobStatus.CANCELLED)));
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
			.sync(newCategory)
			.submit()
			.thenRun(() -> reply("Transferred to **" + camelCase(repo) + "**"))
			.exceptionally(ex -> {
				ex.printStackTrace();
				reply("Could not automatically update channel, please complete manually");
				return null;
			});
	}

	private static final String BRANDING_URL = "https://raw.githubusercontent.com/ProjectEdenGG/branding/main/jayce/";

	@Command("Update embed description")
	void embed(@Desc("Embed description") String description) {
		CompletableFutures.allOf(new ArrayList<CompletableFuture<?>>() {{
			channel().getIterableHistory().forEach(message -> add(message.delete().submit()));
		}}).thenRun(() -> {
			final EmbedBuilder image = new EmbedBuilder().setImage(BRANDING_URL + channel().getName() + ".png");
			final EmbedBuilder text = new EmbedBuilder().appendDescription(description);

			channel().sendMessage(new MessageBuilder().setEmbeds(image.build(), text.build()).build()).queue();
			event.getEvent().reply("Success").setEphemeral(true).queue();
		});
	}

}
