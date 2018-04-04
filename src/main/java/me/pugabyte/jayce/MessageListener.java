package me.pugabyte.jayce;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class MessageListener extends ListenerAdapter {
	public static void main(String[] args)
			throws LoginException, RateLimitedException, InterruptedException {
		JDA jda = new JDABuilder(AccountType.BOT).setToken("token").buildBlocking();
		jda.addEventListener(new MessageListener());
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		System.out.println("Processing message: " + event.getMessage().getContent());
		if (event.getMessage().getContent().contains("git#")) {
			String[] words = event.getMessage().getContent().split(" ");
			for (String word : words) {
				System.out.println(word);
				if (word.startsWith("git#")) {
					word = word.replaceAll("git#", "");
					try {
						int id = Integer.parseInt(word);
						event.getTextChannel().sendMessage("<https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues/" + id + ">").queue();
					} catch (NumberFormatException ex) {
						System.out.println("Error parsing " + word);
					}
				}
			}
		}
	}
}
