package me.pugabear.jayce;

import me.pugabear.jayce.Commands.*;
import me.pugabear.jayce.Utils.*;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;

public class Jayce
{
	public static Aliases ALIASES;
	public static Config CONFIG;
	public static Services SERVICES;
	
	public static String USAGE;

	public static void main(String[] args) throws Exception 
	{
		try
		{
			CONFIG = new Config();
			ALIASES = new Aliases();
			SERVICES = new Services();

			USAGE = "Correct usage: " + CONFIG.commandPrefix + CONFIG.commandName + " ";
			
			CommandClientBuilder client = new CommandClientBuilder();
			client.setPrefix(CONFIG.commandPrefix);
			client.setOwnerId(CONFIG.ownerId);
			client.setPlaying(CONFIG.commandPrefix + CONFIG.commandName);
			client.addCommand(new IssueCommand());

			JDA jda = new JDABuilder(AccountType.BOT)
					.setToken(CONFIG.discordToken)
					.buildAsync();

			jda.addEventListener(client.build());
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
