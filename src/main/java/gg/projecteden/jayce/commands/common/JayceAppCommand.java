package gg.projecteden.jayce.commands.common;

import gg.projecteden.discord.appcommands.AppCommand;
import gg.projecteden.discord.appcommands.AppCommandEvent;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.utils.Utils;

public abstract class JayceAppCommand extends AppCommand {

	public JayceAppCommand(AppCommandEvent event) {
		super(event);
		System.out.println("handling " + getClass().getSimpleName());
	}

	protected boolean isWebhookChannel() {
		return channel().getId().equals(Config.WEBHOOK_CHANNEL_ID);
	}

	protected int getIssueId() {
		return Utils.getIssueId(event.getEvent().getTextChannel());
	}

	protected RepoContext repo() {
		return Repos.repo(channel().getParent());
	}

	protected RepoIssueContext issues() {
		return repo().issues();
	}

}
