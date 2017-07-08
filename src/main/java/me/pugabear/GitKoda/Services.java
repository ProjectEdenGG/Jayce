package me.pugabear.GitKoda;

import org.eclipse.egit.github.core.service.IssueService;

public class Services
{
	IssueService issues = new IssueService();
	
	Services()
	{
		issues.getClient().setOAuth2Token(Utils.getToken("github"));
	}
}
