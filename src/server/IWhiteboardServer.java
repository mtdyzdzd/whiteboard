package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWhiteboardServer extends Remote {

    void broadcastImage(String base64Image,String username) throws RemoteException;

    String getCurrentImage() throws RemoteException;

    boolean requestJoin(String username, IClient client) throws RemoteException;

    void registerManager(String username, IClient client) throws RemoteException;

    void leaveBoard(String username) throws RemoteException;

    void kickUser(String username) throws RemoteException;

    void managerQuit() throws RemoteException;

    List<String> getUserList() throws RemoteException;

    void sendChat(String username,String message) throws RemoteException;
}
