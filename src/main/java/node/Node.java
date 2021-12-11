package node;

import common.ConnectionNode;
import common.DataInfo;
import node.logs.LogSystem;
import node.managers.NodeManager;
import node.managers.files.FileManager;
import node.managers.files.FileSystemManger;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Node {
    private Registry registry;
    private NodeManager nodeManager;
    private FileManager fileManager;
    private ConnectionNode connectionNode;
    private int port;

    public Node(NodeManager nodeManager, FileManager fileManager, int port) throws RemoteException, AlreadyBoundException {
        this.nodeManager = nodeManager;
        this.fileManager = fileManager;
        this.connectionNode = new NetworkConnexionNode(this.nodeManager);
        this.nodeManager.setConnectionNode(this.connectionNode);
        this.port = port;
        this.registry = startRegistry(port);
        registryNode();

    }

    public Node(int port) throws RemoteException, AlreadyBoundException {
        this.fileManager = new FileSystemManger("contentsClient");
        this.nodeManager = new NodeManager(this.fileManager);
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
    }

    private void registryNode() throws RemoteException, AlreadyBoundException {
        registry = startRegistry(this.port);
        registry.bind("node", connectionNode);
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
