package me.pugabear.GitKoda;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.events.Event;

public class DiscordListener extends ListenerAdapter {
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    	System.out.println(event.getMessage().getContent());
    }
    
}





























