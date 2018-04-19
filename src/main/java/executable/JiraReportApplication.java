package executable;

import client.JiraServiceDeskClient;
import javafx.util.Pair;
import model.Ticket;
import org.json.simple.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import printer.ExcelPrintService;

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
@Component
public class JiraReportApplication {
    private static final String DATE_PATTERN = "yyyy/MM/dd";
    private final Converter converter;

    public JiraReportApplication(Converter converter) {
        this.converter = converter;
    }

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
        LocalDate lastDayOfMonth = parse(to, DateTimeFormatter.ofPattern(DATE_PATTERN)).with(lastDayOfMonth());

        JiraServiceDeskClient client = JiraServiceDeskClient.builder()
                .setServerUrl(jiraUrl)
                .setUserName(jiraAdminUserName)
                .setPassword(jiraAdminPassword)
                .setProjectKey(projectKey)
                .build();

        HashMap<String, JSONObject> issues = collectIssues(from, to, client);
        Set<Ticket> tickets = new TreeSet<>(Comparator.comparing(Ticket::getTicketKey));
        issues.forEach((key, issue) -> {
            Ticket ticket = createTicketByIssue(client, new Pair<>(key, issue));
            tickets.add(ticket);
        });

        ExcelPrintService excelPrintService = new ExcelPrintService(reportPath);
        excelPrintService.setDate(lastDayOfMonth);
        excelPrintService.printReport(tickets);

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

    private static Ticket createTicketByIssue(JiraServiceDeskClient client, Pair<String, JSONObject> issue) {
        Ticket ticket = new Ticket(issue.getKey());
        ticket.setType((String) ((JSONObject) ((JSONObject) issue.getValue().get("fields")).get("issuetype")).get("name"));
        ticket.setStatus((String) ((JSONObject) ((JSONObject) issue.getValue().get("fields")).get("status")).get("name"));
        String stringDate = (String) ((JSONObject) issue.getValue().get("fields")).get("created");
        LocalDateTime localDateTime = LocalDateTime.parse(stringDate, DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
        ticket.setCreated(localDateTime);


        ticket.setDescription((String) ((JSONObject) issue.getValue().get("fields")).get("description"));
        ticket.setPriority((String) ((JSONObject) ((JSONObject) issue.getValue().get("fields")).get("priority")).get("name"));
        Object paidObj = ((JSONObject) issue.getValue().get("fields")).get("customfield_10501");
        if (paidObj != null)
            ticket.setPaid((String) ((JSONObject) paidObj).get("value"));

        Object minutesOfSupportObj = ((JSONObject) issue.getValue().get("fields")).get("customfield_10502");
        if (minutesOfSupportObj != null)
            ticket.setMinutesOfSupport((Double) minutesOfSupportObj);

        ticket.setElapsedTime(client.getElapsedTimeByIssue(issue.getValue()));
        ticket.setRemainingTime(client.getRemainingTimeByIssue(issue.getValue()));
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
