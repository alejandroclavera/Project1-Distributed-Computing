package node.managers.ws;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.IOException;;
import java.net.HttpURLConnection;
import java.util.HashMap;

import common.DataInfo;


public class WSClientManager implements WSManager {
    private final String apiUrl = "http://127.0.0.1:5000/";
    private Response lastResponse = null;

    @Override
    public Status signup(String userName, String password) {
        Response response;
        try {
             response = WSClient.signup(userName, password);
        } catch (IOException e) {
            return Status.CONNECTION_ERROR;
        }

        return getStatus(response);
    }

    @Override
    public Status signing(String userName, String password) {
        Response response;
        try {
            response = WSClient.signing(userName, password);
        } catch (IOException e) {
            return Status.CONNECTION_ERROR;
        }
        return getStatus(response);
    }

    public Status createNewContent(DataInfo dataInfo) {
        // Use the web service client to create the new content information
        Response response = null;
        try {
            response = WSClient.createContent(generateRequestBody(dataInfo));
        } catch (IOException e) {
            return Status.CONNECTION_ERROR;
        }
        return getStatus(response);
    }

    public Status updateContent(String id, DataInfo dataInfo) {
        // Use the web service client to create  content
        Response response = null;
        try {
            response = WSClient.updateContent(id, generateRequestBody(dataInfo));
        } catch (IOException e) {
            return Status.CONNECTION_ERROR;
        }
        return getStatus(response);
    }

    public Status deleteContent(String id) {
        // Use the web service client to create  content
        Response response = null;
        try {
            response = WSClient.deleteContent(id);
        } catch (IOException e) {
            return Status.CONNECTION_ERROR;
        }
        return getStatus(response);
    }

    private JSONObject generateRequestBody(DataInfo dataInfo) {
        JSONObject requestBody = new JSONObject();
        String title = (dataInfo.titles.size() > 0) ? dataInfo.titles.get(0) : "";
        requestBody.put("title", title);
        requestBody.put("description", dataInfo.metadata.get("description"));

        // Add metadata information in form of keywords
        JSONArray metadata = new JSONArray();
        requestBody.put("keywords", metadata);
        JSONObject metadataHash = new JSONObject();
        metadataHash.put("keyword", "hash");
        metadataHash.put("value", dataInfo.hash);
        metadata.add(metadataHash);
        dataInfo.metadata.forEach((key,value) -> {
            if (!key.equals("description")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("keyword", key);
                jsonObject.put("value", value);
                metadata.add(jsonObject);
            }
        });
        return requestBody;
    }

    private Status getStatus(Response response) {
        int statusCode;
        if (response == null) {
           return Status.RESPONSE_BODY_ERROR;
        }
        // Store the last response
        lastResponse = response;
        statusCode = response.statusCode;
        if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
           return Status.OK;
        } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
           return Status.BAD_REQUEST;
        } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
           return Status.UNAUTHORIZED;
        } else if (statusCode ==  HttpURLConnection.HTTP_FORBIDDEN) {
           return Status.FORBIDDEN;
        } else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
           return Status.NOT_FOUND;
        }
        return Status.SERVER_ERROR;
    }

}
