package me.pugabear.GitKoda;

import com.google.gson.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDA;
import org.eclipse.egit.github.core.service.IssueService;

public class GitKoda {
	static Services SERVICES;
	
	
	public static void main(String[] args) throws Exception {
		try {
	        JDA jda = new JDABuilder(AccountType.BOT)
	                .setToken(Utils.getToken("discord"))
			        .buildBlocking();
	        jda.addEventListener(new DiscordListener());

			SERVICES = new Services();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
