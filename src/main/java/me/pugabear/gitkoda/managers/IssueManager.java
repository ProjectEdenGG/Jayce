package me.pugabear.gitkoda.managers;

import org.eclipse.egit.github.core.Issue;
import java.util.List;

import static me.pugabear.gitkoda.GitKoda.SERVICES;

public class IssueManager
{
	static final String REPO = "GitKodaTest";
	static final String USER = "PugaBear";
	
	public static int createIssue(String title, String body) {
		try {
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody(body);
			Issue result = SERVICES.issues.createIssue(USER, REPO, issue);

			return result.getNumber();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return 0;
		}
	}

	public static boolean editIssue(String id, String what, String content) {
		try {
			Issue issue = SERVICES.issues.getIssue(USER, REPO, Integer.parseInt(id));
			if (what.equalsIgnoreCase("title")) 
			{
				issue.setTitle(content);
			}
			else if (what.equalsIgnoreCase("body")) 
			{
				issue.setBody(content);
			}
			else {
				return false;
			}
			SERVICES.issues.editIssue(USER, REPO, issue);
			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	public static boolean closeIssue(String id) {
		try {
			Issue issue = SERVICES.issues.getIssue(USER, REPO, Integer.parseInt(id));
			SERVICES.issues.editIssue(USER, REPO, issue.setState("closed"));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
