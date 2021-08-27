package me.pugabyte.jayce.services;

import gg.projecteden.exceptions.EdenException;
import me.pugabyte.jayce.utils.Utils;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class Users {
	public static final UserService USERS = Utils.load(new UserService());

	@CheckReturnValue
	public static UserGet get(String username) {
		return new UserGet(username);
	}

	public record UserGet(String username) implements Executor<User> {
		public CompletableFuture<User> execute() {
			try {
				return CompletableFuture.completedFuture(USERS.getUser(username));
			} catch (IOException ex) {
				throw new EdenException("Error retrieving user " + username, ex);
			}
		}

	}

}
