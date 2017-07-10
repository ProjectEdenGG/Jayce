package me.pugabear.gitkoda.managers;

import static me.pugabear.gitkoda.GitKoda.SERVICES;
import static me.pugabear.gitkoda.GitKoda.CONFIG;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchIssue;

import java.io.IOException;
import java.util.List;

public class IssueManager
{	
	public static int createIssue(String title, String body, String name)
	{
		try 
		{
			Issue issue = new Issue();
			issue.setTitle(title);
			issue.setBody("**" + name + "**: " + body);
			Issue result = SERVICES.issues.createIssue(CONFIG.githubUser, CONFIG.githubRepo, issue);

			return result.getNumber();
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return 0;
		}
	}

	public static boolean editIssue(String id, String what, String content)
	{
		try 
		{
			Issue issue = SERVICES.issues.getIssue(CONFIG.githubUser, CONFIG.githubRepo, Integer.parseInt(id));
			if (what.equalsIgnoreCase("title")) 
			{
				issue.setTitle(content);
			}
			else if (what.equalsIgnoreCase("body")) 
			{
				issue.setBody(content);
			}
			else {
				return false;
			}

			SERVICES.issues.editIssue(CONFIG.githubUser, CONFIG.githubRepo, issue);
			return true;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean changeState(String id, String state)
	{
		try 
		{
			Issue issue = SERVICES.issues.getIssue(CONFIG.githubUser, CONFIG.githubRepo, Integer.parseInt(id));
			SERVICES.issues.editIssue(CONFIG.githubUser, CONFIG.githubRepo, issue.setState(state));
			return true;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean assign(String id, String user)
	{
		try 
		{
			Issue issue = SERVICES.issues.getIssue(CONFIG.githubUser, CONFIG.githubRepo, Integer.parseInt(id));
			SERVICES.issues.editIssue(CONFIG.githubUser, CONFIG.githubRepo, issue.setAssignee(SERVICES.users.getUser(user)));

			return true;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean comment(String id, String comment, String name)
	{
		try {
			SERVICES.issues.createComment(CONFIG.githubUser, CONFIG.githubRepo, id, "**" + name + "**: " + comment);

			return true;
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static List<SearchIssue> search(String state, String query)
	{
		try
		{
			Repository repo = SERVICES.repos.getRepository(CONFIG.githubUser, CONFIG.githubRepo);
			return SERVICES.issues.searchIssues(repo, state, query);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}