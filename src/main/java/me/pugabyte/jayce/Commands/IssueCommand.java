package me.pugabyte.jayce.Commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.pugabyte.jayce.Commands.SubCommands.AssignSubCommand;
import me.pugabyte.jayce.Jayce;

import java.util.Arrays;

public class IssueCommand extends Command {
    public IssueCommand() {
        this.name = Jayce.CONFIG.commandName;
        this.aliases = Jayce.CONFIG.commandAliases;
        if (!Jayce.CONFIG.requiredRole.isEmpty())
            this.requiredRole = Jayce.CONFIG.requiredRole;
    }

    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");

        String name;
        try {
            if (event.getMember().getNickname().equals("null"))
                name = event.getAuthor().getName();
            else
                name = event.getMember().getNickname();
        } catch (NullPointerException ex) {
            name = event.getAuthor().getName();
        }

        System.out.println("Processing command by " + name + ": " + event.getArgs());

        try {

            int id = 0;
            switch (args[0].toLowerCase()) {
                case "edit":
                case "close":
                case "open":
                case "assign":
                case "comment": {
                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        throw new me.pugabyte.jayce.Utils.InvalidArgumentException("You didn't supply a valid issue ID!");
                    }

                    if (id < 1)
                        throw new me.pugabyte.jayce.Utils.InvalidArgumentException("You didn't supply a valid issue ID!");

                    break;
                }
                case "label":
                case "labels": {
                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    }
                    break;
                }
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    new me.pugabyte.jayce.Commands.SubCommands.CreateSubCommand(name, event);
                    break;

                case "search":
                    new me.pugabyte.jayce.Commands.SubCommands.SearchSubCommand(event);
                    break;

                case "edit":
                    new me.pugabyte.jayce.Commands.SubCommands.EditSubCommand(id, event);
                    break;

                case "close":
                case "open":
                    new me.pugabyte.jayce.Commands.SubCommands.ChangeStateSubCommand(id, event);
                    break;

                case "assign":
                    new AssignSubCommand(id, event);
                    break;

                case "comment":
                    new me.pugabyte.jayce.Commands.SubCommands.CommentSubCommand(id, name, event);
                    break;

                case "label":
                case "labels":
                    String action = null;
                    String[] labels = null;
                    if (id == 0)
                        action = "get";
                    else
                        try {
                            action = args[2].toLowerCase();
                            labels = Arrays.copyOfRange(args, 3, args.length);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    new me.pugabyte.jayce.Commands.SubCommands.LabelSubCommand(id, action, labels, event);
                    break;

                default:
                    if (args[0].trim().length() < 1)
                        event.reply("<https://github.com/" + Jayce.CONFIG.githubUser + "/" + Jayce.CONFIG.githubRepo + "/issues>");
                    else
                        event.reply("Invalid action");
                    break;
            }
        } catch (me.pugabyte.jayce.Utils.InvalidArgumentException ex) {
            event.reply(ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            event.reply("Error trying to parse arguments");
        }
    }
}
