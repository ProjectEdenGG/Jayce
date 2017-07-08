package me.pugabear.gitkoda.managers;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import java.util.List;

import static me.pugabear.gitkoda.GitKoda.SERVICES;

public class LabelManager
{
	static final String REPO = "GitKodaTest";
	static final String USER = "PugaBear";
	
	public static boolean addLabels(String id, String[] labels) {
		try {
			Issue issue = SERVICES.issues.getIssue(USER, REPO, Integer.parseInt(id));
			List<Label> issueLabels = issue.getLabels();
			for (String label : labels)
			{
				issueLabels.add(SERVICES.labels.getLabel(USER, REPO, label));	
			}
			SERVICES.labels.setLabels(USER, REPO, id, issueLabels);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static boolean removeLabels(String id, String[] labels) {
		try {
			Issue issue = SERVICES.issues.getIssue(USER, REPO, Integer.parseInt(id));
			List<Label> issueLabels = issue.getLabels();
			for (String label : labels)
			{
				issueLabels.remove(SERVICES.labels.getLabel(USER, REPO, label));
			}
			SERVICES.labels.setLabels(USER, REPO, id, issueLabels);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
