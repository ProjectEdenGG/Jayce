package me.pugabyte.jayce.services;

import gg.projecteden.exceptions.EdenException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.pugabyte.jayce.utils.Aliases;
import me.pugabyte.jayce.utils.Config;
import me.pugabyte.jayce.utils.Utils;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.service.IssueService;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Issues {
	public static final IssueService ISSUES = Utils.load(new IssueService());

	public static IssueAction repo() {
		return repo(Config.GITHUB_REPO);
	}

	public static IssueAction repo(String repo) {
		return repo(Config.GITHUB_USER, repo);
	}

	public static IssueAction repo(String user, String repo) {
		return new IssueAction(user, repo);
	}

	public record IssueAction(String user, String repo) {

		@CheckReturnValue
		public IssueCreate create(Issue issue) {
			return new IssueCreate(issue);
		}

		@CheckReturnValue
		public IssueGet get(int id) {
			return new IssueGet(id);
		}

		@CheckReturnValue
		public IssueAssign assign(int id, String user) {
			return new IssueAssign(id, user);
		}

		@CheckReturnValue
		public IssueEdit edit(int id, Consumer<Issue> consumer) {
			return new IssueEdit(id, consumer);
		}

		@CheckReturnValue
		public IssueSave save(Issue issue) {
			return new IssueSave(issue);
		}

		@CheckReturnValue
		public IssueComment comment(int id, String text) {
			return new IssueComment(id, text);
		}

		@CheckReturnValue
		public IssueSearch search(String text) {
			return new IssueSearch(text);
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
		public class IssueCreate implements Executor<Issue> {
			private final Issue issue;

			public CompletableFuture<Issue> execute() {
				try {
					return CompletableFuture.completedFuture(ISSUES.createIssue(user, repo, issue));
				} catch (IOException ex) {
					ex.printStackTrace();
					throw new EdenException("Error creating new issue in " + user + "/" + repo);
				}
			}

		}

		@AllArgsConstructor
		public class IssueGet implements Executor<Issue> {
			private final int id;

			public CompletableFuture<Issue> execute() {
				try {
					return CompletableFuture.completedFuture(ISSUES.getIssue(user, repo, id));
				} catch (IOException ex) {
					throw new EdenException("Error retrieving issue " + user + "/" + repo + "#" + id, ex);
				}
			}

		}

		@AllArgsConstructor
		public class IssueAssign implements Executor<Issue> {
			private final int id;
			private final String user;

			public CompletableFuture<Issue> execute() {
				return Aliases.githubOf(user).thenCompose(user ->
					edit(id, issue -> issue.setAssignee(user)).execute());
			}

		}

		@AllArgsConstructor
		public class IssueEdit implements Executor<Issue> {
			private final int id;
			private final Consumer<Issue> consumer;

			public CompletableFuture<Issue> execute() {
				return get(id).execute().thenCompose(result -> {
					consumer.accept(result);
					return save(result).execute();
				});
			}

		}

		@AllArgsConstructor
		public class IssueSave implements Executor<Issue> {
			private final Issue issue;

			public CompletableFuture<Issue> execute() {
				try {
					return CompletableFuture.completedFuture(ISSUES.editIssue(user, repo, issue));
				} catch (IOException ex) {
					throw new EdenException("Error saving issue " + user + "/" + repo + "#" + issue.getNumber(), ex);
				}
			}

		}

		@AllArgsConstructor
		public class IssueComment implements Executor<Comment> {
			private final int id;
			private final String text;

			public CompletableFuture<Comment> execute() {
				try {
					return CompletableFuture.completedFuture(ISSUES.createComment(user, repo, id, text));
				} catch (IOException ex) {
					throw new EdenException("Error creating comment on issue " + user + "/" + repo + "#" + id, ex);
				}
			}

		}

		@AllArgsConstructor
		public class IssueSearch implements Executor<List<SearchIssue>> {
			private final String text;

			public CompletableFuture<List<SearchIssue>> execute() {
				return Repos.repo(user, repo).get().execute().thenCompose(repo -> {
					try {
						return CompletableFuture.completedFuture(ISSUES.searchIssues(repo, IssueState.ofQuery(text).name(), text));
					} catch (IOException ex) {
						throw new EdenException("Error searching issues in " + user + "/" + repo, ex);
					}
				});
			}

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

			public String execute() {
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
