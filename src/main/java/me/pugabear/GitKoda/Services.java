package me.pugabear.GitKoda;

import org.eclipse.egit.github.core.service.IssueService;

public class Services
{
	IssueService issueService = new IssueService();
	
	Services()
	{
		issueService.getClient().setOAuth2Token(Utils.getToken("github"));
	}
}
