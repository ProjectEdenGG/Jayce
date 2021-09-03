package gg.projecteden.jayce.commands.common;

import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.utils.Utils;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public abstract class AppCommand {
	protected final AppCommandEvent event;

	protected Member member() {
		return event.getEvent().getMember();
	}

	protected String name() {
		return member().getEffectiveName();
	}

	protected String mention() {
		return member().getAsMention();
	}

	protected Guild guild() {
		return event.getEvent().getGuild();
	}

	protected TextChannel channel() {
		return event.getEvent().getTextChannel();
	}

	protected Category category() {
		return channel().getParent();
	}

	protected Category category(String name) {
		return guild().getCategoriesByName(name, true).iterator().next();
	}

	protected CompletableFuture<InteractionHook> reply(String message) {
		return event.getEvent().reply(message).submit();
	}

	protected CompletableFuture<InteractionHook> reply(EmbedBuilder message) {
		return event.getEvent().reply(new MessageBuilder().setEmbeds(message.build()).build()).submit();
	}

	protected CompletableFuture<InteractionHook> thumbsup() {
		return reply(":thumbsup:");
	}

	// Jayce specific

	protected boolean isWebhookChannel() {
		return channel().getId().equals(Config.WEBHOOK_CHANNEL_ID);
	}

	protected int getIssueId() {
		return Utils.getIssueId(event.getEvent().getTextChannel());
	}

	protected RepoContext repo() {
		return Repos.repo(channel().getParent());
	}

	protected RepoIssueContext issues() {
		return repo().issues();
	}

}
