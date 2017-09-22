package me.pugabyte.jayce.Commands.SubCommands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LabelSubCommand {
    private static final String USAGE = "label[s] [<id> <add|remove> <labels>]";

    public LabelSubCommand(int id, String action, String[] labels, CommandEvent event) throws me.pugabyte.jayce.Utils.InvalidArgumentException {
        if (id != 0 && action == null)
            throw new me.pugabyte.jayce.Utils.InvalidArgumentException(me.pugabyte.jayce.Jayce.USAGE + USAGE);
        if (!(action.equals("add") || action.equals("remove") || action.equals("get")))
            throw new me.pugabyte.jayce.Utils.InvalidArgumentException(me.pugabyte.jayce.Jayce.USAGE + USAGE);
        if (!action.equals("get") && labels.length == 0)
            throw new me.pugabyte.jayce.Utils.InvalidArgumentException(me.pugabyte.jayce.Jayce.USAGE + USAGE);

        List<String> _validLabels;
        try {
            _validLabels = me.pugabyte.jayce.Jayce.SERVICES.labels.getLabels(me.pugabyte.jayce.Jayce.CONFIG.githubUser, me.pugabyte.jayce.Jayce.CONFIG.githubRepo).stream()
                    .map(Label::getName)
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            ex.printStackTrace();
            event.reply("Error occurred trying to get valid labels");
            return;
        }

        String[] validLabels = _validLabels.toArray(new String[0]);

        if (action.equals("get"))
            event.reply("Valid labels: " + String.join(", ", validLabels));
        else {
            for (String label : labels)
                if (Arrays.stream(validLabels).noneMatch(label::equals))
                    throw new me.pugabyte.jayce.Utils.InvalidArgumentException("Label \"" + label + "\" does not exist!");

            if (modifyLabels(id, action, labels))
                event.reply(":thumbsup:");
            else
                event.reply("Could not modify labels");
        }

    }

    private static boolean modifyLabels(int id, String action, String[] labels) {
        try {
            Issue issue = me.pugabyte.jayce.Jayce.SERVICES.issues.getIssue(me.pugabyte.jayce.Jayce.CONFIG.githubUser, me.pugabyte.jayce.Jayce.CONFIG.githubRepo, id);
            List<Label> issueLabels = issue.getLabels();
            if (action.equals("add"))
                for (String label : labels)
                    issueLabels.add(me.pugabyte.jayce.Jayce.SERVICES.labels.getLabel(me.pugabyte.jayce.Jayce.CONFIG.githubUser, me.pugabyte.jayce.Jayce.CONFIG.githubRepo, label));

            else if (action.equals("remove"))
                for (String label : labels)
                    issueLabels.remove(me.pugabyte.jayce.Jayce.SERVICES.labels.getLabel(me.pugabyte.jayce.Jayce.CONFIG.githubUser, me.pugabyte.jayce.Jayce.CONFIG.githubRepo, label));

            me.pugabyte.jayce.Jayce.SERVICES.labels.setLabels(me.pugabyte.jayce.Jayce.CONFIG.githubUser, me.pugabyte.jayce.Jayce.CONFIG.githubRepo, String.valueOf(id), issueLabels);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
