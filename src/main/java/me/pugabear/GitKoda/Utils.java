package me.pugabear.GitKoda;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.BufferedReader;
import java.util.HashMap;
import java.io.InputStream;
import java.io.InputStreamReader;

import static me.pugabear.GitKoda.GitKoda.SERVICES;

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
	
	public static int createIssue(String title, String body) {
		try {
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody(body);
			Issue result = SERVICES.issueService.createIssue("PugaBear", "GitKodaTest", issue);

			return result.getNumber();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return 0;
		}
	}
	
	public static boolean closeIssue(String id) {
		try {
			System.out.println("Closing issue #" + id);
			SERVICES.issueService.editIssue("PugaBear", "GitKodaTest", SERVICES.issueService.getIssue("PugaBear", "GitKodaTest", Integer.parseInt(id)).setState("closed"));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
