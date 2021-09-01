package gg.projecteden.jayce.commands.common;

import gg.projecteden.jayce.Jayce;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.utils.DiscordId;
import gg.projecteden.utils.Env;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static gg.projecteden.jayce.Jayce.JDA;
import static java.util.Objects.requireNonNull;

public class AppCommandRegistration {

	@SneakyThrows
	public static void registerAll() {
		if (Jayce.get().getEnv() != Env.DEV)
			return;

		register(build());
	}

	private static void register(CommandData command) {
		for (Guild guild : JDA.getGuilds()) {
			try { Thread.sleep(300); } catch (Exception ignored) {}

			if (guild.getId().equals(DiscordId.Guild.PROJECT_EDEN.getId()))
				continue;

			String id = "/" + command.getName() + " | " + guild.getName() + " |";

			Consumer<String> success = action -> System.out.println(id + " ✔ " + action);
			Consumer<String> failure = action -> System.out.println(id + " ✗ " + action);

			/*
			guild.retrieveCommands().complete().forEach(existingCommand -> {
				guild.deleteCommandById(existingCommand.getId()).complete();
				success.accept("DELETE EXISTING");
			});
			*/

			guild.retrieveCommandPrivileges().complete().forEach((existingCommand, privileges) -> {
				for (CommandPrivilege privilege : privileges)
					success.accept("Found privilege " + privilege + " for " + existingCommand + " in " + guild.getName());
			});

			Consumer<Command> setPrivilege = response -> {
				// TODO Not sure this works
				final CommandPrivilege privilege = new CommandPrivilege(Type.ROLE, true, Long.parseLong("278969395831635968"));
				guild.updateCommandPrivilegesById(response.getId(), privilege).submit()
					.thenAccept(response2 -> {
						success.accept("PRIVILEGE");
					}).exceptionally(ex -> {
					failure.accept("PRIVILEGE");
					ex.printStackTrace();
					return null;
				});
			};

			guild.upsertCommand(command).submit()
				.thenAccept(response -> {
					success.accept("COMMAND");

					setPrivilege.accept(response);
				}).exceptionally(ex -> {
				failure.accept("COMMAND");
				ex.printStackTrace();
				return null;
			});
		}
	}

	@NotNull
	@SneakyThrows
	private static CommandData build() {
		final SubcommandData editTitle = new SubcommandData("title", "Edit an issue's title")
			.addOption(OptionType.INTEGER, "id", "Issue Number", true)
			.addOption(OptionType.STRING, "text", "Title", true);
		final SubcommandData editBody = new SubcommandData("body", "Edit an issue's body")
			.addOption(OptionType.INTEGER, "id", "Issue Number", true)
			.addOption(OptionType.STRING, "text", "Body", true);
		final SubcommandGroupData edit = new SubcommandGroupData("edit", "Interact with labels")
			.addSubcommands(editTitle, editBody);

		final SubcommandData labelsAdd = new SubcommandData("add", "Add a label to an issue")
			.addOption(OptionType.INTEGER, "id", "Issue Number", true);
		final SubcommandData labelsRemove = new SubcommandData("remove", "Remove a label from an issue")
			.addOption(OptionType.INTEGER, "id", "Issue Number", true);
		final SubcommandGroupData labels = new SubcommandGroupData("labels", "Interact with labels")
			.addSubcommands(labelsAdd, labelsRemove);

		final SubcommandData assign = new SubcommandData("assign", "Assign a user to an issue")
			.addOption(OptionType.INTEGER, "id", "Issue Number", true);
		final SubcommandData unassign = new SubcommandData("unassign", "Unassign a user from an issue")
			.addOption(OptionType.INTEGER, "id", "Issue Number", true);

		final List<Choice> labelChoices = Repos.main().listLabels().get().stream()
			.map(label -> requireNonNull(label.name()).toLowerCase())
			.map(label -> new Choice(label, label))
			.toList();

		for (int i = 1; i <= 2; i++) {
			final OptionData option = new OptionData(OptionType.STRING, "label" + i, "Label #" + i, i == 1).addChoices(labelChoices);
			labelsAdd.addOptions(option);
			labelsRemove.addOptions(option);
		}

		for (int i = 1; i <= 3; i++) {
			final OptionData option = new OptionData(OptionType.USER, "user" + i, "User #" + i, i == 1);
			assign.addOptions(option);
			unassign.addOptions(option);
		}

		return new CommandData("issues", "Interact with GitHub issues")
			.addSubcommandGroups(edit, labels)
			.addSubcommands(assign, unassign)
			.addSubcommands(
				new SubcommandData("create", "Create an issue")
					.addOption(OptionType.STRING, "title", "Title", true)
					.addOption(OptionType.STRING, "body", "Body", true),
				new SubcommandData("open", "Open an existing issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true),
				new SubcommandData("close", "Close an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true),
				new SubcommandData("comment", "Add a comment to an issue")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true)
					.addOption(OptionType.STRING, "text", "Comment", true),
				new SubcommandData("search", "Search issues")
					.addOption(OptionType.STRING, "query", "Query", true),
				new SubcommandData("transfer", "Transfer an issue to another repository")
					.addOption(OptionType.INTEGER, "id", "Issue Number", true)
					.addOption(OptionType.STRING, "repo", "Destination repository", true)
			);
	}

}
