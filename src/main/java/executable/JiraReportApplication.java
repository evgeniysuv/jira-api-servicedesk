package executable;

import client.JiraServiceDeskClient;
import model.Ticket;
import org.json.simple.JSONObject;
import printer.ExcelPrinterService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.LocalDate.parse;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Created by esuv on 4/10/18
 */
public class JiraReportApplication {
    public static void main(String[] args) throws IOException {
        printBanner();

        if (!(args.length == 7))
            throw new IllegalArgumentException();

        String jiraUrl = args[0];
        String jiraAdminUserName = args[1];
        String jiraAdminPassword = args[2];
        String projectKey = args[3];
        String from = args[4];
        String to = args[5];
        String reportPath = args[6];
        LocalDate lastDayOfMonth = parse(to, DateTimeFormatter.ofPattern("yyyy/MM/dd")).with(lastDayOfMonth());

        JiraServiceDeskClient client = JiraServiceDeskClient.builder()
                .setServerUrl(jiraUrl)
                .setUserName(jiraAdminUserName)
                .setPassword(jiraAdminPassword)
                .setProjectKey(projectKey)
                .build();

        HashMap<String, JSONObject> issues = collectIssues(from, to, client);
        Set<Ticket> tickets = new TreeSet<>(Comparator.comparing(Ticket::getTicketKey));
        issues.forEach((key, issue) -> {
            Ticket ticket = createTicketByIssue(client, key, issue);
            tickets.add(ticket);
        });

        ExcelPrinterService excelPrinterService = new ExcelPrinterService(reportPath);
        excelPrinterService.setDate(lastDayOfMonth);
        excelPrinterService.printReport(tickets);

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
        String stringDate = (String) ((JSONObject) issue.get("fields")).get("created");
        LocalDateTime localDateTime = LocalDateTime.parse(stringDate, DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
        ticket.setCreated(localDateTime);


        ticket.setDescription((String) ((JSONObject) issue.get("fields")).get("description"));
        ticket.setPriority((String) ((JSONObject) ((JSONObject) issue.get("fields")).get("priority")).get("name"));
        Object paidObj = ((JSONObject) issue.get("fields")).get("customfield_10501");
        if (paidObj != null)
            ticket.setPaid((String) ((JSONObject) paidObj).get("value"));

        Object minutesOfSupportObj = ((JSONObject) issue.get("fields")).get("customfield_10502");
        if (minutesOfSupportObj != null)
            ticket.setMinutesOfSupport((Double) minutesOfSupportObj);

        ticket.setElapsedTime(client.getElapsedTimeByIssue(issue));
        ticket.setRemainingTime(client.getRemainingTimeByIssue(issue));
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
