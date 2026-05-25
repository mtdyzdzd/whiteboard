package client;

import server.IClient;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClientImpl extends UnicastRemoteObject implements IClient {

    // 需要拿到主窗口来更新界面
    WhiteboardFrame frame;

    public ClientImpl(WhiteboardFrame frame) throws RemoteException {
        super();
        this.frame = frame;
    }

    // 收到服务器推送的最新画布
    public void receiveImage(String base64Image, String fromUsername) throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (fromUsername.equals("server")) {
                    frame.whiteboardPanel.approved = true;
                }
                frame.whiteboardPanel.setCanvasFromBase64(base64Image);
                frame.repaint();
                frame.log("Canvas update from:" + fromUsername);
            }
        });
    }

    // 被管理员踢出
    public void onKicked() throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(frame,
                        "You have been kicked out by the manager.",
                        "Kicked",
                        JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        });
    }

    // 管理员关闭了白板
    public void onManagerQuit() throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object[] options = {"OK"};
                JOptionPane.showOptionDialog(
                        frame,
                        "The manager has closed the whiteboard.",
                        "Whiteboard Closed",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]
                );
                System.exit(0);
            }
        });
    }

    // 更新在线用户列表
    public void updateUserList(List<String> users) throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.updateUserList(users);
            }
        });
    }

    // 申请被拒绝
    public void onJoinRejected(String reason) throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(frame,
                        reason,
                        "Join Failed",
                        JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        });
    }

    public void receiveChat(String username, String message) throws RemoteException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.appendChat(username, message);
            }
        });
    }

    public boolean onJoinRequest(String username) throws RemoteException {
        final int[] result = {-1};
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    result[0] = JOptionPane.showConfirmDialog(
                            frame,
                            username + " wants to share your whiteboard. Allow?",
                            "New Join Request",
                            JOptionPane.YES_NO_OPTION
                    );
                }
            });
        } catch (Exception e) {
            return false;
        }
        return result[0] == JOptionPane.YES_OPTION;
    }
}