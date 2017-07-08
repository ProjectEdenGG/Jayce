package me.pugabear.GitKoda;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.service.RepositoryService;

import static me.pugabear.GitKoda.GitKoda.SERVICES;

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

	public static boolean editIssue(String id, String title, String body) {
		try {
			Issue issue = SERVICES.issues.getIssue("PugaBear", repo, Integer.parseInt(id));
			issue.setTitle(title);
			issue.setBody(body);
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
	
	public static boolean changeLabels(String action, String id, String[] labelNames) {
		try {
			Issue issue = SERVICES.issues.getIssue("PugaBear", repo, Integer.parseInt(id));
			if (action.equalsIgnoreCase("add")) {
				List<Label> labels = issue.getLabels();
				for (String label : labelNames) {
					issue.getLabels().add(SERVICES.labels.getLabel("PugaBear", repo, label));
				}
				SERVICES.issues.editIssue("PugaBear", repo, issue);
			} else if (action.equalsIgnoreCase("remove")) {
				List<Label> labels = issue.getLabels();
				int i = 0;
				for (Label label : labels) {
					for (String labelName : labelNames) {
						if (labelName.equalsIgnoreCase(label.getName())) {
							labels.remove(i);
						}
					}
					i++;
				}
			} else if (action.equalsIgnoreCase("set")) {
				List<Label> labels = new ArrayList<Label>();
				for (String label : labelNames) {
					labels.add(SERVICES.labels.getLabel("PugaBear", repo, label));
				}
				SERVICES.issues.editIssue("PugaBear", repo, issue.setLabels(labels));
			} 
			
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
