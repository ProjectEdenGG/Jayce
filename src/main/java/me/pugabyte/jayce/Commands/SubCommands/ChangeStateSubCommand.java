package me.pugabyte.jayce.Commands.SubCommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Jayce;
import me.pugabyte.jayce.Utils.InvalidArgumentException;
import me.pugabyte.jayce.Utils.Utils;
import org.eclipse.egit.github.core.Issue;

public class ChangeStateSubCommand {
    private static final String USAGE = "<open|close> <id>";

    public ChangeStateSubCommand(int id, CommandEvent event) throws InvalidArgumentException {
        String[] args = event.getArgs().split(" ");
        String state;
        try {
            state = args[0].toLowerCase();
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new InvalidArgumentException(Jayce.USAGE + USAGE);
        }

        if (!(state.equals("open") || state.equals("close"))) {
            throw new InvalidArgumentException(Jayce.USAGE + USAGE);
        }

        if (changeState(id, state)) {
            Utils.reply(event, (state.equals("open") ? "Opened" : "Closed") + " issue: "
                    + "<https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues/" + id + ">");
        } else {
            event.reply("Could not " + state + " issue");
        }
    }

    private boolean changeState(int id, String state) {
        try {
            Issue issue = Jayce.SERVICES.issues.getIssue(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo, id);
            Jayce.SERVICES.issues.editIssue(Jayce.CONFIG.githubUser, Jayce.CONFIG.githubRepo, issue.setState(state));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
