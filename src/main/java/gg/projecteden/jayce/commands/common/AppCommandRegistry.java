package gg.projecteden.jayce.commands.common;

import gg.projecteden.jayce.commands.common.annotations.Desc;
import gg.projecteden.utils.DiscordId;
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
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static gg.projecteden.jayce.Jayce.JDA;
import static gg.projecteden.jayce.commands.common.AppCommandBuilder.getCommandName;
import static gg.projecteden.jayce.commands.common.AppCommandHandler.parseMentions;

public record AppCommandRegistry(String packageName) {
	static final Map<String, Class<? extends AppCommand>> COMMANDS = new HashMap<>();
	static final Map<Class<? extends AppCommand>, Map<String, Method>> METHODS = new HashMap<>();
	static final Map<Class<?>, Function<OptionMapping, Object>> CONVERTERS = new HashMap<>();
	static final Map<Class<?>, Supplier<List<Choice>>> CHOICES = new HashMap<>();
	static final Map<Class<?>, List<Choice>> CHOICES_CACHE = new HashMap<>();
	static final Map<Class<?>, OptionType> OPTION_TYPE_MAP = new HashMap<>();

	@SneakyThrows
	public void registerAll() {
		var reflections = new Reflections(packageName);
		for (var clazz : reflections.getSubTypesOf(AppCommand.class))
			AppCommandRegistry.register(clazz);
	}

	public static void register(Class<? extends AppCommand> clazz) {
		try {
			Class.forName(clazz.getName(), true, clazz.getClassLoader());
			var command = new AppCommandBuilder(clazz).build();
			register(command);
			cache(command.getName(), clazz);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void register(CommandData command) {
		for (Guild guild : JDA.getGuilds()) {
			try {
				Thread.sleep(300);
			} catch (Exception ignored) {
			}

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

	private static void cache(String command, Class<? extends AppCommand> clazz) {
		COMMANDS.put(command, clazz);

		Map<String, Method> methods = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getAnnotation(Desc.class) == null)
				continue;

			method.setAccessible(true);
			methods.put(getCommandName(clazz) + "/" + method.getName().replaceAll("_", "/"), method);
		}

		METHODS.put(clazz, methods);
	}

	public static void mapOptionType(Class<?> clazz, OptionType optionType) {
		mapOptionType(List.of(clazz), optionType);
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

	static OptionType resolveOptionType(Class<?> type) {
		return OPTION_TYPE_MAP.getOrDefault(type, OptionType.STRING);
	}

	public static void supplyChoices(Class<?> clazz, Supplier<List<Choice>> supplier) {
		CHOICES.put(clazz, supplier);
	}

	static List<Choice> loadChoices(Class<?> clazz) {
		return CHOICES_CACHE.computeIfAbsent(clazz, $ -> CHOICES.getOrDefault(clazz, Collections::emptyList).get());
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
