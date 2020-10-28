package me.pugabyte.jayce.commands.subcommands;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.pugabyte.jayce.Jayce;
import me.pugabyte.jayce.utils.InvalidArgumentException;
import me.pugabyte.jayce.utils.Utils;
import org.eclipse.egit.github.core.Issue;

public class CreateSubCommand {
	private static final String USAGE = "create <short desc> | <longer description>";

	public CreateSubCommand(String name, CommandEvent event) throws InvalidArgumentException {
		// TODO Allow setting more options on creation (assignees, labels...)
		String[] content;
		try {
			content = event.getArgs().split(" ", 2)[1].split("( \\| )", 2);

			if (content[0].isEmpty()) {
				throw new InvalidArgumentException(Jayce.USAGE + USAGE);
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new InvalidArgumentException(Jayce.USAGE + USAGE);
		}

		int id;
		try {
			id = create(content[0], "**" + name + "**: " + content[1]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			id = create(content[0], "Submitted by **" + name + "**");
		}
		if (id != 0) {
			Utils.reply(event, "https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues/" + id);
		} else {
			Utils.reply(event, "Issue creation failed");
		}
	}

	private int create(String title, String body) {
		try {
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody(body);
			Issue result = Jayce.SERVICES.issues.createIssue(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo, issue);

			return result.getNumber();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return 0;
		}
	}
}
