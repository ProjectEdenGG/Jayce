package me.pugabyte.jayce.utils;

public class InvalidArgumentException extends Exception {
	private static final long serialVersionUID = 350342632708492047L;

	public InvalidArgumentException(String message) {
		super(message);
	}
}
