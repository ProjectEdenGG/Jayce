package gg.projecteden.jayce.listeners;

import com.google.gson.Gson;
import com.spotify.github.v3.comment.Comment;
import gg.projecteden.api.common.utils.StringUtils;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import gg.projecteden.jayce.github.Repos;
import gg.projecteden.jayce.github.Repos.RepoContext;
import gg.projecteden.jayce.listeners.common.DiscordListener;
import gg.projecteden.jayce.utils.Utils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static gg.projecteden.api.common.utils.TimeUtils.shortDateTimeFormat;
import static java.util.Objects.requireNonNull;

public class RepoChannelListener extends DiscordListener {

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		try {
			if (shouldIgnore(event))
				return;

			if (!(event.getChannel() instanceof TextChannel channel))
				return;

			final Category category = channel.getParentCategory();
			final Message message = event.getMessage();
			final Member member = event.getMember();

			if (member == null || category == null)
				return;

			int issueId = Utils.getIssueId(channel);
			if (issueId < 1)
				return;

			final RepoContext repo = Repos.repo(category);
			final RepoIssueContext issues = repo.issues();

			verifyArchive(channel, issues, issueId).thenRun(() ->
				issues.comment(issueId, CommentMeta.asComment(message))).exceptionally(ex -> {
					handleException(event, ex);
					return null;
				});
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

	@Override
	public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
		try {
			if (shouldIgnoreGuild(event))
				return;

			if (!(event.getChannel() instanceof TextChannel channel))
				return;

			final Category category = channel.getParentCategory();
			final Message message = event.getMessage();
			final Member member = event.getMember();

			if (member == null || category == null)
				return;

			int issueId = Utils.getIssueId(channel);
			if (issueId < 1)
				return;

			final RepoContext repo = Repos.repo(category);
			final RepoIssueContext issues = repo.issues();

			editComment(issues, issueId, message.getId(), comment ->
				issues.editComment(comment.id(), CommentMeta.asComment(message)));
		} catch (Exception ex) {
			handleException(event, ex);
		}
	}

	@Override
	public void onMessageDelete(@NotNull MessageDeleteEvent event) {
		try {
			if (shouldIgnoreGuild(event))
				return;

			if (!(event.getChannel() instanceof TextChannel channel))
				return;

			final Category category = channel.getParentCategory();
			final String messageId = event.getMessageId();

			if (category == null)
				return;

			int issueId = Utils.getIssueId(channel);
			if (issueId < 1)
				return;

			final RepoContext repo = Repos.repo(category);
			final RepoIssueContext issues = repo.issues();

			editComment(issues, issueId, messageId, comment ->
				issues.editComment(comment.id(), CommentMeta.asDeletedComment(comment)));
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

	@Data
	@RequiredArgsConstructor
	public static class CommentMeta {
		private final String messageId;
		private final String userId;
		private transient String body;

		private static final String COMMENT_START = "<!--";
		private static final String COMMENT_END = "-->";

		public String asCommentComment() {
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

		@NotNull
		public static String asComment(Message message) {
			String content = message.getContentDisplay();
			for (Attachment attachment : message.getAttachments())
				content += System.lineSeparator() + "![Attachment](" + attachment.getUrl() + ")";

			return asComment(message.getId(), requireNonNull(message.getMember()), content);
		}

		public static String asComment(String eventId, Member member, String content) {
			final String json = new CommentMeta(eventId, member.getId()).asCommentComment();
			final String display = "**" + member.getEffectiveName() + "**: " + content;
			return String.format("%s%n%s", json, display);
		}

		@NotNull
		public static String asDeletedComment(Comment comment) {
			final CommentMeta meta = CommentMeta.deserialize(comment);
			return String.format(
				"%s%n<details><summary>Comment deleted at %s</summary>%n%n%s%n</details>",
				meta.asCommentComment(),
				shortDateTimeFormat(LocalDateTime.now()),
				meta.getBody());
		}
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
