package node;


import common.ConnectionNode;
import common.DataInfo;
import node.managers.NodeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class CommandSystem {

    public static void main(String[] args) throws Exception {
        Node node = new Node(1099);
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        while (!exit){
            String command = sc.nextLine();
            String[] arguments = command.split(" ");
            if(arguments[0].equals("connect")) {
                if (arguments.length == 2)
                    node.connectTo(arguments[1]);
                else
                    node.connectTo(arguments[1], Integer.parseInt(arguments[2]));
            }else if (arguments[0].equals("search")){
                HashMap <String, DataInfo> dataInfo = node.search();
                System.out.println(dataInfo);
            }else if(arguments[0].equals("download")){
                node.download(arguments[1]);
            }else if(arguments[0].equals("exit")) {
                exit = true;
            }
        }
    }
}
