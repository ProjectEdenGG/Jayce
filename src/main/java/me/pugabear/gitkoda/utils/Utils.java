package me.pugabear.gitkoda.utils;

import java.io.BufferedReader;
import java.util.HashMap;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
	
	public static String getToken(String service) {
		String token = null;
		try {
			InputStream in = Utils.class.getResourceAsStream("/" + service + ".token");
			BufferedReader input = new BufferedReader(new InputStreamReader(in));
			token = input.readLine();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return token;
	}
}
