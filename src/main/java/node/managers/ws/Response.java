package node.managers.ws;

import org.json.simple.JSONObject;

public class Response {
    public final int statusCode;
    public final JSONObject jsonObject;

    public Response(int statusCode, JSONObject jsonObject) {
        this.statusCode = statusCode;
        this.jsonObject = jsonObject;
    }
}
