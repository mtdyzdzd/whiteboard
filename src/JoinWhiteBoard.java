import client.ClientImpl;
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
                try {
                    // 先创建窗口
                    WhiteboardFrame frame = new WhiteboardFrame(
                            name, serverIP, serverPort, false);

                    // 申请加入
                    Registry registry = LocateRegistry.getRegistry(serverIP, serverPort);
                    IWhiteboardServer server = (IWhiteboardServer) registry.lookup("WhiteboardService");
                    server.requestJoin(name, frame.clientImpl);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to connect: " + e.getMessage());
                }
            }
        });
    }
}