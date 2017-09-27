package me.pugabyte.jayce.Commands.SubCommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;
import me.pugabyte.jayce.Utils.InvalidArgumentException;
import net.dv8tion.jda.core.entities.User;
import org.eclipse.egit.github.core.Issue;

import java.util.ArrayList;
import java.util.List;

public class AssignSubCommand {
    public static final String USAGE = "assign <id> <@users>";

    public AssignSubCommand(int id, CommandEvent event) throws InvalidArgumentException {
        if (event.getMessage().getMentionedUsers().size() == 0)
            throw new InvalidArgumentException("You didn't supply a user to assign to the issue");

        List<String> userIds = new ArrayList<>();
        for (User user : event.getMessage().getMentionedUsers())
            userIds.add(user.getId());

        if (assign(id, userIds))
            event.reply(":thumbsup:");
        else
            event.reply("Couldn't assign users to issue");
    }

    private boolean assign(int id, List<String> userIds) {
        try {
            Issue issue = Jayce.SERVICES.issues.getIssue(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo, id);
            for (String userId : userIds)
                issue.setAssignee(Jayce.SERVICES.users.getUser(Jayce.ALIASES.aliases.get(userId)));
            Jayce.SERVICES.issues.editIssue(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo, issue);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
