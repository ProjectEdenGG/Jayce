package gg.projecteden.jayce;

import com.spotify.github.v3.clients.GitHubClient;
import gg.projecteden.EdenAPI;
import gg.projecteden.discord.appcommands.AppCommandRegistry;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.models.scheduledjobs.ScheduledJobsRunner;
import gg.projecteden.mongodb.DatabaseConfig;
import gg.projecteden.utils.Env;
import gg.projecteden.utils.Utils;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;

public class Jayce extends EdenAPI {
	public static JDA JDA;
	public static GitHubClient GITHUB;
	public static final String PROJECT_EDEN_GUILD_ID = "132680070480396288";
	public static final String UNRESOLVED = "✘";
	public static final String RESOLVED = "✔";

	public static void main(String[] args) {
		new Jayce();
	}

	public Jayce() {
		instance = this;

		try {
			github();
			jda();

			ScheduledJobsRunner.start();
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

		final String commandsPackage = Jayce.class.getPackage().getName() + ".commands";
		new AppCommandRegistry(JDA, commandsPackage).registerAll();
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
		final Reflections reflections = new Reflections(Jayce.class.getPackage().getName());
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

}
