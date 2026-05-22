package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IClient extends Remote {

    // 服务器把最新画布推送给客户端
    void receiveImage(String base64Image,String fromUsername) throws RemoteException;

    // 被管理员踢出
    void onKicked() throws RemoteException;

    // 管理员关闭了白板
    void onManagerQuit() throws RemoteException;

    // 更新在线用户列表
    void updateUserList(List<String> users) throws RemoteException;

    // 申请加入被拒绝
    void onJoinRejected(String reason) throws RemoteException;

    // 收到聊天消息
    void receiveChat(String username, String message) throws RemoteException;
}