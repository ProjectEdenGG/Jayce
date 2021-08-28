package gg.projecteden.jayce.commands.common;

import com.vdurmont.emoji.EmojiManager;
import gg.projecteden.jayce.config.Config;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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

}
