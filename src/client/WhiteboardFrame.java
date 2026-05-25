package client;

import server.IWhiteboardServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


public class WhiteboardFrame extends JFrame {

    WhiteboardPanel whiteboardPanel;
    IWhiteboardServer server;
    public ClientImpl clientImpl;

    String username;
    boolean isManager;

    DefaultListModel<String> userListModel;
    JList<String> userList;
    // show log
    JTextArea logArea;
    JTextField chatInput;
    JTextArea chatArea;

    public WhiteboardFrame(String username, String serverIP, int serverPort, boolean isManager) {
        this.username = username;
        this.isManager = isManager;

        setTitle("Whiteboard - " + username);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        whiteboardPanel = new WhiteboardPanel();
        ToolPanel toolPanel = new ToolPanel(whiteboardPanel);

        if (isManager) {
            setJMenuBar(buildMenuBar());
        }

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setPreferredSize(new Dimension(150,200));
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder(" -Online Users- "));

        // chat area
        chatArea = new JTextArea(6, 15);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createTitledBorder("Chat"));

        chatInput = new JTextField();
        chatInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendChat();
            }
        });

        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendChat();
            }
        });

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendBtn, BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        rightPanel.add(chatInputPanel, BorderLayout.SOUTH);

        if (isManager) {
            JButton kickBtn = new JButton("Kick");
            kickBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selected = userList.getSelectedValue();
                    if (selected == null) {
                        JOptionPane.showMessageDialog(WhiteboardFrame.this,
                                "Please select a user to kick.");
                        return;
                    }
                    if (selected.equals(username)) {
                        JOptionPane.showMessageDialog(WhiteboardFrame.this,
                                "You cannot kick yourself.");
                        return;
                    }
                    try {
                        server.kickUser(selected);
                        log("Kicked: " + selected);
                    } catch (Exception ex) {
                        log("Failed to kick: " + ex.getMessage());
                    }
                }
            });
            JPanel userPanel = new JPanel(new BorderLayout());
            userPanel.add(userScroll, BorderLayout.CENTER);
            userPanel.add(kickBtn, BorderLayout.SOUTH);
            rightPanel.add(userPanel, BorderLayout.NORTH);
        } else {
            rightPanel.add(userScroll, BorderLayout.NORTH);
        }

        //log area
        logArea = new JTextArea(4, 40);
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(Color.GREEN);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log"));

        // when release the mouse, sent the board to server
        whiteboardPanel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                sendCanvasToServer();
            }
        });

        // mouse click
        whiteboardPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!whiteboardPanel.approved) return;
                if(whiteboardPanel.tool.equals("text")){
                    String input = JOptionPane.showInputDialog("Please enter messages:");
                    if (input != null && !input.isEmpty()){
                        whiteboardPanel.drawText(input, e.getX(), e.getY());
                        sendCanvasToServer();
                    }
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                handleClose();
            }
        });

        // layout
        add(toolPanel, BorderLayout.NORTH);
        add(new JScrollPane(whiteboardPanel), BorderLayout.CENTER);
        add(logScroll, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // connect RMI server
        connectToServer(serverIP, serverPort);

        if (!isManager) {
            whiteboardPanel.approved = false;
        }

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem closeItem = new JMenuItem("Close");

        newItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboardPanel.clearCanvas();
                sendCanvasToServer();
                log("New whiteboard created");
            }
        });

        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int result = fc.showOpenDialog(WhiteboardFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        BufferedImage img = ImageIO.read(file);
                        whiteboardPanel.setCanvasImage(img);
                        sendCanvasToServer();
                        log("Opened: " + file.getName());
                    } catch (Exception ex) {
                        log("Failed to open: " + ex.getMessage());
                    }
                }
            }
        });

        final File[] lastSaveFile = {null};
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lastSaveFile[0] != null) {
                    saveToFile(lastSaveFile[0]);
                } else {
                    saveAsAction(lastSaveFile);
                }
            }
        });

        saveAsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAsAction(lastSaveFile);
            }
        });

        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleClose();
            }
        });

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);

        return menuBar;
    }

    void saveAsAction(File[] lastSaveFile) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("whiteboard.png"));
        int result = fc.showSaveDialog(WhiteboardFrame.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            lastSaveFile[0] = fc.getSelectedFile();
            saveToFile(lastSaveFile[0]);
        }
    }

    void saveToFile(File file) {
        try {
            ImageIO.write(whiteboardPanel.canvas, "png", file);
            log("Saved: " + file.getName());
        } catch (Exception e) {
            log("Failed to save: " + e.getMessage());
        }
    }

    void connectToServer(String ip,int port){
        try{
            Registry registry = LocateRegistry.getRegistry(ip,port);
            server = (IWhiteboardServer) registry.lookup("WhiteboardService");
            clientImpl = new ClientImpl(this);

            if (isManager) {
                server.registerManager(username, clientImpl);
                log("Whiteboard created successfully");
                log("Connected to server successfully");
            } else {
                log("Connected to server. Waiting for manager approval...");
            }
        } catch (Exception e) {
            log("Sry. Failed to connect to server: " + e.getMessage());
        }
    }

    void sendCanvasToServer(){
        if (server == null || !whiteboardPanel.approved) return;
        try{
            String base64 = whiteboardPanel.getCanvasAsBase64();
            server.broadcastImage(base64,username);
        } catch (Exception e){
            log("Sry. Failed to send canvas: " + e.getMessage());
        }
    }

    void sendChat() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;
        try {
            server.sendChat(username, msg);
            chatInput.setText("");
        } catch (Exception e) {
            log("Failed to send chat: " + e.getMessage());
        }
    }

    public void appendChat(String from, String message) {
        chatArea.append(from + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // update user list
    public void updateUserList(List<String> users){
        userListModel.clear();
        for (String u : users){
            userListModel.addElement(u);
        }
        log("Hi~ User list updated:" + users);
    }

    void handleClose() {
        try {
            if (isManager) {
                Object[] options = {"Yes", "No"};
                int result = JOptionPane.showOptionDialog(
                        this,
                        "Closing will disconnect all users. Continue?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]
                );
                if (result == 0) {
                    server.managerQuit();
                    System.exit(0);
                }
            } else {
                server.leaveBoard(username);
                System.exit(0);
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }

    public void log(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logArea.append("[LOG] " + message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }
}
