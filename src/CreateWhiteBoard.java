import client.WhiteboardFrame;
import server.WhiteboardServerImpl;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CreateWhiteBoard {

    public static void main(String[] args) {

        String ip = "localhost";
        int port = 8888;
        String username = "Admin";

        if (args.length >= 3) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
            username = args[2];
        }

        try {
            WhiteboardServerImpl serverImpl = new WhiteboardServerImpl();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("WhiteboardService", serverImpl);
            System.out.println("Whiteboard server started on port " + port);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to start server: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String serverIP = ip;
        final int serverPort = port;
        final String name = username;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WhiteboardFrame(name, serverIP, serverPort, true);
            }
        });
    }
}