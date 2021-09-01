package gg.projecteden.jayce.commands.common;

import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.commands.common.annotations.Choices;
import gg.projecteden.jayce.commands.common.annotations.Desc;
import gg.projecteden.jayce.commands.common.annotations.Path;
import gg.projecteden.utils.DiscordId;
import gg.projecteden.utils.Env;
import lombok.Data;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static gg.projecteden.jayce.Jayce.JDA;
import static gg.projecteden.jayce.commands.common.AppCommandHandler.parseMentions;
import static gg.projecteden.utils.StringUtils.replaceLast;
import static java.util.stream.Collectors.joining;

public class AppCommandRegistration {
	static final Map<String, Class<? extends AppCommand>> COMMANDS = new HashMap<>();
	static final Map<Class<? extends AppCommand>, Map<String, Method>> METHODS = new HashMap<>();
	static final Map<Class<?>, Supplier<List<Choice>>> CHOICES = new HashMap<>();
	static final Map<Class<?>, List<Choice>> CHOICES_CACHE = new HashMap<>();
	static final Map<Class<?>, Function<OptionMapping, Object>> CONVERTERS = new HashMap<>();
	static final Map<Class<?>, OptionType> OPTION_TYPE_MAP = new HashMap<>();

	public static void mapOptionType(Class<?> clazz, OptionType optionType) {
		mapOptionType(List.of(clazz),  optionType);
	}

	public static void mapOptionType(List<Class<?>> classes, OptionType optionType) {
		for (Class<?> clazz : classes)
			OPTION_TYPE_MAP.put(clazz, optionType);
	}

	public static void registerConverter(Class<?> clazz, Function<OptionMapping, Object> converter) {
		registerConverter(List.of(clazz), converter);
	}

	public static void registerConverter(List<Class<?>> classes, Function<OptionMapping, Object> converter) {
		for (Class<?> clazz : classes)
			CONVERTERS.put(clazz, converter);
	}

	public static void supplyChoices(Class<?> clazz, Supplier<List<Choice>> supplier) {
		CHOICES.put(clazz, supplier);
	}

	private static List<Choice> loadChoices(Class<?> clazz) {
		return CHOICES_CACHE.computeIfAbsent(clazz, $ -> CHOICES.getOrDefault(clazz, Collections::emptyList).get());
	}

	@NotNull
	private static String getCommandName(Class<? extends AppCommand> clazz) {
		return replaceLast(clazz.getSimpleName(), "AppCommand", "").toLowerCase();
	}

	@SneakyThrows
	public static void registerAll() {
		if (Jayce.get().getEnv() != Env.DEV)
			return;

		var reflections = new Reflections(Jayce.class.getPackage().getName() + ".commands");
		for (var clazz : reflections.getSubTypesOf(AppCommand.class))
			register(clazz);

		METHODS.forEach((cmd, map) -> {
			System.out.println(cmd.getSimpleName() + " methods:");
			map.forEach((path, method) ->
				System.out.println(path + ": " + method.getName()));
		});
	}

	private static void register(CommandData command) {
		for (Guild guild : JDA.getGuilds()) {
			try { Thread.sleep(300); } catch (Exception ignored) {}

			if (guild.getId().equals(DiscordId.Guild.PROJECT_EDEN.getId()))
				continue;

			String id = "/" + command.getName() + " | " + guild.getName() + " |";

			Consumer<String> success = action -> System.out.println(id + " ✔ " + action);
			Consumer<String> failure = action -> System.out.println(id + " ✗ " + action);

			/*
			guild.retrieveCommands().complete().forEach(existingCommand -> {
				guild.deleteCommandById(existingCommand.getId()).complete();
				success.accept("DELETE EXISTING");
			});

			guild.retrieveCommandPrivileges().complete().forEach((existingCommand, privileges) -> {
				for (CommandPrivilege privilege : privileges)
					success.accept("Found privilege " + privilege + " for " + existingCommand + " in " + guild.getName());
			});
			*/

			Consumer<Command> setPrivilege = response -> {
				/* TODO
				final CommandPrivilege privilege = new CommandPrivilege(Type.ROLE, true, guild.getRolesByName());
				guild.updateCommandPrivilegesById(response.getId(), privilege).submit()
					.thenAccept(response2 -> {
						success.accept("PRIVILEGE");
					}).exceptionally(ex -> {
						failure.accept("PRIVILEGE");
						ex.printStackTrace();
						return null;
					});

				*/
			};

			System.out.println("/" + command.getName() + ": " + command.toData());

			guild.upsertCommand(command).submit()
				.thenAccept(response -> {
					success.accept("COMMAND");
					setPrivilege.accept(response);
				}).exceptionally(ex -> {
					failure.accept("COMMAND");
					ex.printStackTrace();
					return null;
				});
		}
	}

