package gg.projecteden.jayce.commands.common;

import gg.projecteden.jayce.commands.common.annotations.Choices;
import gg.projecteden.jayce.commands.common.annotations.Command;
import gg.projecteden.jayce.commands.common.annotations.Desc;
import gg.projecteden.jayce.commands.common.annotations.Optional;
import gg.projecteden.jayce.commands.common.exceptions.AppCommandMisconfiguredException;
import gg.projecteden.utils.Utils;
import lombok.Data;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static gg.projecteden.jayce.commands.common.AppCommandRegistry.loadChoices;
import static gg.projecteden.jayce.commands.common.AppCommandRegistry.resolveOptionType;
import static gg.projecteden.utils.StringUtils.replaceLast;

public record AppCommandBuilder(Class<? extends AppCommand> clazz) {

	@NotNull
	CommandData build() {
		final CommandData command = new CommandData(getCommandName(clazz), requireDescription(clazz));

		Map<String, SubcommandGroupData> subcommands = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			try {
				final Command annotation = method.getAnnotation(Command.class);
				if (annotation == null)
					continue;

				final String desc = annotation.value();
				final String literal = method.getName().replaceAll("_", " ");
				final List<OptionData> options = buildOptions(clazz, method);

				final String[] literals = literal.toLowerCase().split(" ");
				switch (literals.length) {
					case 0 -> command.addOptions(options);
					case 1 -> command.addSubcommands(new SubcommandData(literals[0], desc)
							.addOptions(options));
					case 2 -> subcommands.computeIfAbsent(literals[0], $ -> new SubcommandGroupData(literals[0], desc))
							.addSubcommands(new SubcommandData(literals[1], desc)
								.addOptions(options));
					default -> throw new AppCommandMisconfiguredException((clazz.getSimpleName() + "#" + method.getName()) + " has more than 2 literal arguments");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		command.addSubcommandGroups(subcommands.values());
		return command;
	}

	private static List<OptionData> buildOptions(Class<? extends AppCommand> clazz, Method method) {
		return Stream.of(method.getParameters()).map(parameter -> new AppCommandArgument(clazz, method, parameter).asOption()).toList();
	}

	@NotNull
	static String getCommandName(Class<? extends AppCommand> clazz) {
		return replaceLast(clazz.getSimpleName(), AppCommand.class.getSimpleName(), "").toLowerCase();
	}

	@NotNull
	private static String requireDescription(Class<?> clazz) {
		final Command annotation = clazz.getAnnotation(Command.class);
		if (annotation == null)
			throw new AppCommandMisconfiguredException(clazz.getSimpleName() + " does not have @" + Command.class.getSimpleName());

		return annotation.value();
	}

	@NotNull
	static String defaultDescription(Parameter parameter) {
		final Desc annotation = parameter.getAnnotation(Desc.class);
		return annotation == null ? parameter.getName() : annotation.value();
	}

	@Data
	private static class AppCommandArgument {
		private final Class<? extends AppCommand> command;
		private final Method method;
		private final Parameter parameter;
		private final String description;
		private final Class<?> type;
		private final Class<?> choices;
		private final boolean required;
		private final OptionType optionType;

		public AppCommandArgument(Class<? extends AppCommand> clazz, Method method, Parameter parameter) {
			this.command = clazz;
			this.method = method;
			this.parameter = parameter;
			this.description = defaultDescription(parameter);
			this.type = parameter.getType();
			final Choices choicesAnnotation = parameter.getAnnotation(Choices.class);
			this.choices = choicesAnnotation == null ? type : choicesAnnotation.value();
			this.required = parameter.getAnnotation(Optional.class) == null;
			this.optionType = resolveOptionType(this.type);
		}

		private OptionData asOption() {
			final OptionData option = new OptionData(optionType, parameter.getName().toLowerCase(), description, required);

			if (choices != null) {
				final List<Choice> choices = loadChoices(this.choices);
				if (!Utils.isNullOrEmpty(choices))
					option.addChoices(choices);
			}

			return option;
		}

	}

}
