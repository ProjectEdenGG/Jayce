package me.pugabear.GitKoda;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import com.google.gson.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDA;


public class GitKoda {
	
	public static void main(String[] args) throws Exception {
		try {
	        JDA jda = new JDABuilder(AccountType.BOT)
	                .setToken("token")
			        .buildBlocking();
	        jda.addEventListener(new DiscordListener());
/*	        
			IssueService service = new IssueService();
			service.getClient().setOAuth2Token("token");
			
			Issue issue = new Issue();
			issue.setTitle("Test");
			issue.setBody("Hello, GitHub!");
			Issue result = service.createIssue("PugaBear", "GitKodaTest", issue);
*/			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
