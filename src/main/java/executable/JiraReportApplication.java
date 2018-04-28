package executable;

import client.JiraServiceDeskClient;
import config.ApplicationContextProvider;
import javafx.util.Pair;
import model.Ticket;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import printer.PrintService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Created by esuv on 4/10/18
 */
@Component
public class JiraReportApplication {

    private static final String DATE_PATTERN = "yyyy/MM/dd";

    public static void main(String[] args) {

        printBanner();

        if (!(args.length == 7)) {
            throw new IllegalArgumentException();
        }

        EnumMap<PARAMS, String> params = new EnumMap<>(PARAMS.class);
        params.put(PARAMS.JIRA_URL, args[0]);
        params.put(PARAMS.JIRA_ADMIN_USER_NAME, args[1]);
        params.put(PARAMS.JIRA_ADMIN_PASSWORD, args[2]);
        params.put(PARAMS.PROJECT_KEY, args[3]);
        params.put(PARAMS.FROM, args[4]);
        params.put(PARAMS.TO, args[5]);
        params.put(PARAMS.REPORT_PATH, args[6]);


        ApplicationContextProvider contextProvider = new ApplicationContextProvider();
        AutowireCapableBeanFactory factory = contextProvider.getApplicationContext().getAutowireCapableBeanFactory();
        factory.autowireBean(params);
        factory.initializeBean(params, "params");
        ReportHandler reportHandler = factory.getBean(ReportHandler.class);
        reportHandler.execute();


        LocalDate lastDayOfMonth = parse(to, DateTimeFormatter.ofPattern(DATE_PATTERN)).with(lastDayOfMonth());


        HashMap<String, JSONObject> issues = collectIssues(from, to, client);
        Set<Ticket> tickets = new TreeSet<>(Comparator.comparing(Ticket::getTicketKey));
        issues.forEach((key, issue) -> {
            Ticket ticket = createTicketByIssue(client, new Pair<>(key, issue));
            tickets.add(ticket);
        });

        printService.setDate(lastDayOfMonth);
        printService.printReport(tickets);

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


    public enum PARAMS {
        JIRA_URL,
        JIRA_ADMIN_USER_NAME,
        JIRA_ADMIN_PASSWORD,
        PROJECT_KEY,
        FROM,
        TO,
        REPORT_PATH
    }
}
