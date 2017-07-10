package me.pugabear.gitkoda.managers;

import static me.pugabear.gitkoda.GitKoda.SERVICES;
import static me.pugabear.gitkoda.GitKoda.CONFIG;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;

import java.util.List;

public class LabelManager
{
	public static boolean addLabels(String id, String[] labels)
	{
		try
		{
			Issue issue = SERVICES.issues.getIssue(CONFIG.githubUser, CONFIG.githubRepo, Integer.parseInt(id));
			List<Label> issueLabels = issue.getLabels();
			for (String label : labels)
			{
				issueLabels.add(SERVICES.labels.getLabel(CONFIG.githubUser, CONFIG.githubRepo, label));	
			}

			SERVICES.labels.setLabels(CONFIG.githubUser, CONFIG.githubRepo, id, issueLabels);
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean removeLabels(String id, String[] labels)
	{
		try
		{
			Issue issue = SERVICES.issues.getIssue(CONFIG.githubUser, CONFIG.githubRepo, Integer.parseInt(id));
			List<Label> issueLabels = issue.getLabels();
			for (String label : labels)
			{
				issueLabels.remove(SERVICES.labels.getLabel(CONFIG.githubUser, CONFIG.githubRepo, label));
			}

			SERVICES.labels.setLabels(CONFIG.githubUser, CONFIG.githubRepo, id, issueLabels);
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
}