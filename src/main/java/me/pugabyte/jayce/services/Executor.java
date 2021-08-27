package me.pugabyte.jayce.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Executor<T> {

	CompletableFuture<T> execute();

	static <T> CompletableFuture<List<T>> join(List<CompletableFuture<T>> futures) {
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
			.thenApply($ -> futures.stream().map(CompletableFuture::join).toList());
	}

}
