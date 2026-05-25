package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;


public class WhiteboardServerImpl extends UnicastRemoteObject
        implements IWhiteboardServer {

    String currentImage = "";
    ConcurrentHashMap<String, IClient> clients = new ConcurrentHashMap<>();
    String managerName;

    public WhiteboardServerImpl() throws RemoteException {
        super();
    }

    public void broadcastImage(String base64Image,String senderName) throws RemoteException {
        currentImage = base64Image;
        System.out.println("Canvas update from:" + senderName);
        for (String name : new ArrayList<>(clients.keySet())) {
            if (name.equals(senderName)) continue;
            try {
                clients.get(name).receiveImage(base64Image,senderName);
            } catch (Exception e) {
                System.out.println("Failed to send canvas to " + name);
            }
        }
    }

    public String getCurrentImage() throws RemoteException {
        return currentImage;
    }

    public boolean requestJoin(String username, IClient client) throws RemoteException {
        System.out.println(username + " wants to join");

        if (managerName == null) {
            client.onJoinRejected("No manager exists, sorry.");
            System.out.println("No manager exists, rejected: " + username);
            return false;
        }

        if (clients.containsKey(username)) {
            client.onJoinRejected("Username already exists, please try another.");
            System.out.println(username + " rejected: username already exists");
            return false;
        }

        IClient managerClient = clients.get(managerName);
        boolean approved = managerClient.onJoinRequest(username);

        if (approved) {
            clients.put(username, client);
            System.out.println(username + " joined");
            client.receiveImage(currentImage, "server");
            broadcastUserList();
            return true;
        } else {
            client.onJoinRejected("Your request was rejected by the manager.");
            System.out.println(username + " was rejected");
            return false;
        }
    }

    public void registerManager(String username, IClient client) throws RemoteException {
        managerName = username;
        clients.put(username, client);
        System.out.println("Manager registered: " + username);
        broadcastUserList();
    }

    public void leaveBoard(String username) throws RemoteException {
        clients.remove(username);
        System.out.println(username + " left");
        broadcastUserList();
    }

    public void kickUser(String username) throws RemoteException {
        IClient target = clients.get(username);
        if (target != null) {
            try {
                target.onKicked();
            } catch (Exception e) {
                System.out.println("Failed to kick " + username);
            }
            clients.remove(username);
            System.out.println(username + " was kicked");
            broadcastUserList();
        }
    }

    public List<String> getUserList() throws RemoteException {
        return new ArrayList<>(clients.keySet());
    }

    public void managerQuit() throws RemoteException {
        System.out.println("Manager quit");
        for (String name : new ArrayList<>(clients.keySet())) {
            if (name.equals(managerName)) continue;
            try {
                clients.get(name).onManagerQuit();
            } catch (Exception e) {
                System.out.println("Failed to notify " + name);
            }
        }
        clients.clear();
    }

    public void sendChat(String username, String message) throws RemoteException {
        System.out.println("Chat from " + username + ": " + message);
        for (String name : new ArrayList<>(clients.keySet())) {
            try {
                clients.get(name).receiveChat(username, message);
            } catch (Exception e) {
                System.out.println("Failed to send chat to " + name);
            }
        }
    }

    void broadcastUserList() {
        List<String> users = new ArrayList<>(clients.keySet());
        for (String name : new ArrayList<>(clients.keySet())) {
            try {
                clients.get(name).updateUserList(users);
            } catch (Exception e) {
                System.out.println("Failed to update user list for " + name);
            }
        }
    }
}
