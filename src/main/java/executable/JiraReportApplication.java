package executable;

import client.JiraServiceDeskClient;
import model.Ticket;
import org.json.simple.JSONObject;
import printer.ExcelPrinterService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by esuv on 4/10/18
 */
public class JiraReportApplication {
    private static final String JIRA_URL = "http://37.34.104.30:8080";
    private static final String JIRA_ADMIN_USERNAME = "esuvorov";
    private static final String JIRA_ADMIN_PASSWORD = "esuv";
    private static final String JIRA_PROJECT_KEY = "KMS0D0";

    public static void main(String[] args) throws IOException, URISyntaxException {
        printBanner();

        String from = "2018/04/05";
        String to = "2019/04/07";

        JiraServiceDeskClient client = JiraServiceDeskClient.builder()
                .setServerUrl(JIRA_URL)
                .setUserName(JIRA_ADMIN_USERNAME)
                .setPassword(JIRA_ADMIN_PASSWORD)
                .setProjectKey(JIRA_PROJECT_KEY)
                .build();

        HashMap<String, JSONObject> issues = collectIssues(from, to, client);
        Set<Ticket> tikets = new HashSet<>();
        issues.forEach((key, issue) -> {
            Ticket ticket = createTicketByIssue(client, key, issue);
            tikets.add(ticket);
        });

        ExcelPrinterService excelPrinterService = new ExcelPrinterService();
        excelPrinterService.printReport(tikets);

    }

    private static void printBanner() {
        String intro =
                "**********************************************************************************************\r\n" +
                "* JIRA Java REST Client ('JRJC') example.                                                    *\r\n" +
                "* NOTE: Start JIRA using the Atlassian Plugin SDK before running this example.               *\r\n" +
                "* (for example, use 'atlas-run-standalone --product jira --version 7.6 --data-version 7.6'.) *\r\n" +
                "**********************************************************************************************\r\n";
        System.out.println(intro);
    }

    private static Ticket createTicketByIssue(JiraServiceDeskClient client, String key, JSONObject issue) {
        Ticket ticket = new Ticket(key);
        ticket.setType((String) ((JSONObject) ((JSONObject) issue.get("fields")).get("issuetype")).get("name"));
        ticket.setStatus((String) ((JSONObject) ((JSONObject) issue.get("fields")).get("status")).get("name"));
        ticket.setCreated((String) ((JSONObject) issue.get("fields")).get("created"));
        ticket.setDescription((String) ((JSONObject) issue.get("fields")).get("description"));
        ticket.setPriority((String) ((JSONObject) ((JSONObject) issue.get("fields")).get("priority")).get("name"));
        Object paidObj = ((JSONObject) issue.get("fields")).get("customfield_10501");
        if (paidObj != null)
            ticket.setPaid((String) ((JSONObject) paidObj).get("value"));

        ticket.setRemainingTime(client.getElapsedTimeByIssue(issue));
        ticket.setElapsedTime(client.getRemainingTimeByIssue(issue));
        return ticket;
    }

    private static HashMap<String, JSONObject> collectIssues(String from, String to, JiraServiceDeskClient client) {
        Map<String, JSONObject> created = client.getIssuesBySpecificDateInPeriod("created", from, to);
        HashMap<String, JSONObject> issues =
                new HashMap<>(created);
        issues.putAll(client.getIssuesBySpecificDateInPeriod("resolved", from, to));
        issues.putAll(client.getUnfinishedIssues());
        return issues;
    }
}
