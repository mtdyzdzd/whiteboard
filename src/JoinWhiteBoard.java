import client.WhiteboardFrame;
import server.IWhiteboardServer;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JoinWhiteBoard {

    public static void main(String[] args) {
        String ip = "localhost";
        int port = 8888;
        String username = "user";

        if (args.length >= 3) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
            username = args[2];
        }

        final String serverIP = ip;
        final int serverPort = port;
        final String name = username;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                WhiteboardFrame frame = new WhiteboardFrame(
                        name, serverIP, serverPort, false);

                if (frame.clientImpl == null) return;

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Registry registry = LocateRegistry.getRegistry(serverIP, serverPort);
                            IWhiteboardServer server =
                                    (IWhiteboardServer) registry.lookup("WhiteboardService");
                            server.requestJoin(name, frame.clientImpl);
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    JOptionPane.showMessageDialog(frame,
                                            "Failed to request join: " + e.getMessage(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }
}