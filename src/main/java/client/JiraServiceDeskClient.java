package client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import exception.ServiceDeskElapsedTimeNotFound;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Created by esuv on 4/10/18
 */
public class JiraServiceDeskClient {
    private static final String JIRA_LATEST_API = "/rest/api/latest/search?jql=project=";
    private Client client;

    private String serverUrl;
    private String userName;
    private String password;
    private String projectKey;
    private JSONParser jsonParser;

    private JiraServiceDeskClient() {

    }

    public static Builder builder() {
        return new JiraServiceDeskClient().new Builder();
    }

    public class Builder {

        private Builder() {

        }

        public Builder setServerUrl(String serverUrl) {
            JiraServiceDeskClient.this.serverUrl = serverUrl;
            return this;
        }

        public Builder setUserName(String userName) {
            JiraServiceDeskClient.this.userName = userName;
            return this;
        }

        public Builder setPassword(String password) {
            JiraServiceDeskClient.this.password = password;
            return this;
        }

        public Builder setProjectKey(String projectKey) {
            JiraServiceDeskClient.this.projectKey = projectKey;
            return this;
        }

        public JiraServiceDeskClient build() {
            Assert.notNull(serverUrl, "Jira server address must not be null");
            Assert.notNull(userName, "User login must not be null");
            Assert.notNull(password, "Password must not be null");
            Assert.notNull(projectKey, "Project key must not be null");
            jsonParser = new JSONParser();
            client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter(userName, password));
            System.out.println(String.format("Logging in to %s with username '%s'", serverUrl, userName));
            return JiraServiceDeskClient.this;
        }
    }

    public Map<String, JSONObject> getIssuesBySpecificDateInPeriod(String specificDate, String from, String to) {
        String url = serverUrl + JIRA_LATEST_API + projectKey +
                "%20and%20" + specificDate + "%20%3E%3D%20%22" + from +
                "%22%20and%20" + specificDate + "%20%3C%3D%20%22" + to + "%22";
        JSONObject jsonObject = getResponse(url);
        return extractIssues(jsonObject);
    }

    public Map<String, JSONObject> getUnfinishedIssues() {
        String url = serverUrl + JIRA_LATEST_API + projectKey + "%20and%20status%20not%20in%20(%22Closed%22)";
        JSONObject jsonObject = getResponse(url);
        return extractIssues(jsonObject);
    }

    public String getElapsedTimeByIssue(JSONObject issue) {
        String url = getServiceDeskUrl(issue, "customfield_10319");
        JSONObject response = getResponse(url);
        return getElapsedTime(response);
    }

    public Long getRemainingTimeByIssue(JSONObject issue) {
        String url = getServiceDeskUrl(issue, "customfield_10319");
        JSONObject response = getResponse(url);
        Long remainingTime = getRemainingTime(response);
        if (remainingTime == null) {
            url = getServiceDeskUrl(issue, "customfield_10320");
            response = getResponse(url);
            List completedCycles = (List) response.get("completedCycles");
            if (!completedCycles.isEmpty()) {
                remainingTime = (Long) ((JSONObject) ((JSONObject) completedCycles.get(0))
                        .get("remainingTime"))
                        .get("millis");
            }
        }
        return remainingTime;
    }

    private Long getRemainingTime(JSONObject response) {
        JSONObject cycle = (JSONObject) response.get("ongoingCycle");
        if (cycle == null) {
            JSONArray cycleArray = (JSONArray) response.get("completedCycles");
            if (!cycleArray.isEmpty()) {
                cycle = (JSONObject) cycleArray.get(0);
                return (Long) ((JSONObject) cycle.get("remainingTime")).get("millis");
            }
        } else {
            return (Long) ((JSONObject) cycle.get("remainingTime")).get("millis");
        }
        throw new ServiceDeskElapsedTimeNotFound();
    }

    private String getElapsedTime(JSONObject response) {
        JSONObject cycle = (JSONObject) response.get("ongoingCycle");
        if (cycle == null) {
            JSONArray cycleArray = (JSONArray) response.get("completedCycles");
            if (!cycleArray.isEmpty()) {
                cycle = (JSONObject) cycleArray.get(0);
                return (String) ((JSONObject) cycle.get("elapsedTime")).get("friendly");
            }
        } else {
            return (String) ((JSONObject) cycle.get("elapsedTime")).get("friendly");
        }
        throw new ServiceDeskElapsedTimeNotFound();
    }


    private String getServiceDeskUrl(JSONObject parse, String field) {
        return ((JSONObject) ((JSONObject) ((JSONObject) parse
                .get("fields"))
                .get(field))
                .get("_links"))
                .get("self").toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, JSONObject> extractIssues(JSONObject jsonObject) {
        List<JSONObject> issues = (List<JSONObject>) jsonObject.get("issues");
        return issues.stream()
                .collect(Collectors.toMap(issue -> (String) issue.get("key"), issue -> issue, (a, b) -> b));
    }

    private JSONObject getResponse(String url) {
        WebResource webResource = client.resource(url);
        String jsonResponse = webResource.type(APPLICATION_JSON_TYPE).accept(APPLICATION_JSON_TYPE).get(String.class);
        return parseJSON(jsonResponse);
    }

    private JSONObject parseJSON(String jsonResponse) {
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonResponse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
