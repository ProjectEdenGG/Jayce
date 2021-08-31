package gg.projecteden.jayce;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.jda.JDA4CommandManager;
import cloud.commandframework.jda.JDACommandSender;
import cloud.commandframework.meta.CommandMeta;
import com.spotify.github.v3.clients.GitHubClient;
import gg.projecteden.EdenAPI;
import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.commands.IssueCommand;
import gg.projecteden.jayce.commands.SupportChannelCommands;
import gg.projecteden.jayce.commands.common.CommandEvent;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.mongodb.DatabaseConfig;
import gg.projecteden.utils.Env;
import gg.projecteden.utils.Utils;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.reflections.Reflections;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Jayce extends EdenAPI {
	public static JDA JDA;
	public static JDA4CommandManager<CommandEvent> COMMAND_MANAGER;
	public static GitHubClient GITHUB;

	public static void main(String[] args) {
		new Jayce();
	}

	public Jayce() {
		instance = this;

		try {
			github();
			jda();
			cloud();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void github() {
		GITHUB = GitHubClient.create(URI.create("https://api.github.com/"), Config.GITHUB_TOKEN);
	}

	@SneakyThrows
	private void jda() {
		JDA = JDABuilder.createDefault(Config.DISCORD_TOKEN)
			.addEventListeners(getListeners().toArray())
			.build()
			.awaitReady();

		if (getEnv() == Env.DEV)
			registerAppCommand();
	}

	private void cloud() throws InterruptedException {
		COMMAND_MANAGER = new JDA4CommandManager<>(
			JDA,
			event -> Config.COMMAND_PREFIX,
			CommandEvent::hasRole,
			CommandExecutionCoordinator.simpleCoordinator(),
			event -> new CommandEvent(event.getEvent().get(), event.getEvent().get().getMember(), event.getChannel()),
			event -> JDACommandSender.of(event.getEvent())
		);

		COMMAND_MANAGER.registerExceptionHandler(CommandExecutionException.class, (event, ex) -> {
			if (ex.getCause() instanceof EdenException edenException)
				event.getChannel().sendMessage(edenException.getMessage()).queue();
			else {
				ex.printStackTrace();
				event.reply("An internal error occurred while attempting to perform this command");
			}
		});

		COMMAND_MANAGER.registerExceptionHandler(NoSuchCommandException.class, (event, ignore) -> {});

		final var annotationParser = new AnnotationParser<>(COMMAND_MANAGER, CommandEvent.class, params ->
			CommandMeta.simple()
				.with(CommandMeta.DESCRIPTION, params.get(StandardParameters.DESCRIPTION, "No description"))
				.build()
		);

		annotationParser.parse(new IssueCommand());
		annotationParser.parse(new SupportChannelCommands());
	}

	@Override
	public Env getEnv() {
		return Env.valueOf(Config.ENV);
	}

	@Override
	public DatabaseConfig getDatabaseConfig() {
		return DatabaseConfig.builder()
			.password(Config.DATABASE_PASSWORD)
			.env(getEnv())
			.build();
	}

	private Stream<? extends ListenerAdapter> getListeners() {
		final Reflections reflections = new Reflections(Jayce.class.getPackage().getName() + ".listeners");
		return reflections.getSubTypesOf(ListenerAdapter.class).stream().map(clazz -> {
			try {
				if (Utils.canEnable(clazz))
					return clazz.getConstructor().newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return null;
		}).filter(Objects::nonNull);
	}

	// This is just for testing at the moment
	@SneakyThrows
	private void registerAppCommand() {
		JDA.retrieveCommands().queue(commands -> {
			for (Command command : commands)
				if (!command.getName().equals("issues"))
					JDA.deleteCommandById(command.getId()).queue();
		});

		final CommandData commandData = new CommandData("issues", "Interact with GitHub issues")
			.addSubcommands(
				new SubcommandData("create", "Edit the title and/or body of an issue")
					.addOption(OptionType.STRING, "title", "Title", true)
					.addOption(OptionType.STRING, "body", "Body", true),
				new SubcommandData("open", "Open an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true),
				new SubcommandData("close", "Close an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true),
				new SubcommandData("assign", "Assign a user to an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true)
					.addOption(OptionType.USER, "user", "User", true),
				new SubcommandData("unassign", "Unassign a user from an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true)
					.addOption(OptionType.USER, "user", "User", true),
				new SubcommandData("edit", "Edit the title and/or body of an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true)
					.addOption(OptionType.STRING, "title", "Title")
					.addOption(OptionType.STRING, "body", "Body"),
				new SubcommandData("comment", "Add a comment to an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true)
					.addOption(OptionType.STRING, "text", "Comment", true),
				new SubcommandData("search", "Search issues")
					.addOption(OptionType.STRING, "query", "Query", true)
			);

		final List<Choice> labelChoices = Repos.main().listLabels().get().stream()
			.map(label -> requireNonNull(label.name()).toLowerCase())
			.map(label -> new Choice(label, label))
			.toList();

		final SubcommandData labelsAdd = new SubcommandData("add", "Add a label to an issue");
		final SubcommandData labelsRemove = new SubcommandData("remove", "Remove a label from an issue");

		for (int i = 1; i <= 5; i++) {
			final OptionData option = new OptionData(OptionType.STRING, "label" + i, "Label #" + i, i == 1).addChoices(labelChoices);
			labelsAdd.addOptions(option);
			labelsRemove.addOptions(option);
		}

		commandData.addSubcommandGroups(
			new SubcommandGroupData("labels", "Interact with labels")
				.addSubcommands(labelsAdd, labelsRemove)
		);

		final Guild guild = requireNonNull(JDA.getGuildById("241774576822910976"));
		guild.upsertCommand(commandData).queue();
	}

}
