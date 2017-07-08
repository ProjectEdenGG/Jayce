package me.pugabear.GitKoda;

import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;

public class Services
{
	IssueService issues = new IssueService();
	LabelService labels = new LabelService();
	
	Services()
	{
		issues.getClient().setOAuth2Token(Utils.getToken("github"));
		labels.getClient().setOAuth2Token(Utils.getToken("github"));
	}
}
