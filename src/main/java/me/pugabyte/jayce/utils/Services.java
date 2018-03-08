package me.pugabyte.jayce.utils;

import me.pugabyte.jayce.Jayce;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

public class Services {
	public final IssueService issues = new IssueService();
	public final LabelService labels = new LabelService();
	public final UserService users = new UserService();
	public final RepositoryService repos = new RepositoryService();

	public Services() {
		issues.getClient().setOAuth2Token(Jayce.CONFIG.githubToken);
		labels.getClient().setOAuth2Token(Jayce.CONFIG.githubToken);
		users.getClient().setOAuth2Token(Jayce.CONFIG.githubToken);
		repos.getClient().setOAuth2Token(Jayce.CONFIG.githubToken);
	}
}
