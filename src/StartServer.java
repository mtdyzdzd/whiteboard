import server.IWhiteboardServer;
import server.WhiteboardServerImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartServer {

    public static void main(String[] args) {
        int port = 8888; // 默认端口

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        try {
            WhiteboardServerImpl server = new WhiteboardServerImpl();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("WhiteboardService", server);

            System.out.println("Server started successfully on port " + port);
            System.out.println("Waiting for client connections...");

        } catch (Exception e) {
            System.out.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}