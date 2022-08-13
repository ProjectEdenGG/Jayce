package gg.projecteden.jayce.listeners.common;

import gg.projecteden.api.common.exceptions.EdenException;
import gg.projecteden.api.common.utils.Env;
import gg.projecteden.jayce.Jayce;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static gg.projecteden.jayce.Jayce.PROJECT_EDEN_GUILD_ID;

public abstract class DiscordListener extends ListenerAdapter {

	protected void handleException(@NotNull GenericMessageEvent event, Throwable ex) {
		if (ex instanceof EdenException edenEx)
			event.getChannel().sendMessage(edenEx.getMessage()).queue();
		else if (ex.getCause() instanceof EdenException edenEx)
			event.getChannel().sendMessage(edenEx.getMessage()).queue();
		else
			ex.printStackTrace();
	}

	protected boolean shouldIgnore(@NotNull MessageReceivedEvent event) {
		final Member member = event.getMember();
		if (member == null)
			return true;
		if (member.getUser().isBot())
			return true;

		return shouldIgnoreGuild(event);
	}

	protected boolean shouldIgnoreGuild(@NotNull GenericMessageEvent event) {
		return Jayce.get().getEnv() != Env.PROD && event.getGuild().getId().equals(PROJECT_EDEN_GUILD_ID);
	}

}
