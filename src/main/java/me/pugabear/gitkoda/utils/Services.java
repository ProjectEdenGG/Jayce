package me.pugabear.gitkoda.utils;

import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;

public class Services
{
	public IssueService issues = new IssueService();
	public LabelService labels = new LabelService();
	
	public Services()
	{
		issues.getClient().setOAuth2Token(Utils.getToken("github"));
		labels.getClient().setOAuth2Token(Utils.getToken("github"));
	}
}
