package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IClient extends Remote {

    void receiveImage(String base64Image,String fromUsername) throws RemoteException;

    void onKicked() throws RemoteException;

    void onManagerQuit() throws RemoteException;

    void updateUserList(List<String> users) throws RemoteException;

    void onJoinRejected(String reason) throws RemoteException;

    void receiveChat(String username, String message) throws RemoteException;

    boolean onJoinRequest(String username) throws RemoteException;
}
