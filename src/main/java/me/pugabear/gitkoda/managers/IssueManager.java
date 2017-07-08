package me.pugabear.gitkoda.managers;

import me.pugabear.gitkoda.commands.*;
import me.pugabear.gitkoda.GitKoda;
import static me.pugabear.gitkoda.GitKoda.SERVICES;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.List;
import java.util.ArrayList;

public class IssueManager
{
	static String repo = "GitKodaTest";
	
	public static int createIssue(String title, String body) {
		try {
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody(body);
			Issue result = SERVICES.issues.createIssue("PugaBear", repo, issue);

			return result.getNumber();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return 0;
		}
	}

	public static boolean editIssue(String id, String what, String content) {
		try {
			Issue issue = SERVICES.issues.getIssue("PugaBear", repo, Integer.parseInt(id));
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
			SERVICES.issues.editIssue("PugaBear", repo, issue);
			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}
	
	public static boolean closeIssue(String id) {
		try {
			Issue issue = SERVICES.issues.getIssue("PugaBear", repo, Integer.parseInt(id));
			SERVICES.issues.editIssue("PugaBear", repo, issue.setState("closed"));
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
