package gg.projecteden.jayce.listeners;

import gg.projecteden.annotations.Environments;
import gg.projecteden.utils.Env;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Environments(Env.TEST)
public class ApplicationCommands extends ListenerAdapter {

	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		System.out.println("Command name: " + event.getName());
		System.out.println("Command path: " + event.getCommandPath());
		if (!event.getName().equals("issue")) return;
		try {
			event.getOptions().forEach(optionMapping -> System.out.println(" " + optionMapping.toString()));
			event.reply("ok").queue();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
