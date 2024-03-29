package gg.projecteden.jayce.github;

import com.spotify.github.async.AsyncPage;
import com.spotify.github.v3.ImmutableUser;
import com.spotify.github.v3.User;
import com.spotify.github.v3.clients.IssueClient;
import com.spotify.github.v3.clients.SearchClient;
import com.spotify.github.v3.comment.Comment;
import com.spotify.github.v3.issues.ImmutableIssue;
import com.spotify.github.v3.issues.ImmutableLabel;
import com.spotify.github.v3.issues.Issue;
import com.spotify.github.v3.issues.Label;
import com.spotify.github.v3.search.SearchIssue;
import com.spotify.github.v3.search.SearchIssues;
import com.spotify.github.v3.search.requests.ImmutableSearchParameters;
import gg.projecteden.jayce.config.Aliases;
import gg.projecteden.jayce.github.Repos.RepoContext;
import kotlin.Pair;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import static gg.projecteden.api.common.utils.Nullables.isNullOrEmpty;
import static gg.projecteden.api.common.utils.Utils.bash;
import static gg.projecteden.api.common.utils.Utils.mutableCopyOf;
import static gg.projecteden.jayce.Jayce.GITHUB;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("unused")
public class Issues {
	private static final Map<Pair<String, String>, IssueClient> clients = new HashMap<>();
	private static final SearchClient searchClient = GITHUB.createSearchClient();

	public record RepoIssueContext(RepoContext repo) {

		private IssueClient client() {
			return clients.computeIfAbsent(new Pair<>(repo.user(), repo.repo()), $ ->
				repo.client().createIssueClient());
		}

		public ImmutableIssue.Builder of(final Member member, final String title, final String body) {
			final ImmutableIssue.Builder builder = ImmutableIssue.builder()
				.title(title);

			if (isNullOrEmpty(body))
				builder.body(Optional.of("Submitted by **" + member.getEffectiveName() + "**"));
			else
				builder.body(Optional.of("**" + member.getEffectiveName() + "**: " + body));

			return builder;
		}

		public CompletableFuture<Issue> create(final Issue issue) {
			return client().createIssue(issue);
		}

		public CompletableFuture<Issue> get(final int issueId) {
			return client().getIssue(issueId);
		}

		private @NotNull Iterator<AsyncPage<Issue>> list() {
			return client().listIssues();
		}

		public CompletableFuture<List<Issue>> listAll() {
			final Iterable<AsyncPage<Issue>> pages = this::list;
			return CompletableFuture.supplyAsync(() -> stream(pages.spliterator(), true)
				.flatMap(page -> stream(page.spliterator(), true))
				.toList());
		}

		@NotNull
		public ImmutableIssue addAssignees(final ImmutableIssue issue, final Member member) {
			return addAssignees(issue, List.of(Aliases.githubOf(member.getId())));
		}

		@NotNull
		public ImmutableIssue addAssignees(final ImmutableIssue issue, final List<String> userIds) {
			List<User> assignees = mutableCopyOf(issue.assignees());
			userIds.stream().map(userId -> ImmutableUser.builder().login(userId).build()).forEach(assignees::add);
			return issue.withAssignees(assignees);
		}

		@NotNull
		public ImmutableIssue removeAssignees(final ImmutableIssue issue, final Member member) {
			return removeAssignees(issue, List.of(Aliases.githubOf(member.getId())));
		}

		@NotNull
		public ImmutableIssue removeAssignees(final ImmutableIssue issue, final List<String> userIds) {
			List<User> assignees = mutableCopyOf(issue.assignees());
			assignees.removeIf(user -> userIds.stream().anyMatch(userId -> userId.equalsIgnoreCase(user.login())));
			return issue.withAssignees(assignees);
		}

		public CompletableFuture<Issue> assign(final int issueId, final Member member) {
			return edit(issueId, issue -> addAssignees(issue, member));
		}

		public CompletableFuture<Issue> assign(final int issueId, final List<String> userIds) {
			return edit(issueId, issue -> addAssignees(issue, userIds));
		}

		public CompletableFuture<Issue> unassign(final int issueId, final Member member) {
			return unassign(issueId, List.of(Aliases.githubOf(member.getId())));
		}

		public CompletableFuture<Issue> unassign(final int issueId, final List<String> userIds) {
			return edit(issueId, issue -> removeAssignees(issue, userIds));
		}

		public CompletableFuture<Issue> addLabels(final int issueId, final List<String> labelIds) {
			return edit(issueId, issue -> {
				final List<Label> labels = mutableCopyOf(issue.labels());
				labelIds.stream().map(label -> ImmutableLabel.builder().name(label).build()).forEach(labels::add);
				return issue.withLabels(labels);
			});
		}

		public CompletableFuture<Issue> removeLabels(final int issueId, final List<String> labelIds) {
			return edit(issueId, issue -> {
				final List<Label> labels = mutableCopyOf(issue.labels());
				labels.removeIf(label -> labelIds.stream().anyMatch(labelId -> labelId.equalsIgnoreCase(label.name())));
				return issue.withLabels(labels);
			});
		}

		public CompletableFuture<Issue> open(final int issueId) {
			return edit(issueId, IssueState.OPEN::set);
		}

		public CompletableFuture<Issue> close(final int issueId) {
			return edit(issueId, IssueState.CLOSED::set);
		}

		public CompletableFuture<Issue> edit(final int issueId, final Function<ImmutableIssue, ImmutableIssue> consumer) {
			return get(issueId).thenCompose(issue -> save(consumer.apply(ImmutableIssue.copyOf(issue))));
		}

		public CompletableFuture<Issue> save(ImmutableIssue issue) {
			return client().editIssue(issue).thenCompose(response -> completedFuture(issue));
		}

		// Uses `hub` cli since GitHub's REST API does not support
		// transferring issues (only their GraphQL API does)
		public int transfer(final int issueId, String repo) {
			final String command = "./transfer-issue " + repo().repo() + " " + issueId + " " + repo;
			final String result = bash(command);
			final String[] split = result.split("/");
			return Integer.parseInt(split[split.length - 1]);
		}

		public CompletableFuture<Comment> comment(final int issueId, final String text) {
			return client().createComment(issueId, text);
		}

		public CompletableFuture<Void> editComment(final int commentId, final String body) {
			return client().editComment(commentId, body);
		}

		public Iterator<AsyncPage<Comment>> listComments(final int issueId) {
			return client().listComments(issueId);
		}

		public @NotNull CompletableFuture<@NotNull List<Comment>> listAllComments(final int issueId) {
			final Iterable<AsyncPage<Comment>> pages = () -> listComments(issueId);
			return CompletableFuture.supplyAsync(() -> stream(pages.spliterator(), true)
				.flatMap(page -> stream(page.spliterator(), true))
				.toList());
		}

		public CompletableFuture<List<SearchIssue>> search(final String text) {
			return searchClient.issues(ImmutableSearchParameters.builder()
				.q(String.format("repo:%s/%s %s", repo.user(), repo.repo(), text))
				.build()).thenApply(SearchIssues::items);
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

			public String build() {
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
