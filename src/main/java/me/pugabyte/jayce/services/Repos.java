package me.pugabyte.jayce.services;

import gg.projecteden.exceptions.EdenException;
import me.pugabyte.jayce.utils.Config;
import me.pugabyte.jayce.utils.Utils;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class Repos {
	public static final RepositoryService REPOS = Utils.load(new RepositoryService());

	public static RepoAction repo() {
		return repo(Config.GITHUB_REPO);
	}

	public static RepoAction repo(String repo) {
		return repo(Config.GITHUB_USER, repo);
	}

	public static RepoAction repo(String user, String repo) {
		return new RepoAction(user, repo);
	}

	public record RepoAction(String user, String repo) {

		public @CheckReturnValue
		RepoGet get() {
			return new RepoGet();
		}

		public class RepoGet implements Executor<Repository> {
			public CompletableFuture<Repository> execute() {
				try {
					return CompletableFuture.completedFuture(REPOS.getRepository(user, repo));
				} catch (IOException ex) {
					throw new EdenException("Error retrieving repository " + user + "/" + repo, ex);
				}
			}

		}

	}

}
