package gg.projecteden.jayce.commands.common;

import com.vdurmont.emoji.EmojiManager;
import gg.projecteden.jayce.config.Config;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public final class CommandEvent {
	private final MessageReceivedEvent event;
	private final Member member;
	private final MessageChannel channel;

	public Message getMessage() {
		return event.getMessage();
	}

	public void thumbsup() {
		getMessage().addReaction(EmojiManager.getForAlias("thumbsup").getUnicode()).queue();
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
			String name;
			if (message.isFromGuild() && message.getGuild().isMember(user))
				name = message.getGuild().getMember(user).getEffectiveName();
			else
				name = user.getName();
			content = content.replaceAll("<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(name));
		}

		for (Emote emote : message.getEmotes()) {
			content = content.replace(emote.getAsMention(), ":" + emote.getName() + ":");
		}

		for (TextChannel mentionedChannel : message.getMentionedChannels()) {
			content = content.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());
		}

		for (Role mentionedRole : message.getMentionedRoles()) {
			content = content.replace(mentionedRole.getAsMention(), '@' + mentionedRole.getName());
		}

		return content;
	}


}
