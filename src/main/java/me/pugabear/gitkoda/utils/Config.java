package me.pugabear.gitkoda.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;

public class Config
{
	public String githubToken,
				  discordToken, 
				  ownerId,
				  requiredRole,
				  iconUrl,
				  githubUser,
				  githubRepo,
				  webhookChannelId,
				  commandPrefix,
				  commandName;
	public String[] commandAliases;
	
	public Config() throws IOException
	{
		List<String> config = Files.readAllLines(Paths.get("GitKoda" + FileSystems.getDefault().getSeparator() + "config.txt"));
		for (String line : config) 
		{
			String[] setting = line.split(": ");
			System.out.println(setting[0] + " = " + setting[1]);
			switch (setting[0]) 
			{
				case "githubToken":
					this.githubToken = setting[1]; 
					break;
				case "discordToken":
					this.discordToken = setting[1];
					break;
				case "ownerId":
					this.ownerId = setting[1];
					break;
				case "requiredRole":
					this.requiredRole = setting[1];
					break;
				case "iconUrl":
					this.iconUrl = setting[1];
					break;
				case "githubUser":
					this.githubUser = setting[1];
					break;
				case "githubRepo":
					this.githubRepo = setting[1];
					break;
				case "webhookChannelId":
					this.webhookChannelId = setting[1];
					break;
				case "commandPrefix":
					this.commandPrefix = setting[1];
					break;
				case "commandName":
					this.commandName = setting[1];
					break;
				case "commandAliases":
					this.commandAliases = setting[1].split(",");
					break;
			}
		}
	}
}
