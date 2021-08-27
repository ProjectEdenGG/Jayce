package gg.projecteden.jayce.services;

import gg.projecteden.exceptions.EdenException;
import gg.projecteden.jayce.utils.Utils;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class Users {
	public static final UserService USERS = Utils.load(new UserService());

	public static CompletableFuture<User> get(String username) {
		try {
			return CompletableFuture.completedFuture(USERS.getUser(username));
		} catch (IOException ex) {
			throw new EdenException("Error retrieving user " + username, ex);
		}
	}

}
