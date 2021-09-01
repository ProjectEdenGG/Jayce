package gg.projecteden.jayce.commands.common;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@Data
@RequiredArgsConstructor
public class AppCommandEvent {
	private final SlashCommandEvent event;
}
