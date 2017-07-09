package me.pugabear.gitkoda;

import me.pugabear.gitkoda.utils.Utils;
import me.pugabear.gitkoda.utils.Services;
import me.pugabear.gitkoda.commands.*;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDA;

import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;

public class GitKoda
{
	private final static String ownerId = "115552359458799616";
	public final static String REPO = "BearNation";
	public final static String USER = "PugaBear";
	public static Services SERVICES;
	
	public static void main(String[] args) throws Exception 
	{
		try
		{
	        CommandClientBuilder client = new CommandClientBuilder();
	        client.setPrefix("!");
	        client.setOwnerId(ownerId);
	        client.setPlaying("!issue");
	        
	        client.addCommand(new IssueCommand());
			
	        JDA jda = new JDABuilder(AccountType.BOT)
	                .setToken(Utils.getToken("discord"))
			        .buildAsync();
	        
            jda.addEventListener(client.build());
            
            

			SERVICES = new Services();
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
