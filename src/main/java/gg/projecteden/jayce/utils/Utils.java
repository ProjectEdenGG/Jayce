package gg.projecteden.jayce.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gg.projecteden.jayce.Jayce;
import lombok.SneakyThrows;
import org.eclipse.egit.github.core.service.GitHubService;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Utils {

	public static <T extends GitHubService> T load(T service) {
		service.getClient().setOAuth2Token(Config.GITHUB_TOKEN);
		return service;
	}

	@SneakyThrows
	public static Map<String, String> readConfig(String file) {
		try {
			final String path = Jayce.class.getSimpleName() + FileSystems.getDefault().getSeparator() + file;
			String json = String.join("", Files.readAllLines(Paths.get(path)));
			return new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
			}.getType());
		} catch (Exception ex) {
			ex.printStackTrace();
			return new HashMap<>();
		}
	}

	public static <T> CompletableFuture<List<T>> join(List<CompletableFuture<T>> futures) {
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
			.thenApply($ -> futures.stream().map(CompletableFuture::join).toList());
	}

}
