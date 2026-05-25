package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;


public class WhiteboardServerImpl extends UnicastRemoteObject
        implements IWhiteboardServer {

    // 保存当前画布的 Base64 字符串
    String currentImage = "";
    // 保存所有在线客户端，用户名 → 客户端对象
    ConcurrentHashMap<String, IClient> clients = new ConcurrentHashMap<>();
    String managerName;

    public WhiteboardServerImpl() throws RemoteException {
        super();
    }

    // 客户端调用这个方法，把最新画布发过来
    public void broadcastImage(String base64Image,String senderName) throws RemoteException {
        currentImage = base64Image;
        System.out.println("Canvas update from:" + senderName);
        // Phase 2 再做：广播给其他所有客户端
        for (String name : new ArrayList<>(clients.keySet())) {

            //skip youself
            if (name.equals(senderName)) continue;;
            try {
                clients.get(name).receiveImage(base64Image,senderName);
            } catch (Exception e) {
                System.out.println("Failed to send canvas to " + name);
            }
        }
    }
    // 新用户加入时调用，返回当前画布
    public String getCurrentImage() throws RemoteException {
        return currentImage;
    }

    // 申请加入
    public boolean requestJoin(String username, IClient client) throws RemoteException {
        System.out.println(username + " wants to join");

        // 没有管理员时直接拒绝
        if (managerName == null) {
            client.onJoinRejected("No manager exists, sorry.");
            System.out.println("No manager exists, rejected: " + username);
            return false;
        }
        // 检查用户名是否已经存在
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

    // 注册管理员
    public void registerManager(String username, IClient client) throws RemoteException {
        managerName = username;
        clients.put(username, client);
        System.out.println("Manager registered: " + username);
        broadcastUserList();
    }

    // 主动退出
    public void leaveBoard(String username) throws RemoteException {
        clients.remove(username);
        System.out.println(username + " left");
        broadcastUserList();
    }

    // 管理员踢人
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

    // 获取在线用户列表
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

    // 广播聊天消息给所有人
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