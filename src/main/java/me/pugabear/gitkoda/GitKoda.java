package me.pugabear.gitkoda;

import me.pugabear.gitkoda.utils.*;
import me.pugabear.gitkoda.commands.*;

import com.google.gson.*;
import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDA;
import org.eclipse.egit.github.core.service.IssueService;

public class GitKoda {
	public static Services SERVICES;
	
	public static void main(String[] args) throws Exception {
		try {

	        CommandClientBuilder client = new CommandClientBuilder();
	        client.setPrefix("!");
	        client.setOwnerId("115552359458799616");
	        client.setPlaying("!issue");
	        
	        client.addCommands(
	        		new IssueCommand() 
	        );
			
	        JDA jda = new JDABuilder(AccountType.BOT)
	                .setToken(Utils.getToken("discord"))
			        .buildAsync();
	        
	        // jda.addEventListener(new DiscordListener());
            jda.addEventListener(client.build());

			SERVICES = new Services();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
