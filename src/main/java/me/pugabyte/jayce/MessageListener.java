package me.pugabyte.jayce;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().contains("git#")) {
			String[] words = event.getMessage().getContentRaw().split(" ");
			for (String word : words) {
				if (word.startsWith("git#")) {
					word = word.replaceAll("git#", "");
					try {
						int id = Integer.parseInt(word);
						event.getTextChannel().sendMessage("<https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues/" + id + ">").queue();
					} catch (NumberFormatException ex) {
					}
				}
			}
		}
	}
}
