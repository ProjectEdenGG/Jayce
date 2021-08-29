package gg.projecteden.jayce.listeners;

import com.google.gson.Gson;
import com.spotify.github.v3.comment.Comment;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.listeners.common.DiscordListener;
import gg.projecteden.utils.StringUtils;
import lombok.Data;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class RepoChannelListener extends DiscordListener {

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		try {
			final TextChannel channel = event.getChannel();
			final Category category = channel.getParent();
			final Message message = event.getMessage();
			final Member member = event.getMember();

			if (member == null || category == null)
				return;

			if (member.getUser().isBot())
				return;

			if (shouldIgnore(event))
				return;

			final String channelName = channel.getName();
			final String channelPrefix = "(?i)" + category.getName() + "-";
			if (!channelName.matches(channelPrefix + "\\d+"))
				return;

			int issueId = Integer.parseInt(channelName.replaceAll(channelPrefix, ""));

			final RepoContext repo = Repos.repo(category.getName());
			final RepoIssueContext issues = repo.issues();
			verifyArchive(channel, issues, issueId);
			issues.comment(issueId, getCommentBody(member, message)).exceptionally(ex -> {
				handleException(event, ex);
				return null;
			});
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

	@Data
	public static class CommentMeta {
		private final String messageId;
		private final String userId;

		public String serialize() {
			return StringUtils.toPrettyString(this);
		}

		public static CommentMeta deserialize(String json) {
			return new Gson().fromJson(json, CommentMeta.class);
		}
	}

	@NotNull
	private String getCommentBody(Member member, Message message) {
		final String json = new CommentMeta(message.getId(), member.getId()).serialize();
		final String display = "**" + member.getEffectiveName() + "**: " + message.getContentDisplay();
		final String nl = System.lineSeparator();
		return "<!--" + nl + json + nl + "-->" + nl + display;
	}

	private void verifyArchive(TextChannel channel, RepoIssueContext issues, int issueId) {
		if (true) return; // TODO

		issues.listAllComments(issueId).thenAccept(comments -> {
			for (Comment comment : comments) {

			}
		});
	}

}
