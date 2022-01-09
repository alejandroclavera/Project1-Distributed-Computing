package node;

import common.ConnectionNode;
import common.DataInfo;
import node.logs.LogSystem;
import node.managers.NodeManager;
import node.managers.files.FileManager;
import node.managers.files.FileSystemManger;
import org.json.simple.parser.ParseException;
import ws.Status;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;

public class Node {
    private Registry registry;
    private NodeManager nodeManager;
    private FileManager fileManager;
    private ConnectionNode connectionNode;
    private int port;
    public static boolean isRunning = true;

    public Node(NodeManager nodeManager, FileManager fileManager, int port) throws RemoteException, AlreadyBoundException {
        loadConfigurations();
        this.nodeManager = nodeManager;
        this.fileManager = fileManager;
        this.connectionNode = new NetworkConnexionNode(this.nodeManager);
        this.nodeManager.setConnectionNode(this.connectionNode);
        this.port = port;
        this.registry = startRegistry(port);

        registryNode();
    }

    public Node(int port) throws RemoteException {
        loadConfigurations();
        this.nodeManager = new NodeManager();
        this.connectionNode = new NetworkConnexionNode(this.nodeManager);
        this.nodeManager.setConnectionNode(this.connectionNode);
        this.port = port;
        this.registry = startRegistry(port);
        registryNode();
    }

    public HashMap<String, DataInfo> search() throws RemoteException {
        return nodeManager.search();
    }

    public HashMap<String, DataInfo> search(HashMap<String, String> filterBy) throws RemoteException {
        return nodeManager.search(filterBy);
    }

    public void recognizeContents() {
        nodeManager.recogniceContents();
    }

    public List<DataInfo> getContentList() {
        return nodeManager.getContentsList();
    }

    public List<DataInfo> getNewContentList() {
        return nodeManager.getNewContentList();
    }

    public List<DataInfo> getUpdatedContent() {
        return nodeManager.getContentUpdatedList();
    }

    public List<DataInfo> getDeletedContent() {
        return nodeManager.getContentDeletedList();
    }

    public void download(String hash){
        nodeManager.downloadContent(hash);
    }

    public boolean connectTo(String host, int port){
        return nodeManager.connectTo(host, port);
    }

    public boolean connectTo(String host) {
        return nodeManager.connectTo(host);
    }

    public void disconnect() throws RemoteException, NotBoundException {
        UnicastRemoteObject.unexportObject(connectionNode, true);
        registry.unbind("node");
        isRunning = false;
    }

    public void addMetadata(String hash, HashMap<String, String> metadata) {
        nodeManager.addMetadata(hash, metadata);
    }

    public String getDownloadStatus() {
        return nodeManager.getDownloadStatus();
    }

    private void loadConfigurations() {
        try {
            NodeConfiguration.loadConfigurations();
        } catch (IOException e) {
            LogSystem.logErrorMessage("Can load the configurations");
            System.exit(-1);
        } catch (ParseException e) {
            LogSystem.logErrorMessage("Configuration parse file error");
            System.exit(-1);
        }
    }

    public Status createNewContentInfo(DataInfo dataInfo) {
        return nodeManager.createNewContentInfo(dataInfo);
    }

    public Status updateContentInfo(DataInfo dataInfo) {
        return nodeManager.updateContentInfo(dataInfo.wsId, dataInfo);
    }

    public Status deleteContentInfo(DataInfo dataInfo) {
        return nodeManager.removeContentInfo(dataInfo);
    }

    private void registryNode()  {
        try {
            registry = startRegistry(this.port);
            registry.bind("node", connectionNode);
        } catch (RemoteException e) {
           System.out.println("Error al exportar");
           System.exit(-1);
        } catch (AlreadyBoundException e) {
            System.out.println("Excepcion");
        }
    }


    private Registry startRegistry(Integer port) throws RemoteException {
        try {
            if (port == null)
                port = 1099;
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list();
            // The above call will throw an exception
            // if the registry does not already exist
            return registry;
        } catch (RemoteException ex) {
            // No valid registry at that port.
            LogSystem.logInfoMessage("RMI registry cannot be located ");
            Registry registry = LocateRegistry.createRegistry(port);
            LogSystem.logInfoMessage("RMI registry created at port ");
            return registry;
        }
    }

}
