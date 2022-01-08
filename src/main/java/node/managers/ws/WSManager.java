package node.managers.ws;

import common.DataInfo;
import ws.Status;

public interface WSManager {
    public Status signup(String userName, String password);
    public Status signing(String userName, String password);
    Status updateContent(String id, DataInfo dataInfo);
    Status createNewContent(DataInfo dataInfo);
    Status deleteContent(String id);
}
