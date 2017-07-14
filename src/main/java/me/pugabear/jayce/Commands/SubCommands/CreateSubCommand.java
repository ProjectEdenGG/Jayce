package me.pugabear.jayce.Commands.SubCommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabear.jayce.Jayce;
import me.pugabear.jayce.Utils.InvalidArgumentException;
import me.pugabear.jayce.Utils.Utils;
import org.eclipse.egit.github.core.Issue;

import static me.pugabear.jayce.Jayce.CONFIG;
import static me.pugabear.jayce.Jayce.SERVICES;

public class CreateSubCommand {
	private static final String USAGE = "create <short desc> | <longer description>";

	public CreateSubCommand(String name, CommandEvent event) throws InvalidArgumentException {
		// TODO Allow setting more options on creation (assignees, labels...)
		String[] content;
		try {
			content = event.getArgs().split(" ", 2)[1].split("( \\| )", 2);

			if (content[0].isEmpty())
				throw new InvalidArgumentException(Jayce.USAGE + USAGE);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new InvalidArgumentException(Jayce.USAGE + USAGE);
		}

		int id = 0;
		try {
			id = create(content[0], content[1], name);
		} catch (ArrayIndexOutOfBoundsException ex) {
			id = create(content[0], "", name);
		}

		if (id != 0)
			Utils.reply(event, "https://github.com/" + CONFIG.githubUser + "/" + CONFIG.githubRepo + "/issues/" + id);
		else
			event.reply("Issue creation failed");
	}

	private int create(String title, String body, String name) {
		try {
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody("**" + name + "**: " + body);
			Issue result = SERVICES.issues.createIssue(CONFIG.githubUser, CONFIG.githubRepo, issue);

			return result.getNumber();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return 0;
		}
	}
}
