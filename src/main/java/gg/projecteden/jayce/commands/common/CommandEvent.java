package gg.projecteden.jayce.commands.common;

import com.vdurmont.emoji.EmojiManager;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.utils.Utils;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@Data
public final class CommandEvent {
	private final MessageReceivedEvent event;
	private final Member member;
	private final MessageChannel channel;

	public boolean hasRole(String requiredRole) {
		return getMember().getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase(requiredRole));
	}

	public Message getMessage() {
		return event.getMessage();
	}

	public void thumbsup() {
		getMessage().addReaction(EmojiManager.getForAlias("thumbsup").getUnicode()).queue();
	}

	public Guild getGuild() {
		return getEvent().getGuild();
	}

	public TextChannel getTextChannel() {
		return (TextChannel) channel;
	}

	public String getMemberName() {
		return member.getEffectiveName();
	}

	public boolean isWebhookChannel() {
		return event.getChannel().getId().equals(Config.WEBHOOK_CHANNEL_ID);
	}

	public void reply(String message) {
		channel.sendMessage(message).queue();
	}

	public void reply(EmbedBuilder embed) {
		channel.sendMessage(embed.build()).queue();
	}

	// https://github.com/DV8FromTheWorld/JDA/blob/969f3f39/src/main/java/net/dv8tion/jda/internal/entities/ReceivedMessage.java#L608-L642
	public String parseMentions(String content) {
		final Message message = getMessage();

		for (User user : message.getMentionedUsers()) {
			String name = user.getName();
			if (message.isFromGuild() && message.getGuild().isMember(user))
				name = requireNonNull(message.getGuild().getMember(user)).getEffectiveName();
			content = content.replaceAll("<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(name));
		}

		for (Emote emote : message.getEmotes())
			content = content.replace(emote.getAsMention(), ":" + emote.getName() + ":");

		for (TextChannel mentionedChannel : message.getMentionedChannels())
			content = content.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());

		for (Role mentionedRole : message.getMentionedRoles())
			content = content.replace(mentionedRole.getAsMention(), '@' + mentionedRole.getName());

		return content;
	}

	public void handleException(Throwable ex) {
		if (ex instanceof EdenException edenEx)
			event.getChannel().sendMessage(edenEx.getMessage()).queue();
		else if (ex.getCause() instanceof EdenException edenEx)
			event.getChannel().sendMessage(edenEx.getMessage()).queue();
		else
			ex.printStackTrace();
	}

	// Support channels

	public int getIssueId() {
		return Utils.getIssueId(getTextChannel());
	}

	public RepoContext repo() {
		return Repos.repo(getTextChannel().getParent());
	}

	public RepoIssueContext issues() {
		return repo().issues();
	}

}
