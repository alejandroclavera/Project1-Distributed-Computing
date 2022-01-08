package ws;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WSClient {
    private static final String apiUrl = "http://127.0.0.1:5000/";
    private static String userToken;

    private static HttpURLConnection openHttpConnection(String url) throws IOException {
        URL urlToConnect = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlToConnect.openConnection();
        return conn;
    }

    private static InputStreamReader getResponseBodyStream(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            return new InputStreamReader(conn.getInputStream());
        } else {
            return new InputStreamReader(conn.getErrorStream());
        }
    }

    private static JSONObject getResponseJSONBody(HttpURLConnection conn) throws IOException {
        InputStreamReader responseBodyStream = getResponseBodyStream(conn);

        // Get response body json
        JSONParser parser = new JSONParser();
        JSONObject jsonResponseBody;
        try {
            jsonResponseBody = (JSONObject) parser.parse(getResponseBodyStream(conn));
        } catch (ParseException e) {
            return null;
        }
        return jsonResponseBody;
    }

    private static Response authRequest(String userName, String password, HttpURLConnection conn) throws IOException {
        JSONObject requestBody = new JSONObject();

        // Put post information in json
        requestBody.put("user_name", userName);
        requestBody.put("password", password);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(requestBody.toJSONString().getBytes());
        os.flush();

        // Get json Response body and close the connection
        JSONObject json = getResponseJSONBody(conn);
        conn.disconnect();

        // If the json is null -> bad format of json response
        if (json == null)
            return null;

        // Get status code and store the user token if the response code is 200
        int statusCode = conn.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            userToken = (String) json.get("user-token");
        }

        return new Response(statusCode, json);
    }

    public static Response signup(String userName, String password) throws IOException {
        HttpURLConnection conn = openHttpConnection(apiUrl + "user/signup/");
        return authRequest(userName, password, conn);
    }

    public static Response signing(String userName, String password) throws IOException {
        HttpURLConnection conn = openHttpConnection(apiUrl + "user/signin/");
        return authRequest(userName, password, conn);
    }


    public static Response createContent(JSONObject contentToCreate) throws IOException {
        HttpURLConnection conn = openHttpConnection(apiUrl + "content/");

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("user-token", userToken);

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(contentToCreate.toJSONString().getBytes());
        os.flush();

        // Get json Response body and close the connection
        JSONObject json = getResponseJSONBody(conn);
        conn.disconnect();

        // If the json is null -> bad format of json response
        if (json == null)
            return null;

        return new Response(conn.getResponseCode(), json);
    }

    public static Response updateContent(String id, JSONObject contentModification) throws IOException {
        HttpURLConnection conn = openHttpConnection(apiUrl + "content/" +  id);

        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("user-token", userToken);

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(contentModification.toJSONString().getBytes());
        os.flush();

        // Get json Response body and close the connection
        JSONObject json = getResponseJSONBody(conn);
        conn.disconnect();

        // If the json is null -> bad format of json response
        if (json == null)
            return null;

        return new Response(conn.getResponseCode(), json);
    }

    public static Response deleteContent(String id) throws IOException {
        HttpURLConnection conn = openHttpConnection(apiUrl + "content/" + id);

        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("user-token", userToken);

        // Get json Response body and close the connection
        JSONObject json = getResponseJSONBody(conn);
        conn.disconnect();

        // If the json is null -> bad format of json response
        if (json == null)
            return null;

        return new Response(conn.getResponseCode(), json);
    }
}
