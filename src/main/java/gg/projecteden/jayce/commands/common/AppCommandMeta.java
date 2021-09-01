package gg.projecteden.jayce.commands.common;

import gg.projecteden.jayce.commands.common.annotations.Choices;
import gg.projecteden.jayce.commands.common.annotations.Command;
import gg.projecteden.jayce.commands.common.annotations.Desc;
import gg.projecteden.jayce.commands.common.annotations.Optional;
import gg.projecteden.jayce.commands.common.exceptions.AppCommandMisconfiguredException;
import gg.projecteden.utils.Utils;
import lombok.Data;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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

import static gg.projecteden.jayce.commands.common.AppCommandRegistry.COMMANDS;
import static gg.projecteden.jayce.commands.common.AppCommandRegistry.CONVERTERS;
import static gg.projecteden.jayce.commands.common.AppCommandRegistry.loadChoices;
import static gg.projecteden.jayce.commands.common.AppCommandRegistry.resolveOptionType;
import static gg.projecteden.utils.StringUtils.replaceLast;

@Data
public class AppCommandMeta<T extends AppCommand> {
	private final String name;
	private final Class<T> clazz;
	private final Map<String, AppCommandMethod> methods;
	private final CommandData command;

	public AppCommandMeta(Class<T> clazz) {
		this.name = replaceLast(clazz.getSimpleName(), AppCommand.class.getSimpleName(), "").toLowerCase();
		this.clazz = clazz;
		this.command = new CommandData(name, requireDescription(clazz));

		init();

		this.methods = new HashMap<>() {{
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.getAnnotation(Command.class) == null)
					continue;

				method.setAccessible(true);
				put(name + "/" + method.getName().replaceAll("_", "/"), new AppCommandMethod(method));
			}
		}};
	}

	private void init() {
		try {
			// Load class into JVM to call static initializers
			Class.forName(this.clazz.getName());
		} catch (Exception | ExceptionInInitializerError ex) {
			ex.printStackTrace();
		}
	}

	@SneakyThrows
	public T newInstance(AppCommandEvent event) {
		return clazz.getConstructor(event.getClass()).newInstance(event);
	}

	public AppCommandMethod getMethod(String commandPath) {
		return methods.get(commandPath);
	}

	@Data
	public class AppCommandMethod {
		private final Method method;
		private final String name;
		private final String description;
		private final String[] literals;
		private final List<AppCommandArgument> arguments;
		private final List<OptionData> options;

		public AppCommandMethod(Method method) {
			this.method = method;
			this.name = method.getName();
			this.literals = name.toLowerCase().split("_");
			this.description = method.getAnnotation(Command.class).value();
			this.arguments = Stream.of(method.getParameters()).map(AppCommandArgument::new).toList();
			this.options = this.arguments.stream().map(AppCommandArgument::asOption).toList();
			build();
		}

		public static AppCommandMeta<?>.AppCommandMethod of(SlashCommandEvent event) {
			return COMMANDS.get(event.getName()).getMethod(event.getCommandPath());
		}

		@SneakyThrows
		public void handle(SlashCommandEvent event) {
			method.invoke(newInstance(new AppCommandEvent(event)), convert(event.getOptions()));
		}

		@NotNull
		private Object[] convert(List<OptionMapping> options) {
			final Parameter[] parameters = method.getParameters();

			final Object[] arguments = new Object[parameters.length];

			// convert passed arguments
			int index = 0;
			for (Parameter parameter : parameters) {
				for (OptionMapping option : options) {
					if (!option.getName().equals(parameter.getName()))
						continue;

					arguments[index++] = CONVERTERS.get(parameter.getType()).apply(option);
				}
			}

			// fill in the rest
			while (index < arguments.length)
				arguments[index++] = null;

			return arguments;
		}

		@Data
		public class AppCommandArgument {
			private final Parameter parameter;
			private final String description;
			private final Class<?> type;
			private final Class<?> choices;
			private final boolean required;
			private final OptionType optionType;

			public AppCommandArgument(Parameter parameter) {
				this.parameter = parameter;
				this.description = defaultDescription(parameter);
				this.type = parameter.getType();
				final Choices choicesAnnotation = parameter.getAnnotation(Choices.class);
				this.choices = choicesAnnotation == null ? type : choicesAnnotation.value();
				this.required = parameter.getAnnotation(Optional.class) == null;
				this.optionType = resolveOptionType(this.type);
			}

			protected OptionData asOption() {
				if (!CONVERTERS.containsKey(type))
					throw new AppCommandMisconfiguredException("No converter for " + type.getSimpleName() + " registered");

				final OptionData option = new OptionData(optionType, parameter.getName().toLowerCase(), description, required);

				if (choices != null) {
					final List<Choice> choices = loadChoices(this.choices);
					if (!Utils.isNullOrEmpty(choices))
						option.addChoices(choices);
				}

				return option;
			}
		}

		private void build() {
			try {
				if (literals.length > 2)
					throw new AppCommandMisconfiguredException((clazz.getSimpleName() + "#" + method.getName()) + " has more than 2 literal arguments");

				switch (literals.length) {
					case 0 -> command.addOptions(options);
					case 1 -> command.addSubcommands(asSubcommand(0));
					case 2 -> getGroup().addSubcommands(asSubcommand(1));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@NotNull
		private SubcommandGroupData getGroup() {
			return command.getSubcommandGroups().stream()
				.filter(group -> group.getName().equals(literals[0]))
				.findFirst()
				.orElseGet(() -> {
					final SubcommandGroupData group = new SubcommandGroupData(literals[0], description);
					command.addSubcommandGroups(group);
					return group;
				});
		}

		@NotNull
		private SubcommandData asSubcommand(int index) {
			return new SubcommandData(literals[index], description).addOptions(options);
		}

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

}
