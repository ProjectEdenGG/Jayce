package gg.projecteden.jayce.commands;

import com.spotify.github.v3.issues.ImmutableIssue;
import gg.projecteden.api.common.utils.CompletableFutures;
import gg.projecteden.api.discord.appcommands.AppCommandEvent;
import gg.projecteden.api.discord.appcommands.annotations.Command;
import gg.projecteden.api.discord.appcommands.annotations.Desc;
import gg.projecteden.api.discord.appcommands.annotations.GuildCommand;
import gg.projecteden.api.discord.appcommands.annotations.Optional;
import gg.projecteden.api.discord.appcommands.annotations.RequiredRole;
import gg.projecteden.api.mongodb.models.scheduledjobs.ScheduledJobsService;
import gg.projecteden.api.mongodb.models.scheduledjobs.common.AbstractJob.JobStatus;
import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.commands.common.JayceAppCommand;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.models.scheduledjobs.jobs.SupportChannelArchiveJob;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static gg.projecteden.api.common.utils.StringUtils.camelCase;
import static java.time.LocalDateTime.now;

@GuildCommand
@RequiredRole("Staff")
@Command("Manage support channels")
public class ChannelAppCommand extends JayceAppCommand {
	private static final int ARCHIVE_AFTER_HOURS = 12;

	public ChannelAppCommand(AppCommandEvent event) {
		super(event);
	}

	@Command("Mark this channel as resolved")
	void resolve(@Desc("Close issue") @Optional("false") Boolean close) {
		cancelExistingArchivalJobs();

		boolean closeIssue = close == null || close;
		issues().assign(getIssueId(), member()).thenRun(() -> {
			channel().getManager().setName(Jayce.RESOLVED + "-" + channel().getName().substring(1)).queue();
			new SupportChannelArchiveJob(channel(), closeIssue).schedule(now().plusHours(ARCHIVE_AFTER_HOURS));
			reply("This channel has been marked as **resolved** and will be archived in " + ARCHIVE_AFTER_HOURS + " hours. " +
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
			channel().getManager().setName(Jayce.UNRESOLVED + "-" + channel().getName().substring(1)).queue();
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
		final Supplier<ChannelManager<TextChannel, TextChannelManager>> manager = () -> channel.get().getManager();

		manager.get()
			.setName(channel().getName().charAt(0) + "-" + repo.toLowerCase() + "-" + newId)
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
			final EmbedBuilder text = new EmbedBuilder().appendDescription(description.replaceAll("\\\\n", System.lineSeparator()));

			channel().sendMessage(new MessageBuilder().setEmbeds(image.build(), text.build()).build()).queue();
			event.getEvent().reply("Success").setEphemeral(true).queue();
		});
	}

}
