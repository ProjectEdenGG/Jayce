package me.pugabyte.jayce.Commands.SubCommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;
import me.pugabyte.jayce.Utils.InvalidArgumentException;
import me.pugabyte.jayce.Utils.Utils;
import org.eclipse.egit.github.core.Issue;

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
            Utils.reply(event, "https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues/" + id);
        else
            event.reply("Issue creation failed");
    }

    private int create(String title, String body, String name) {
        try {
            Issue issue = new Issue();
            issue.setTitle(title);
            if (body.isEmpty() || body == null) {
                issue.setBody("Submitted by **" + name + "**");
            } else {
                issue.setBody("**" + name + "**: " + body);
            }
            Issue result = Jayce.SERVICES.issues.createIssue(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo, issue);

            return result.getNumber();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            return 0;
        }
    }
}
