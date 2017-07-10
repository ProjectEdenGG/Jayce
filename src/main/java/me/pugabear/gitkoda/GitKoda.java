package me.pugabear.gitkoda;

import me.pugabear.gitkoda.utils.*;
import me.pugabear.gitkoda.commands.*;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDA;

import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;

public class GitKoda
{
	public static Services SERVICES;
	public static Config CONFIG;

	public static void main(String[] args) throws Exception 
	{
		try
		{
			CONFIG = new Config();
			SERVICES = new Services();

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