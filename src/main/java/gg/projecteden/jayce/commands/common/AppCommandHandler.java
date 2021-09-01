package gg.projecteden.jayce.commands.common;

import gg.projecteden.annotations.Environments;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.utils.Env;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import static gg.projecteden.jayce.commands.common.AppCommandRegistration.COMMANDS;
import static gg.projecteden.jayce.commands.common.AppCommandRegistration.METHODS;

/*
TODO
- Required args
- Exception handling
- Automatic command registration
- Dynamic converters
- Dynamic choices?
- Permissions (Privileges)
 */

@Environments(Env.DEV)
public class AppCommandHandler extends ListenerAdapter {

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		try {
			final Class<? extends AppCommand> clazz = COMMANDS.get(event.getName());
			final Map<String, Method> methods = METHODS.get(clazz);
			final Method method = methods.get(event.getCommandPath());

			final Parameter[] parameters = method.getParameters();

			final Object[] arguments = new Object[parameters.length];

			// convert passed arguments
			int index = 0;
			for (Parameter parameter : parameters) {
				for (OptionMapping option : event.getOptions()) {
					if (!option.getName().equals(parameter.getName()))
						continue;

					arguments[index++] = convert(method, parameter, option);
				}
			}

			// fill in the rest
			while (index < arguments.length)
				arguments[index++] = null;

			final AppCommandEvent commandEvent = new AppCommandEvent(event);
			final AppCommand appCommand = clazz.getConstructor(AppCommandEvent.class).newInstance(commandEvent);
			method.invoke(appCommand, arguments);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private Object convert(Method method, Parameter parameter, OptionMapping option) {
		final Class<?> type = parameter.getType();
		if (type == String.class)
			return parseMentions(option.getAsString());
		else if (type == Boolean.class || type == Boolean.TYPE)
			return option.getAsBoolean();
		else if (type == Integer.class || type == Integer.TYPE)
			return Long.valueOf(option.getAsLong()).intValue();
		else if (type == Long.class || type == Long.TYPE)
			return option.getAsLong();
		else if (type == Double.class || type == Double.TYPE)
			return option.getAsDouble();
		else if (type == Member.class)
			return option.getAsMember();
		else if (type == User.class)
			return option.getAsUser();
		else if (type == Role.class)
			return option.getAsRole();
		else if (type == GuildChannel.class)
			return option.getAsGuildChannel();
		else if (type == MessageChannel.class)
			return option.getAsMessageChannel();
		else if (type == IMentionable.class)
			return option.getAsMentionable();
		else
			throw new EdenException("Could not convert argument [input=" + option.getAsString() + ", type=" +
				type.getSimpleName() + ", method=" + method.getName() + ", parameter=" + parameter.getName() + "]");
	}

	protected String parseMentions(String content) {
		// TODO

		return content;
	}
}
