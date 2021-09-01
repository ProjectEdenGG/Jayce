package gg.projecteden.jayce.commands.common.exceptions;

import gg.projecteden.exceptions.EdenException;

public class AppCommandException extends EdenException {

	public AppCommandException(String message) {
		super(message);
	}

	public AppCommandException(String message, Throwable cause) {
		super(message, cause);
	}

}
