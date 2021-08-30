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
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class RepoChannelListener extends DiscordListener {

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		try {
			if (shouldIgnore(event))
				return;

			final TextChannel channel = event.getChannel();
			final Category category = channel.getParent();
			final Message message = event.getMessage();
			final Member member = event.getMember();

			if (member == null || category == null)
				return;

			Integer issueId = getIssueId(channel);
			if (issueId == null)
				return;

			final RepoContext repo = Repos.repo(category.getName());
			final RepoIssueContext issues = repo.issues();
			verifyArchive(channel, issues, issueId).thenRun(() ->
				issues.comment(issueId, getCommentBody(member, message)).exceptionally(ex -> {
					handleException(event, ex);
					return null;
				}));
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

	@Nullable
	private Integer getIssueId(TextChannel channel) {
		final String channelName = channel.getName();
		final String channelPrefix = "(?i)" + requireNonNull(channel.getParent()).getName() + "-";
		if (!channelName.matches(channelPrefix + "\\d+"))
			return null;

		return Integer.parseInt(channelName.replaceAll(channelPrefix, ""));
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
		return String.format("<!--%n%s%n-->%n%s", json, display);
	}

	private CompletableFuture<Void> verifyArchive(TextChannel channel, RepoIssueContext issues, int issueId) {
		// TODO
		if (true)
			return CompletableFuture.completedFuture(null);

		return issues.listAllComments(issueId).thenAccept(comments -> {
			for (Comment comment : comments) {

			}
		}).thenCompose(null);
	}

}
