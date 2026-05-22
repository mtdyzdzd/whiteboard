import client.WhiteboardFrame;
import javax.swing.*;

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

        final String serverIP = ip;
        final int serverPort = port;
        final String name = username;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WhiteboardFrame(name, serverIP, serverPort,true);
            }
        });
    }
}