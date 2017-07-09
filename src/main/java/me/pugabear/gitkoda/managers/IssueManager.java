package me.pugabear.gitkoda.managers;

import static me.pugabear.gitkoda.GitKoda.SERVICES;
import static me.pugabear.gitkoda.GitKoda.USER;
import static me.pugabear.gitkoda.GitKoda.REPO;

import org.eclipse.egit.github.core.Issue;

public class IssueManager
{	
	public static int createIssue(String title, String body, String name) {
		try {
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody("**" + name + "**: " + body);
			Issue result = SERVICES.issues.createIssue(USER, REPO, issue);

			return result.getNumber();
		} catch (Exception ex) {
			ex.printStackTrace();
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
			ex.printStackTrace();
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
	
	public static boolean openIssue(String id) {
		try {
			Issue issue = SERVICES.issues.getIssue(USER, REPO, Integer.parseInt(id));
			SERVICES.issues.editIssue(USER, REPO, issue.setState("open"));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static boolean assign(String id, String user) {
		try {
			Issue issue = SERVICES.issues.getIssue(USER, REPO, Integer.parseInt(id));
			SERVICES.issues.editIssue(USER, REPO, issue.setAssignee(SERVICES.users.getUser(user)));

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static boolean comment(String id, String comment, String name) {
		try {
			SERVICES.issues.createComment(USER, REPO, id, "**" + name + "**: " + comment);

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
