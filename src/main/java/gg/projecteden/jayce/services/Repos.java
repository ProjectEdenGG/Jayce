package gg.projecteden.jayce.services;

import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.services.Issues.RepoIssueContext;
import gg.projecteden.jayce.services.Labels.RepoLabelContext;
import gg.projecteden.jayce.utils.Config;
import gg.projecteden.jayce.utils.Utils;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class Repos {
	public static final RepositoryService REPOS = Utils.load(new RepositoryService());

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

		public RepoIssueContext issues() {
			return new RepoIssueContext(user, repo);
		}

		public RepoLabelContext labels() {
			return new RepoLabelContext(user, repo);
		}

		public CompletableFuture<Repository> get() {
			try {
				return CompletableFuture.completedFuture(REPOS.getRepository(user, repo));
			} catch (IOException ex) {
				throw new EdenException("Error retrieving repository " + user + "/" + repo, ex);
			}
		}

	}

}
