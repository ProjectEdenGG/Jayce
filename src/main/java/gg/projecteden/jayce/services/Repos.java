package gg.projecteden.jayce.services;

import com.google.common.collect.ImmutableList;
import com.spotify.github.async.AsyncPage;
import com.spotify.github.v3.clients.RepositoryClient;
import com.spotify.github.v3.issues.Label;
import gg.projecteden.jayce.services.Issues.RepoIssueContext;
import gg.projecteden.jayce.utils.Config;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static gg.projecteden.jayce.Jayce.GITHUB;

public class Repos {
	private static final Map<Pair<String, String>, RepositoryClient> clients = new HashMap<>();

	public static RepoContext main() {
		return repo(Config.GITHUB_REPO);
	}

	public static RepoContext repo(String repo) {
		return repo(Config.GITHUB_USER, repo);
	}

	public static RepoContext repo(String user, String repo) {
		return new RepoContext(user, repo);
	}

	public record RepoContext(String user, String repo) {

		public RepositoryClient client() {
			return clients.computeIfAbsent(new Pair<>(user, repo), $ ->
				GITHUB.createRepositoryClient(Config.GITHUB_USER, Config.GITHUB_REPO));
		}

		public RepoIssueContext issues() {
			return new RepoIssueContext(this);
		}

		public @NotNull CompletableFuture<@NotNull List<Label>> listLabels() {
			final Iterator<AsyncPage<Label>> iterator = client().listLabels();
			if (!iterator.hasNext())
				return CompletableFuture.completedFuture(new ArrayList<>());

			final CompletableFuture<List<Label>> future = new CompletableFuture<>();
			final List<Label> labels = new ArrayList<>();
			final AtomicReference<Consumer<AsyncPage<Label>>> consumer = new AtomicReference<>();

			consumer.set(page -> {
				labels.addAll(ImmutableList.copyOf(page));

				page.hasNextPage().thenAccept(hasNextPage -> {
					if (hasNextPage)
						page.nextPage().thenAccept(next -> consumer.get().accept(next));
					else
						future.complete(labels);
				});
			});

			consumer.get().accept(iterator.next());

			return future;
		}

	}

}
