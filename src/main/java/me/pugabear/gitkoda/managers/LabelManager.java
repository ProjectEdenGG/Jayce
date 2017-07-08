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

public class LabelManager
{
	static String repo = "GitKodaTest";
	
	public static boolean addLabels(String id, String[] labels) {
		try {
			Issue issue = SERVICES.issues.getIssue("PugaBear", repo, Integer.parseInt(id));
			List<Label> issueLabels = issue.getLabels();
			for (String label : labels)
			{
				issueLabels.add(SERVICES.labels.getLabel("PugaBear", repo, label));	
			}
			SERVICES.labels.setLabels("PugaBear", repo, id, issueLabels);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static boolean removeLabels(String id, String[] labels) {
		try {
			Issue issue = SERVICES.issues.getIssue("PugaBear", repo, Integer.parseInt(id));
			List<Label> issueLabels = issue.getLabels();
			for (String label : labels)
			{
				issueLabels.remove(SERVICES.labels.getLabel("PugaBear", repo, label));
			}
			SERVICES.labels.setLabels("PugaBear", repo, id, issueLabels);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
