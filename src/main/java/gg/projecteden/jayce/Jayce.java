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
import gg.projecteden.jayce.commands.common.CommandEvent;
import gg.projecteden.jayce.listeners.MessageListener;
import gg.projecteden.jayce.utils.Config;
import gg.projecteden.mongodb.DatabaseConfig;
import gg.projecteden.utils.Env;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Role;

import javax.security.auth.login.LoginException;
import java.net.URI;

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
			jda();
			cloud();
			github();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void github() {
		GITHUB = GitHubClient.create(URI.create("https://api.github.com/"), Config.GITHUB_TOKEN);
	}

	private void jda() throws InterruptedException, LoginException {
		JDA = JDABuilder.createDefault(Config.DISCORD_TOKEN)
			.addEventListeners(new MessageListener())
			.build()
			.awaitReady();
	}

	private void cloud() throws InterruptedException {
		COMMAND_MANAGER = new JDA4CommandManager<>(
			JDA,
			event -> Config.COMMAND_PREFIX,
			(event, command) -> event.getMember().getRoles().stream().map(Role::getName).anyMatch(role -> role.equalsIgnoreCase("Verified")),
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

}
