package gg.projecteden.jayce.github;

import com.spotify.github.async.AsyncPage;
import com.spotify.github.v3.clients.RepositoryClient;
import com.spotify.github.v3.issues.Label;
import gg.projecteden.api.common.exceptions.EdenException;
import gg.projecteden.jayce.config.Config;
import gg.projecteden.jayce.github.Issues.RepoIssueContext;
import kotlin.Pair;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static gg.projecteden.api.common.utils.StringUtils.camelCase;
import static gg.projecteden.jayce.Jayce.GITHUB;
import static java.util.stream.StreamSupport.stream;

public class Repos {
	private static final Map<Pair<String, String>, RepositoryClient> clients = new HashMap<>();

	public static RepoContext main() {
		return repo(Config.GITHUB_REPO);
	}

	public static RepoContext repo(Category category) {
		if (category == null)
			throw new EdenException("Cannot determine repo from null category");
		return repo(category.getName());
	}

	public static RepoContext repo(TextChannel channel) {
		if (channel == null)
			throw new EdenException("Cannot determine repo from null channel");
		return repo(camelCase(channel.getName()));
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
				GITHUB.createRepositoryClient(user, repo));
		}

		public RepoIssueContext issues() {
			return new RepoIssueContext(this);
		}

		public @NotNull CompletableFuture<@NotNull List<Label>> listLabels() {
			final Iterable<AsyncPage<Label>> pages = () -> client().listLabels();
			return CompletableFuture.supplyAsync(() -> stream(pages.spliterator(), true)
				.flatMap(page -> stream(page.spliterator(), true))
				.toList());
		}

	}

}
