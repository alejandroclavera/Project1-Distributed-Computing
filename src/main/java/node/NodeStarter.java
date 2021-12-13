package node;

import node.commandSystem.CommandSystem;
import node.logs.LogSystem;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class NodeStarter {

    public static void main(String[] args) {
        int port = 1099;
        Node node;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        try {
           node = new Node(port);
           System.out.println("System start in the port: " + port);
           new CommandSystem(node).start();
           node.disconnect();
        } catch (RemoteException e) {
            System.exit(-1);
        } catch (AlreadyBoundException e) {
            System.exit(-1);
            LogSystem.logErrorMessage("Node registered in the port " + port);
        } catch (NotBoundException e) {
            LogSystem.logErrorMessage("Node registered in the port " + port);
        }
    }
}
