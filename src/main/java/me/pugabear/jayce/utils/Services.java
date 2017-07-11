package me.pugabear.jayce.utils;

import static me.pugabear.jayce.Jayce.CONFIG;

import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

public class Services
{
	public IssueService issues = new IssueService();
	public LabelService labels = new LabelService();
	public UserService users = new UserService();
	public RepositoryService repos = new RepositoryService();

	public Services()
	{
		issues.getClient().setOAuth2Token(CONFIG.githubToken);
		labels.getClient().setOAuth2Token(CONFIG.githubToken);
		users.getClient().setOAuth2Token(CONFIG.githubToken);
		repos.getClient().setOAuth2Token(CONFIG.githubToken);
	}
}
