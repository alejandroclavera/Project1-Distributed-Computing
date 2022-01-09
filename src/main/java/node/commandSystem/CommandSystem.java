package node.commandSystem;


import common.DataInfo;
import node.Node;
import node.NodeConfiguration;
import ws.Status;
import node.managers.ws.WSClientManager;


import java.io.IOException;
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


    public CommandSystem(Node node) {
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
            } else if (command.equals("status")) {
                System.out.println("Download Status");
                System.out.println(node.getDownloadStatus());
            } else if (command.equals("config")) {
                System.out.println("NODE CONFIGURATION");
                System.out.println(NodeConfiguration.getParams());
            }else if (command.equals("editConfig")) {
                editOptionsCommand();
            }else if (command.equals("saveConfig")) {
                try {
                    NodeConfiguration.saveConfiguration();
                    System.out.println(GREEN + "Config saved");
                } catch (IOException e) {
                    errorMessage("can't save the configurations");
                }
            } else if (command.equals("nodeContents")){
                printNodeContents();
            } else if(command.equals("recognize")) {
                System.out.println("in process...");
                node.recognizeContents();
            } else if (command.equals("addMetadata")) {
                addMetadataCommand();
            } else if (command.equals("signup")) {
                signupCommand();
            }else if (command.equals("signing")) {
                signingCommand();
            }else if (command.equals("addContent")) {
                addNewContentInWS();
            }else if (command.equals("updateContent")) {
                updateContentInWS();
            }else if(command.equals("exit")) {
                exit = true;
            } else {
                errorMessage("command \"" + arguments[0] + "\"");
            }
            resetColors();
        }
        System.out.println("Bye");
    }

    private void signupCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("User name: ");
        String userName = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        Status signingStatus = new WSClientManager().signup(userName, password);
        if (signingStatus == Status.OK) {
            System.out.println("User registered");
        } else if (signingStatus == Status.BAD_REQUEST) {
            System.out.println("Invalid username or password");
        } else {
            System.out.println("Internal server error");
        }
    }

    private void signingCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("User name: ");
        String userName = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        Status signingStatus = new WSClientManager().signing(userName, password);
        if (signingStatus == Status.OK) {
            System.out.println("User logged");
        } else if (signingStatus == Status.BAD_REQUEST) {
            System.out.println("Invalid username or password");
        } else {
            System.out.println("Internal server error");
        }
    }

    private void connectCommand(String[] arguments) {
        boolean isConnected = false;
        if (arguments.length == 2)
            isConnected = node.connectTo(arguments[1]);
        else
            isConnected = node.connectTo(arguments[1], Integer.parseInt(arguments[2]));

        if (isConnected)
            System.out.println(GREEN + "connected with:" + arguments[1]);
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
        try {
            while (indexOption < 0 || indexOption > options.size()) {
                System.out.print("Select the content index: ");
                indexOption = scanner.nextInt();
                if (indexOption < 0 || indexOption > options.size())  {
                    errorMessage("bad index \"" + indexOption + "\"");
                } else {
                    String hash = options.get(indexOption).hash;
                    node.download(hash);
                }
            }
        } catch (Exception e) {
            errorMessage("bad index \"" + indexOption + "\"");
        }
    }

    private void editOptionsCommand() {
        boolean stop = false;
        Scanner scanner = new Scanner(System.in);
        String param, value;
        while (!stop) {
            System.out.println(NodeConfiguration.getParams());
            System.out.print("Enter configuration param: ");
            param = scanner.nextLine();
            System.out.print("Enter value to filter with keyword " + "\"" + param + "\": " );
            value = scanner.nextLine();
            editOptions(param, value);
            System.out.print("Enter other keyword to filter yes/no (y/n): ");
            String res = scanner.nextLine().strip().toLowerCase();
            stop = !res.equals("y");
        }
    }

    private void editOptions(String param, String value) {
        if (param.equals("contentDirectory")){
            NodeConfiguration.contentDirectory = value;
        } else {
            try {
                int numericValue = Integer.parseInt(value);
                if (param.equals("numBytesChunk")) {
                    NodeConfiguration.numBytesChunk = numericValue;
                } else if (param.equals("numMaxDownloadThreads")) {
                    NodeConfiguration.numMaxDownloadThreads = numericValue;
                } else if (param.equals("numMaxUploadThreads")) {
                    NodeConfiguration.numMaxUploadThreads = numericValue;
                } else if (param.equals("chunkWindowSize")) {
                    NodeConfiguration.chunkWindowSize = numericValue;
                } else if (param.equals("windowsTimeout")) {
                    NodeConfiguration.windowsTimeout = numericValue;
                }else if (param.equals("maxTryWindow")) {
                    NodeConfiguration.maxTryWindow = numericValue;
                }else if (param.equals("connectionResponseTimeout")) {
                    NodeConfiguration.connectionResponseTimeout = numericValue;
                } else {
                    errorMessage("bad param \"" + param + "\"");
                }
            } catch (Exception e) {
                errorMessage("bad value \"" + value + "\"");
            }
        }
    }

    private void addMetadataCommand() {
        Scanner scanner = new Scanner(System.in);
        int indexOption = -1;
        boolean stop = false;
        HashMap<String, String> metadata = new HashMap<>();
        List<DataInfo> nodeContents = node.getContentList();
        printNodeContents();
        while (indexOption < 0 || indexOption > nodeContents.size()) {
            System.out.print("Select the content index: ");
            indexOption = scanner.nextInt();
            if (indexOption < 0 || indexOption > nodeContents.size())  {
                errorMessage("bad index \"" + indexOption + "\"");
            }
        }
        String param, value;
        // Reset the scanner to clean the input buffer
        scanner = new Scanner(System.in);
        while (!stop) {
            System.out.print("Enter metadata keyword: ");
            param = scanner.nextLine();
            System.out.print("Enter value to the keyword " + "\"" + param + "\": " );
            value = scanner.nextLine();
            metadata.put(param, value);
            System.out.print("Enter other metadata keyword yes/no (y/n): ");
            String res = scanner.nextLine().strip().toLowerCase();
            stop = !res.equals("y");
        }
        node.addMetadata(nodeContents.get(indexOption).hash, metadata);
        Status status = node.updateContentInfo(nodeContents.get(indexOption));
        printMessageFromStatus(status);
    }

    private void addNewContentInWS() {
        Scanner scanner = new Scanner(System.in);
        int indexOption = -1;
        printNewNodeContents();
        while (indexOption < 0 || indexOption > node.getNewContentList().size()) {
            System.out.print("Select the content index: ");
            indexOption = scanner.nextInt();
            if (indexOption < 0 || indexOption > node.getNewContentList().size())  {
                errorMessage("bad index \"" + indexOption + "\"");
            }
        }
        DataInfo dataInfo = node.getNewContentList().get(indexOption);
        Status status = node.createNewContentInfo(dataInfo);
        printMessageFromStatus(status);
    }

    private void updateContentInWS() {
        Scanner scanner = new Scanner(System.in);
        int indexOption = -1;
        printUpdatedContents();
        List<DataInfo> updatedContents = node.getUpdatedContent();
        while (indexOption < 0 || indexOption > updatedContents.size()) {
            System.out.print("Select the content index: ");
            indexOption = scanner.nextInt();
            if (indexOption < 0 || indexOption > updatedContents.size())  {
                errorMessage("bad index \"" + indexOption + "\"");
            }
        }
        DataInfo dataInfo = updatedContents.get(indexOption);
        Status status = node.updateContentInfo(dataInfo);
        printMessageFromStatus(status);
    }

    private void deleteContentInWS() {
        Scanner scanner = new Scanner(System.in);
        int indexOption = -1;
        printNewNodeContents();
        while (indexOption < 0 || indexOption > node.getNewContentList().size()) {
            System.out.print("Select the content index: ");
            indexOption = scanner.nextInt();
            if (indexOption < 0 || indexOption > node.getNewContentList().size())  {
                errorMessage("bad index \"" + indexOption + "\"");
            }
        }
        DataInfo dataInfo = node.getNewContentList().get(indexOption);
        Status status = node.deleteContentInfo(dataInfo);
        printMessageFromStatus(status);
    }

    private void errorMessage(String message) {
        System.out.println(RED + "Error " + RESET + message);
    }

    private void resetColors() {
        System.out.println(RESET + "\r");
    }

    private void printOptions() {
        int index = 0;
        if (options == null) {
            options = new ArrayList<>();
        }
        for (DataInfo contentInfo : options) {
            System.out.println(index + ": " + contentInfo);
            index +=1;
        }
    }

    private void printNodeContents() {
        System.out.println("Node Contents");
        int index = 0;
        for (DataInfo dataInfo : node.getContentList()) {
            System.out.println(index + ": " + dataInfo);
            index += 1;
        }
    }

    private void printNewNodeContents() {
        System.out.println("New Contents");
        int index = 0;
        for (DataInfo dataInfo : node.getNewContentList()) {
            System.out.println(index + ": " + dataInfo);
            index += 1;
        }
    }

    private void printUpdatedContents() {
        System.out.println("Updated Contents");
        int index = 0;
        for (DataInfo dataInfo : node.getUpdatedContent()) {
            System.out.println(index + ": " + dataInfo);
            index += 1;
        }
    }

    private void printMessageFromStatus(Status status) {
        if (status == Status.CONNECTION_ERROR) {
            errorMessage("Can't connect with the WS");
        } else if (status == Status.NOT_FOUND) {
            errorMessage("Not found");
        } else if (status == Status.BAD_REQUEST) {
            errorMessage("bad request");
        } else if (status == Status.UNAUTHORIZED) {
            errorMessage("Must be in logged status");
        } else if (status == Status.FORBIDDEN) {
            errorMessage("You do not have permissions to do this operation");
        } else if(status == Status.RESPONSE_BODY_ERROR) {
            errorMessage("A error in the WS response");
        } else if (status == Status.SERVER_ERROR) {
            errorMessage("Internal server error");
        }
    }
}
