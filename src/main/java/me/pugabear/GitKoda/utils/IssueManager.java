package me.pugabear.GitKoda;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.service.RepositoryService;

import static me.pugabear.GitKoda.GitKoda.SERVICES;

public class IssueManager
{
	public static int createIssue(String title, String body) {
		try {
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody(body);
			Issue result = SERVICES.issues.createIssue("PugaBear", "GitKodaTest", issue);

			return result.getNumber();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return 0;
		}
	}

	public static boolean editIssue(String id, String title, String body) {
		try {
			Issue issue = SERVICES.issues.getIssue("PugaBear", "GitKodaTest", Integer.parseInt(id));
			issue.setTitle(title);
			issue.setBody(body);
			SERVICES.issues.editIssue("PugaBear", "GitKodaTest", issue);

			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	public static boolean closeIssue(String id) {
		try {
			SERVICES.issues.editIssue("PugaBear", "GitKodaTest", SERVICES.issues.getIssue("PugaBear", "GitKodaTest", Integer.parseInt(id)).setState("closed"));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
