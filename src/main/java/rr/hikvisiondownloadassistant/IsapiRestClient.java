// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rr.hikvisiondownloadassistant.Model.CMSearchDescription;
import rr.hikvisiondownloadassistant.Model.CMSearchResult;
import rr.hikvisiondownloadassistant.Model.SearchMatchItem;
import rr.hikvisiondownloadassistant.Model.TimeSpan;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static rr.hikvisiondownloadassistant.DateConverter.apiStringToNextSecond;

@Getter
@RequiredArgsConstructor
public class IsapiRestClient {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final XmlMapper xmlMapper = new XmlMapper();

    private final String host;
    private final String username;
    private final String password;

    public List<SearchMatchItem> searchMedia(String fromDate, String toDate, int trackId) throws IOException, InterruptedException {
        List<SearchMatchItem> allResults = new LinkedList<>();
        CMSearchResult searchResult;
        final int maxResults = 50;
        int searchResultPosition = 0;

        while (true) {
            searchResult = doHttpRequest(
                    POST,
                    "/ISAPI/ContentMgmt/search",
                    getSearchRequestBodyXml(fromDate, toDate, trackId, searchResultPosition, maxResults),
                    CMSearchResult.class
            );

            List<SearchMatchItem> matches = searchResult.getMatchList();
            if (matches != null) {
                allResults.addAll(matches);
            }
            if (searchResult.isResponseStatus() && searchResult.getResponseStatusStrg().equalsIgnoreCase("more")) {
                if (searchResult.getVersion().equals("1.0")) {
                    // XXX: version 1.0 seems does not support searchResultPosition well, we need update fromDate
                    fromDate = apiStringToNextSecond(matches.get(matches.size() - 1).getTimeSpan().getEndTime());
                }
                else {
                    searchResultPosition += maxResults;
                }
                continue;
            }
            break;
        };

        return allResults;
    }

    private String getSearchRequestBodyXml(String fromDate, String toDate, int trackId, int searchResultPosition, int maxResults) throws JsonProcessingException {
        return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                CMSearchDescription.builder()
                        .searchID(UUID.randomUUID().toString())
                        .maxResults(maxResults)
                        .searchResultPosition(searchResultPosition)
                        .timeSpan(List.of(TimeSpan.builder()
                                .startTime(fromDate)
                                .endTime(toDate)
                                .build()))
                        .trackID(List.of(trackId))
                        .build()
        );
    }

    private <T> T doHttpRequest(String requestMethod, String requestPath, String body, Class<T> resultClass) throws IOException, InterruptedException {
        // Make the first request without an authorization header so we can get the digest challenge response.
        // See https://tools.ietf.org/html/rfc2617
        HttpResponse<String> unauthorizedResponse = doHttpRequestWithAuthHeader(requestMethod, requestPath, body, null);
        if (unauthorizedResponse.statusCode() != 401) {
            throw new RuntimeException("Expected to get a 401 digest auth challenge response but didn't");
        }

        // Calculate the authorization digest value
        String authorizationHeaderValue = new DigestAuth(unauthorizedResponse.headers(), requestMethod, requestPath, username, password)
                .getAuthorizationHeaderValue();

        // Resend the request
        HttpResponse<String> response = doHttpRequestWithAuthHeader(requestMethod, requestPath, body, authorizationHeaderValue);
        if (response.statusCode() == 401) {
            throw new RuntimeException("Could not authenticate. Wrong username or password?");
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("Expected to get successful response but got response code " + response.statusCode());
        }

        // Avoid a jackson parsing error where it doesn't like empty lists
        String workaroundForEmptyResult = response.body().replaceAll("<matchList>\\s+</matchList>", "<matchList/>");

        // Return the parsed response
        return xmlMapper.readValue(workaroundForEmptyResult, resultClass);
    }

    private HttpResponse<String> doHttpRequestWithAuthHeader(String requestMethod, String path, String body, String authHeaderValue) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://" + host + path))
                .header("Accept", "application/xml");

        if (requestMethod.equals(POST)) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
        }

        if (requestMethod.equals(GET)) {
            requestBuilder.GET();
        }

        if (authHeaderValue != null) {
            requestBuilder.header("Authorization", authHeaderValue);
        }

        HttpRequest request = requestBuilder.build();

//        System.err.println("Request Method: " + requestMethod);
//        System.err.println("Request URI: " + request.uri());
//        System.err.println("Request Headers: " + request.headers().map());
//        System.err.println("Request Body:\n" + body);

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

//        System.err.println("Response Code: " + response.statusCode());
//        System.err.println("Response Headers: " + response.headers().map());
//        System.err.println("Response Body:\n" + response.body());

        return response;
    }

}
