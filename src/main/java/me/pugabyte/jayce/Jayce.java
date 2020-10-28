package me.pugabyte.jayce;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.pugabyte.jayce.commands.IssueCommand;
import me.pugabyte.jayce.utils.Aliases;
import me.pugabyte.jayce.utils.Config;
import me.pugabyte.jayce.utils.Services;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Jayce {
	public static Aliases ALIASES;
	public static Config CONFIG;
	public static Services SERVICES;

	public static String USAGE;

	public static void main(String[] args) {
		try {
			CONFIG = new Config();
			ALIASES = new Aliases();
			SERVICES = new Services();

			USAGE = "Correct usage: " + CONFIG.commandPrefix + CONFIG.commandName + " ";

			CommandClientBuilder client = new CommandClientBuilder()
					.setPrefix(CONFIG.commandPrefix)
					.setOwnerId(CONFIG.ownerId)
					.setActivity(Activity.playing(CONFIG.commandPrefix + CONFIG.commandName))
					.addCommand(new IssueCommand());

			JDA jda = JDABuilder.createDefault(CONFIG.discordToken)
					.addEventListeners(client.build())
					.addEventListeners(new MessageListener())
					.build()
					.awaitReady();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
