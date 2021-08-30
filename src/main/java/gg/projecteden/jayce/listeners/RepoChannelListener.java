package gg.projecteden.jayce.listeners;

import com.google.gson.Gson;
import com.spotify.github.v3.comment.Comment;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.listeners.common.DiscordListener;
import gg.projecteden.utils.StringUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static gg.projecteden.utils.TimeUtils.shortDateTimeFormat;
import static java.util.Objects.requireNonNull;

public class RepoChannelListener extends DiscordListener {
	private static final String COMMENT_START = "<!--";
	private static final String COMMENT_END = "-->";

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

	@Override
	public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
		try {
			if (shouldIgnoreGuild(event))
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

			editComment(issues, issueId, message.getId(), comment ->
				issues.editComment(comment.id(), getCommentBody(member, message)));
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

	@Override
	public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
		try {
			if (shouldIgnoreGuild(event))
				return;

			final TextChannel channel = event.getChannel();
			final Category category = channel.getParent();
			final String messageId = event.getMessageId();

			if (category == null)
				return;

			Integer issueId = getIssueId(channel);
			if (issueId == null)
				return;

			final RepoContext repo = Repos.repo(category.getName());
			final RepoIssueContext issues = repo.issues();

			editComment(issues, issueId, messageId, comment ->
				issues.editComment(comment.id(), getDeletedCommentBody(comment)));
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

	private void editComment(RepoIssueContext issues, Integer issueId, String messageId, Consumer<Comment> consumer) {
		issues.listAllComments(issueId).thenAccept(comments -> {
			for (Comment comment : comments)
				if (messageId.equals(CommentMeta.deserialize(comment).getMessageId()))
					consumer.accept(comment);
		});
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
	@RequiredArgsConstructor
	public static class CommentMeta {
		private final String messageId;
		private final String userId;
		private transient String body;

		public String asComment() {
			return String.format("%s%n%s%n%s", COMMENT_START, serialize(), COMMENT_END);
		}

		private String serialize() {
			return StringUtils.toPrettyString(this);
		}

		public static CommentMeta deserialize(String json) {
			return new Gson().fromJson(json, CommentMeta.class);
		}

		public static CommentMeta deserialize(Comment comment) {
			final String text = requireNonNull(comment.body(), "Comment does not have a body");

			final String[] split = text.split(COMMENT_END, 2);
			final String json = split[0].replaceAll(COMMENT_START, "");
			final String body = split[1];
			final CommentMeta meta = deserialize(json);
			meta.setBody(body);
			return meta;
		}
	}

	@NotNull
	private String getCommentBody(Member member, Message message) {
		final String json = new CommentMeta(message.getId(), member.getId()).asComment();
		final String display = "**" + member.getEffectiveName() + "**: " + message.getContentDisplay();
		return String.format("%s%n%s", json, display);
	}

	@NotNull
	private String getDeletedCommentBody(Comment comment) {
		final CommentMeta meta = CommentMeta.deserialize(comment);
		return String.format(
			"%s%n<details><summary>Comment deleted at %s</summary>%n%n%s%n</details>",
			meta.asComment(),
			shortDateTimeFormat(LocalDateTime.now()),
			meta.getBody());
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
