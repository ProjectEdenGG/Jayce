package gg.projecteden.jayce.commands.common;

import gg.projecteden.annotations.Environments;
import gg.projecteden.jayce.commands.common.AppCommandMeta.AppCommandMethod;
import gg.projecteden.utils.Env;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/*
TODO
- Required args
- Exception handling
- Permissions (Privileges)
 */

@Environments(Env.DEV)
public class AppCommandHandler extends ListenerAdapter {

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		try {
			AppCommandMethod.of(event).handle(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected static String parseMentions(String content) {
		// TODO

		return content;
	}
}