	public static void register(Class<? extends AppCommand> clazz) {
		try {
			Class.forName(clazz.getName(), true, clazz.getClassLoader());
			var command = build(clazz);
			register(command);
			cache(command.getName(), clazz);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@NotNull
	private static CommandData build(Class<? extends AppCommand> clazz) {
		final CommandData command = new CommandData(getCommandName(clazz), requireDescription(clazz));

		Map<String, SubcommandGroupData> subcommands = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			try {
				final String methodId = clazz.getSimpleName() + "#" + method.getName();

				final Path pathAnnotation = method.getAnnotation(Path.class);
				if (pathAnnotation == null)
					continue;

				final String path = pathAnnotation.value();
				final String desc = requireDescription(method);

				// TODO Use regex (Almost works: `^[\w\s]+(?![<\[])`)
				final String literal = List.of(path.split(" ")).stream().filter(arg -> arg.matches("[\\w-]+")).collect(joining(" "));
				final String args = path.replaceFirst(literal, "").trim();

				final List<OptionData> options = buildOptions(clazz, method, List.of(args.split(" ")));

				final String[] literals = literal.toLowerCase().split(" ");
				switch (literals.length) {
					case 0 ->
						command.addOptions(options);
					case 1 ->
						command.addSubcommands(new SubcommandData(literals[0], desc)
						.addOptions(options));
					case 2 ->
						subcommands.computeIfAbsent(literals[0], $ -> new SubcommandGroupData(literals[0], desc))
							.addSubcommands(new SubcommandData(literals[1], desc)
								.addOptions(options));
					default ->
						throw new EdenException(methodId + " has more than 2 literal arguments");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		command.addSubcommandGroups(subcommands.values());
		return command;
	}

	private static void cache(String command, Class<? extends AppCommand> clazz) {
		COMMANDS.put(command, clazz);

		Map<String, Method> methods = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			method.setAccessible(true);

			final Path annotation = method.getAnnotation(Path.class);
			if (annotation == null)
				continue;

			final String path = annotation.value();
			final String literals = path.split(" [<\\[]", 2)[0];
			methods.put(getCommandName(clazz) + "/" + literals.replaceAll(" ", "/"), method);
		}

		METHODS.put(clazz, methods);
	}

	private static List<OptionData> buildOptions(Class<? extends AppCommand> clazz, Method method, List<String> pathArguments) {
		final Parameter[] parameters = method.getParameters();

		if (parameters.length != pathArguments.size())
			throw new EdenException(clazz.getSimpleName() + "#" + method.getName() + " path misconfigured " +
				"[params=" + parameters.length + ", args=" + pathArguments.size() + "]");

		List<OptionData> options = new ArrayList<>();

		final Iterator<String> arguments = pathArguments.iterator();
		for (Parameter parameter : parameters) {
			final AppCommandArgument argument = new AppCommandArgument(clazz, method, parameter, arguments.next());

			options.add(argument.asOption());
		}

		return options;
	}

	@Data
	private static class AppCommandArgument {
		private final Class<? extends AppCommand> command;
		private final Method method;
		private final Parameter parameter;
		private final String pathArgument;
		private final String description;
		private final Class<?> type;
		private final Class<?> choices;
		private final boolean required;
		private final OptionType optionType;

		public AppCommandArgument(Class<? extends AppCommand> clazz, Method method, Parameter parameter, String pathArgument) {
			this.command = clazz;
			this.method = method;
			this.parameter = parameter;
			this.pathArgument = pathArgument;
			this.description = requireDescription(parameter);
			this.type = parameter.getType();
			final Choices choicesAnnotation = parameter.getAnnotation(Choices.class);
			this.choices = choicesAnnotation == null ? type : choicesAnnotation.value();
			this.required = pathArgument.startsWith("<");
			this.optionType = resolveOptionType(this.type);
		}

		private OptionData asOption() {
			final OptionData option = new OptionData(optionType, parameter.getName().toLowerCase(), description, required);
			if (choices != null)
				option.addChoices(loadChoices(choices));
			return option;
		}
	}

	@NotNull
	private static String requireDescription(Class<?> clazz) {
		final Desc annotation = clazz.getAnnotation(Desc.class);
		return annotation == null ? clazz.getSimpleName() : annotation.value();
	}

	@NotNull
	private static String requireDescription(Method method) {
		final Desc annotation = method.getAnnotation(Desc.class);
		return annotation == null ? method.getName() : annotation.value();
	}

	@NotNull
	private static String requireDescription(Parameter parameter) {
		final Desc annotation = parameter.getAnnotation(Desc.class);
		return annotation == null ? parameter.getName() : annotation.value();
	}

	private static OptionType resolveOptionType(Class<?> type) {
		return OPTION_TYPE_MAP.getOrDefault(type, OptionType.STRING);
	}

	static {
		mapOptionType(String.class, OptionType.STRING);
		mapOptionType(List.of(Boolean.class, Boolean.TYPE), OptionType.BOOLEAN);
		mapOptionType(List.of(Integer.class, Long.class, Byte.class, Short.class, Integer.TYPE, Long.TYPE, Byte.TYPE, Short.TYPE), OptionType.INTEGER);
		mapOptionType(List.of(Double.class, Float.class, Double.TYPE, Float.TYPE), OptionType.NUMBER);
		mapOptionType(List.of(Member.class, User.class), OptionType.USER);
		mapOptionType(List.of(GuildChannel.class, MessageChannel.class), OptionType.CHANNEL);
		mapOptionType(Role.class, OptionType.ROLE);
		mapOptionType(IMentionable.class, OptionType.MENTIONABLE);

		registerConverter(String.class, option -> parseMentions(option.getAsString()));
		registerConverter(List.of(Boolean.class, Boolean.TYPE), OptionMapping::getAsBoolean);
		registerConverter(List.of(Long.class, Long.TYPE), OptionMapping::getAsLong);
		registerConverter(List.of(Integer.class, Integer.TYPE), option -> Long.valueOf(option.getAsLong()).intValue());
		registerConverter(List.of(Short.class, Short.TYPE), option -> Long.valueOf(option.getAsLong()).shortValue());
		registerConverter(List.of(Byte.class, Byte.TYPE), option -> Long.valueOf(option.getAsLong()).byteValue());
		registerConverter(List.of(Double.class, Double.TYPE), OptionMapping::getAsDouble);
		registerConverter(List.of(Float.class, Float.TYPE), option -> Double.valueOf(option.getAsDouble()).floatValue());
		registerConverter(Member.class, OptionMapping::getAsMember);
		registerConverter(User.class, OptionMapping::getAsUser);
		registerConverter(Role.class, OptionMapping::getAsRole);
		registerConverter(GuildChannel.class, OptionMapping::getAsGuildChannel);
		registerConverter(MessageChannel.class, OptionMapping::getAsMessageChannel);
		registerConverter(IMentionable.class, OptionMapping::getAsMentionable);
	}

}
