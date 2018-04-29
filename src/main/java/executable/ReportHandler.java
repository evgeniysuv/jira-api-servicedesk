package executable;

import client.JiraServiceDeskClient;
import executable.JiraReportApplication.PARAMS;
import javafx.util.Pair;
import model.Ticket;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import printer.ReportService;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static executable.JiraReportApplication.PARAMS.*;

/**
 * Created by esuv on 4/29/18
 */
@Component
public class ReportHandler {
    private final EnumMap<PARAMS, String> params;

    @Autowired
    private ReportService reportService;

    @Autowired
    private Converter<Pair<String, JSONObject>, Ticket> issueConverter;

    public ReportHandler(EnumMap<PARAMS, String> params) {
        this.params = params;
    }

    public void execute() {
        JiraServiceDeskClient client = JiraServiceDeskClient.builder()
                .setServerUrl(params.get(JIRA_URL))
                .setUserName(params.get(JIRA_ADMIN_USER_NAME))
                .setPassword(params.get(JIRA_ADMIN_PASSWORD))
                .setProjectKey(params.get(PROJECT_KEY))
                .build();


        Map<String, JSONObject> issues = client.getIssuesBySpecificDateInPeriod();
        Set<Ticket> tickets = issues
                .entrySet()
                .stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .map(pair -> issueConverter.convert(pair)).collect(Collectors.toSet());
        reportService.setReportPath(params.get(REPORT_PATH));
        try {
            reportService.printReport(tickets);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
