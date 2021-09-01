package gg.projecteden.jayce.commands.common;

import gg.projecteden.annotations.Environments;
import gg.projecteden.jayce.commands.common.exceptions.AppCommandMisconfiguredException;
import gg.projecteden.utils.Env;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import static gg.projecteden.jayce.commands.common.AppCommandRegistry.COMMANDS;
import static gg.projecteden.jayce.commands.common.AppCommandRegistry.CONVERTERS;
import static gg.projecteden.jayce.commands.common.AppCommandRegistry.METHODS;

/*
TODO
- Required args
- Exception handling
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

					arguments[index++] = convert(parameter.getType(), option);
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

	public static Object convert(Class<?> clazz, OptionMapping option) {
		if (!CONVERTERS.containsKey(clazz))
			throw new AppCommandMisconfiguredException("No converter for " + clazz.getSimpleName() + " registered");

		return CONVERTERS.get(clazz).apply(option);
	}

	protected static String parseMentions(String content) {
		// TODO

		return content;
	}
}
