package gg.projecteden.jayce.services;

import com.spotify.github.v3.ImmutableUser;
import com.spotify.github.v3.User;
import com.spotify.github.v3.clients.IssueClient;
import com.spotify.github.v3.clients.SearchClient;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.issues.ImmutableIssue;
import com.spotify.github.v3.issues.ImmutableLabel;
import com.spotify.github.v3.issues.Issue;
import com.spotify.github.v3.issues.Label;
import com.spotify.github.v3.search.SearchIssues;
import com.spotify.github.v3.search.requests.ImmutableSearchParameters;
import gg.projecteden.jayce.services.Repos.RepoContext;
import gg.projecteden.jayce.utils.Utils;
import kotlin.Pair;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import static gg.projecteden.jayce.Jayce.GITHUB;
import static gg.projecteden.jayce.utils.Utils.mutableCopyOf;

@SuppressWarnings("unused")
public class Issues {
	private static final Map<Pair<String, String>, IssueClient> clients = new HashMap<>();
	private static final SearchClient searchClient = GITHUB.createSearchClient();

	public record RepoIssueContext(RepoContext repo) {

		private IssueClient client() {
			return clients.computeIfAbsent(new Pair<>(repo.user(), repo.repo()), $ ->
				repo.client().createIssueClient());
		}

		public CompletableFuture<Issue> create(final Issue issue) {
			return client().createIssue(issue);
		}

		public CompletableFuture<Issue> get(final int issueId) {
			return client().getIssue(issueId);
		}

		public CompletableFuture<Issue> assign(final int issueId, final List<String> userIds) {
			return edit(issueId, issue -> {
				List<User> assignees = mutableCopyOf(issue.assignees());
				userIds.stream().map(userId -> ImmutableUser.builder().login(userId).build()).forEach(assignees::add);
				return issue.withAssignees(assignees);
			});
		}

		public CompletableFuture<Issue> addLabels(final int issueId, final List<String> labelIds) {
			return edit(issueId, issue -> {
				final List<Label> labels = Utils.mutableCopyOf(issue.labels());
				labelIds.stream().map(label -> ImmutableLabel.builder().name(label).build()).forEach(labels::add);
				return issue.withLabels(labels);
			});
		}

		public CompletableFuture<Issue> removeLabels(final int issueId, final List<String> labelIds) {
			return edit(issueId, issue -> {
				final List<Label> labels = Utils.mutableCopyOf(issue.labels());
				labels.removeIf(label -> labelIds.stream().anyMatch(labelId -> labelId.equalsIgnoreCase(label.name())));
				return issue.withLabels(labels);
			});
		}

		public CompletableFuture<Issue> edit(final int issueId, final Function<ImmutableIssue, ImmutableIssue> consumer) {
			return get(issueId).thenCompose(issue -> save(consumer.apply(ImmutableIssue.copyOf(issue))));
		}

		public CompletableFuture<Issue> save(ImmutableIssue issue) {
			return client().editIssue(issue).thenCompose(response -> CompletableFuture.completedFuture(issue));
		}

		public CompletableFuture<Comment> comment(final int issueId, final String text) {
			return client().createComment(issueId, text);
		}

		public CompletableFuture<SearchIssues> search(final String text) {
			return searchClient.issues(ImmutableSearchParameters.builder()
				.q(String.format("repo:%s/%s %s", repo.user(), repo.repo(), text))
				.build());
		}

		public IssueUrl url() {
			return new IssueUrl(-1);
		}

		public IssueUrl url(Issue issue) {
			return new IssueUrl(Objects.requireNonNull(issue.number(), "This issue does not have a number"));
		}

		public IssueUrl url(final int issueId) {
			return new IssueUrl(issueId);
		}

		@RequiredArgsConstructor
		public class IssueUrl {
			private final int issueId;
			private boolean embed = true;

			public IssueUrl embed(boolean embed) {
				this.embed = embed;
				return this;
			}

			public String get() {
				final String number = issueId > 0 ? String.valueOf(issueId) : "";
				String url = String.format("https://github.com/%s/%s/issues/%s", repo.user(), repo.repo(), number);
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

		public ImmutableIssue set(ImmutableIssue issue) {
			return issue.withState(name());
		}
	}

	@AllArgsConstructor
	public enum IssueField {
		TITLE(ImmutableIssue::withTitle),
		BODY((issue, text) -> issue.withBody(Optional.of(text))),
		;

		private final BiFunction<ImmutableIssue, String, ImmutableIssue> consumer;

		public ImmutableIssue edit(ImmutableIssue issue, String text) {
			return consumer.apply(issue, text);
		}
	}

}
