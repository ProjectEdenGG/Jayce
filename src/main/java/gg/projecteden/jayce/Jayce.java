package gg.projecteden.jayce;

import com.spotify.github.v3.clients.GitHubClient;
import gg.projecteden.api.common.utils.Env;
import gg.projecteden.api.common.utils.ReflectionUtils;
import gg.projecteden.api.discord.appcommands.AppCommandRegistry;
import gg.projecteden.api.mongodb.DatabaseConfig;
import gg.projecteden.api.mongodb.EdenDatabaseAPI;
import gg.projecteden.api.mongodb.models.scheduledjobs.ScheduledJobsRunner;
import gg.projecteden.jayce.config.Config;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Stream;

public class Jayce extends EdenDatabaseAPI {
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
			.enableIntents(EnumSet.allOf(GatewayIntent.class))
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

//	@Override
//	public Collection<? extends Class<? extends TypeConverter>> getMongoConverters() {
//		return List.of(LocalDateTimeConverter.class, JobConverter.class, UUIDConverter.class);
//	}

	private Stream<? extends ListenerAdapter> getListeners() {
		return ReflectionUtils.subTypesOf(ListenerAdapter.class, Jayce.class.getPackageName()).stream().map(clazz -> {
			try {
				return clazz.getConstructor().newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return null;
		}).filter(Objects::nonNull);
	}

}
