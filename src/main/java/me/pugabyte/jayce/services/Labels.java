package me.pugabyte.jayce.services;

import gg.projecteden.exceptions.EdenException;
import lombok.AllArgsConstructor;
import me.pugabyte.jayce.utils.Config;
import me.pugabyte.jayce.utils.Utils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.LabelService;

import javax.annotation.CheckReturnValue;
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

		public @CheckReturnValue
		LabelGet get(String id) {
			return new LabelGet(id);
		}

		public @CheckReturnValue
		LabelGetMultiple getMultiple(List<String> labelIds) {
			return new LabelGetMultiple(labelIds);
		}

		public @CheckReturnValue
		LabelGetAll getAll() {
			return new LabelGetAll();
		}

		public @CheckReturnValue
		LabelAdd add(int id, List<String> labelIds) {
			return new LabelAdd(id, labelIds);
		}

		public @CheckReturnValue
		LabelRemove remove(int id, List<String> labelIds) {
			return new LabelRemove(id, labelIds);
		}

		public @CheckReturnValue
		LabelEdit edit(int id, Consumer<Issue> consumer) {
			return new LabelEdit(id, consumer);
		}

		public @CheckReturnValue
		LabelSave save(Issue issue) {
			return new LabelSave(issue);
		}

		@AllArgsConstructor
		public class LabelGet implements Executor<Label> {
			private final String labelId;

			public CompletableFuture<Label> execute() {
				try {
					return CompletableFuture.completedFuture(LABELS.getLabel(user, repo, labelId));
				} catch (IOException ex) {
					throw new EdenException("Error retrieving label " + labelId + " in " + user + "/" + repo, ex);
				}
			}

		}

		@AllArgsConstructor
		public class LabelGetMultiple implements Executor<List<Label>> {
			private final List<String> labelIds;

			public CompletableFuture<List<Label>> execute() {
				return Executor.join(labelIds.stream().map(labelId -> get(labelId).execute()).toList());
			}

		}

		@AllArgsConstructor
		public class LabelGetAll implements Executor<List<Label>> {
			public CompletableFuture<List<Label>> execute() {
				try {
					return CompletableFuture.completedFuture(LABELS.getLabels(user, repo));
				} catch (IOException ex) {
					throw new EdenException("Error retrieving labels in " + user + "/" + repo, ex);
				}
			}

		}

		@AllArgsConstructor
		public class LabelAdd implements Executor<Issue> {
			private final int id;
			private final List<String> labelIds;

			public CompletableFuture<Issue> execute() {
				return getMultiple(labelIds).execute().thenCompose(labels ->
					edit(id, issue -> issue.getLabels().addAll(labels)).execute());
			}

		}

		@AllArgsConstructor
		public class LabelRemove implements Executor<Issue> {
			private final int id;
			private final List<String> labelIds;

			public CompletableFuture<Issue> execute() {
				return getMultiple(labelIds).execute().thenCompose(labels ->
					edit(id, issue -> issue.getLabels().removeAll(labels)).execute());
			}

		}

		@AllArgsConstructor
		public class LabelEdit implements Executor<Issue> {
			private final int id;
			private final Consumer<Issue> consumer;

			public CompletableFuture<Issue> execute() {
				return Issues.repo(user, repo).edit(id, consumer).execute();
			}

		}

		@AllArgsConstructor
		public class LabelSave implements Executor<Issue> {
			private final Issue issue;

			public CompletableFuture<Issue> execute() {
				try {
					return CompletableFuture.completedFuture(LABELS.setLabels(user, repo, String.valueOf(issue.getNumber()), issue.getLabels()))
						.thenCompose(labels -> CompletableFuture.completedFuture(issue));
				} catch (IOException ex) {
					throw new EdenException("Error saving labels of issue " + user + "/" + repo + "#" + issue.getNumber(), ex);
				}
			}

		}

	}

}
