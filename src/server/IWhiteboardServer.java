package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IWhiteboardServer extends Remote {

    // 客户端画完东西后，把画布图像发给服务器
    void broadcastImage(String base64Image,String username) throws RemoteException;

    // 新用户加入时，获取当前画布状态
    String getCurrentImage() throws RemoteException;

    // Phase 2 新增
    // 申请加入，返回true表示请求已发送给管理员
    boolean requestJoin(String username, IClient client) throws RemoteException;

    void registerManager(String username, IClient client) throws RemoteException;

    // 主动退出
    void leaveBoard(String username) throws RemoteException;

    // 管理员踢人
    void kickUser(String username) throws RemoteException;

    void managerQuit() throws RemoteException;
    // 获取当前在线用户列表
    List<String> getUserList() throws RemoteException;

    // chat
    void sendChat(String username,String message) throws RemoteException;
}