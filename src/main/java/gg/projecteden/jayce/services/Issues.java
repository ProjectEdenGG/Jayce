package gg.projecteden.jayce.services;

import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.utils.Aliases;
import gg.projecteden.jayce.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Issues {
	public static final IssueService ISSUES = Utils.load(new IssueService());

	public record IssueAction(String user, String repo) {

		public CompletableFuture<Issue> create(Issue issue) {
			try {
				return CompletableFuture.completedFuture(ISSUES.createIssue(user, repo, issue));
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new EdenException("Error creating new issue in " + user + "/" + repo);
			}
		}

		public CompletableFuture<Issue> get(int id) {
			try {
				return CompletableFuture.completedFuture(ISSUES.getIssue(user, repo, id));
			} catch (IOException ex) {
				throw new EdenException("Error retrieving issue " + user + "/" + repo + "#" + id, ex);
			}
		}

		public CompletableFuture<Issue> assign(int id, String userId) {
			return Aliases.githubOf(userId).thenCompose(user ->
				edit(id, issue -> issue.setAssignee(user)));
		}

		public CompletableFuture<Issue> edit(int id, Consumer<Issue> consumer) {
			return get(id).thenCompose(result -> {
				consumer.accept(result);
				return save(result);
			});
		}

		public CompletableFuture<Issue> save(Issue issue) {
			try {
				return CompletableFuture.completedFuture(ISSUES.editIssue(user, repo, issue));
			} catch (IOException ex) {
				throw new EdenException("Error saving issue " + user + "/" + repo + "#" + issue.getNumber(), ex);
			}
		}

		public CompletableFuture<Comment> comment(int id, String text) {
			try {
				return CompletableFuture.completedFuture(ISSUES.createComment(user, repo, id, text));
			} catch (IOException ex) {
				throw new EdenException("Error creating comment on issue " + user + "/" + repo + "#" + id, ex);
			}
		}

		public CompletableFuture<List<SearchIssue>> search(String text) {
			return Repos.repo(user, repo).get().thenCompose(repo -> {
				try {
					return CompletableFuture.completedFuture(ISSUES.searchIssues(repo, IssueState.ofQuery(text).name(), text));
				} catch (IOException ex) {
					throw new EdenException("Error searching issues in " + user + "/" + repo, ex);
				}
			});
		}

		public IssueUrl url() {
			return new IssueUrl(-1);
		}

		public IssueUrl url(Issue issue) {
			return new IssueUrl((int) issue.getId());
		}

		public IssueUrl url(int id) {
			return new IssueUrl(id);
		}

		@AllArgsConstructor
		@RequiredArgsConstructor
		public class IssueUrl {
			private final int id;
			private boolean embed = true;

			public IssueUrl embed(boolean embed) {
				this.embed = embed;
				return this;
			}

			public String get() {
				String url = String.format("https://github.com/%s/%s/issues/%s", user, repo, id > 0 ? String.valueOf(id) : "");
				if (!embed)
					url = "<" + url + ">";
				return url;
			}

		}

	}

	public enum IssueState {
		OPEN,
		CLOSED,
		;

		public static IssueState ofQuery(String text) {
			for (IssueState state : values())
				if (text.toLowerCase().contains("is:" + state.name().toLowerCase()))
					return state;

			return OPEN;
		}
	}

	@AllArgsConstructor
	public enum IssueField {
		TITLE(Issue::setTitle),
		BODY(Issue::setBody),
		;

		private final BiConsumer<Issue, String> consumer;

		public void edit(Issue issue, String text) {
			consumer.accept(issue, text);
		}
	}

}
