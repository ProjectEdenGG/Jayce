package me.pugabyte.jayce.services;

import gg.projecteden.exceptions.EdenException;
import me.pugabyte.jayce.utils.Config;
import me.pugabyte.jayce.utils.Utils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.LabelService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Labels {
	public static final LabelService LABELS = Utils.load(new LabelService());

	public static LabelAction repo() {
		return repo(Config.GITHUB_REPO);
	}

	public static LabelAction repo(String repo) {
		return repo(Config.GITHUB_USER, repo);
	}

	public static LabelAction repo(String user, String repo) {
		return new LabelAction(user, repo);
	}

	public record LabelAction(String user, String repo) {

		public CompletableFuture<Label> get(String labelId) {
			try {
				return CompletableFuture.completedFuture(LABELS.getLabel(user, repo, labelId));
			} catch (IOException ex) {
				throw new EdenException("Error retrieving label " + labelId + " in " + user + "/" + repo, ex);
			}
		}

		public CompletableFuture<List<Label>> getMultiple(List<String> labelIds) {
			return Utils.join(labelIds.stream().map(this::get).toList());
		}

		public CompletableFuture<List<Label>> getAll() {
			try {
				return CompletableFuture.completedFuture(LABELS.getLabels(user, repo));
			} catch (IOException ex) {
				throw new EdenException("Error retrieving labels in " + user + "/" + repo, ex);
			}
		}

		public CompletableFuture<Issue> add(int id, List<String> labelIds) {
			return getMultiple(labelIds).thenCompose(labels ->
				edit(id, issue -> issue.getLabels().addAll(labels)));
		}

		public CompletableFuture<Issue> remove(int id, List<String> labelIds) {
			return getMultiple(labelIds).thenCompose(labels ->
				edit(id, issue -> issue.getLabels().removeAll(labels)));
		}

		public CompletableFuture<Issue> edit(int id, Consumer<Issue> consumer) {
			return Issues.repo(user, repo).edit(id, consumer);
		}

		public CompletableFuture<Issue> save(Issue issue) {
			try {
				return CompletableFuture.completedFuture(LABELS.setLabels(user, repo, String.valueOf(issue.getNumber()), issue.getLabels()))
					.thenCompose(labels -> CompletableFuture.completedFuture(issue));
			} catch (IOException ex) {
				throw new EdenException("Error saving labels of issue " + user + "/" + repo + "#" + issue.getNumber(), ex);
			}
		}

	}

}
