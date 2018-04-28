package executable;

import client.JiraServiceDeskClient;
import executable.JiraReportApplication.PARAMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import printer.PrintService;

import java.util.EnumMap;

import static executable.JiraReportApplication.PARAMS.*;

/**
 * Created by esuv on 4/29/18
 */
@Component
public class ReportHandler {
    private final EnumMap<PARAMS, String> params;

    @Autowired
    private PrintService printService;

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

        printService.setReportPath(params.get(REPORT_PATH));

    }
}
