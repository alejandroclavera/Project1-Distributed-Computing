package node.commandSystem;


import common.DataInfo;
import node.Node;
import node.managers.NodeManager;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;



public class CommandSystem {
    // Object attributes
    // Colors
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Console header
    private static String header =
            "************************************\n" +
            "*         P2P FILE SHARED          *\n" +
            "************************************\n";

    // Instance attributes
    private Node node;
    private List<DataInfo> options;

    public static void main(String[] args) throws Exception {
        Node node = new Node(1100);
        Node nodeServer = new Node(1099);
        CommandSystem commandSystem = new CommandSystem(node);
        commandSystem.start();
        node.disconnect();
        nodeServer.disconnect();
    }


    public  CommandSystem(Node node) {
        this.node = node;
    }

    public void start() {
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        System.out.println(header);
        while (!exit){
            System.out.print("\r-> ");
            String input = sc.nextLine();
            String[] arguments = input.split(" ");
            String command = arguments[0];
            if(command.equals("connect")) {
                connectCommand(arguments);
            }else if (command.equals("search")){
                searchCommand();
            } else if (command.equals("filterSearch")) {
                filteredSearch();
            }else if(command.equals("download")){
                downloadCommand();
            }else if(command.equals("exit")) {
                exit = true;
            } else {
                errorMessage("command \"" + arguments[0] + "\"");
            }
            resetColors();
        }
        System.out.println("Bye");
    }

    private void connectCommand(String[] arguments) {
        boolean isConnected = false;
        if (arguments.length == 2)
            isConnected = node.connectTo(arguments[1]);
        else
            isConnected = node.connectTo(arguments[1], Integer.parseInt(arguments[2]));

        if (isConnected)
            System.out.println(GREEN + "Connected with:" + arguments[1]);
        else
            errorMessage("can't connect with: " + arguments[1]);
    }

    private void searchCommand() {
        HashMap <String, DataInfo> dataInfo = null;
        try {
            options = new ArrayList<>(node.search().values());
            printOptions();
        } catch (RemoteException e) {
            errorMessage("in the search");
        }
    }

    private void filteredSearch() {
        HashMap<String, String> filterBy = new HashMap<>();
        boolean stop = false;
        Scanner scanner = new Scanner(System.in);
        String keyWord, value;
        while (!stop) {
            System.out.print("Enter keyword: ");
            keyWord = scanner.nextLine();
            System.out.print("Enter value to filter with keyword " + "\"" + keyWord + "\": " );
            value = scanner.nextLine();
            filterBy.put(keyWord, value);
            System.out.print("Enter other keyword to filter yes/no (y/n): ");
            String res = scanner.nextLine().strip().toLowerCase();
            stop = !res.equals("y");
        }
        try {
           options = new ArrayList<>(node.search(filterBy).values());
           printOptions();
        } catch (RemoteException e) {
            errorMessage("in the search");
        }
    }

    private void downloadCommand() {
        Scanner scanner = new Scanner(System.in);
        int indexOption = -1;
        printOptions();
        while (indexOption < 0 || indexOption > options.size()) {
            System.out.print("Select the content index: ");
            indexOption = scanner.nextInt();
            if (indexOption < 0 || indexOption > options.size())  {
                errorMessage("bad index \"" + indexOption + "\"");
            }
        }
        String hash = options.get(indexOption).hash;
        node.download(hash);
    }

    private void errorMessage(String message) {
        System.out.println(RED + "Error " + RESET + message);
    }

    private void resetColors() {
        System.out.println(RESET + "\r");
    }

    private void printOptions() {
        int index = 0;
        for (DataInfo contentInfo : options) {
            System.out.println(index + ": " + contentInfo);
            index +=1;
        }
    }
}
