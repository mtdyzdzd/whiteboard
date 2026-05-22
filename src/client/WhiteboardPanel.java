package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class WhiteboardPanel extends JPanel {

    BufferedImage canvas;
    BufferedImage tempCanvas;
    Graphics2D g2d;

    String tool = "pencil";
    Color color = Color.BLACK;
    int brushSize = 3;

    int startX, startY;
    // remember the location of last point
    int lastX, lastY;

    public WhiteboardPanel() {
        // create a 500X400
        canvas = new BufferedImage(500, 400, BufferedImage.TYPE_INT_RGB);
        // create a temp one
        tempCanvas = new BufferedImage(500, 400, BufferedImage.TYPE_INT_RGB);
        g2d = canvas.createGraphics();

        // 让线条更平滑
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 先把画布填成白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 500, 400);

        // press and releas
        addMouseListener(new MouseAdapter() {
            // 1-pressed
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                lastX = startX;
                lastY = startY;
            }

            // 2-released
            public void mouseReleased(MouseEvent e) {
                int endX = e.getX();
                int endY = e.getY();

                // 规则图形在松开鼠标时才画
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(brushSize));

                if (tool.equals("line")) {
                    g2d.drawLine(startX, startY, endX, endY);

                } else if (tool.equals("rect")) {
                    int x = Math.min(startX, endX);
                    int y = Math.min(startY, endY);
                    int w = Math.abs(endX - startX);
                    int h = Math.abs(endY - startY);
                    g2d.drawRect(x, y, w, h);

                } else if (tool.equals("circle")) {
                    int x = Math.min(startX, endX);
                    int y = Math.min(startY, endY);
                    int w = Math.abs(endX - startX);
                    int h = Math.abs(endY - startY);
                    g2d.drawOval(x, y, w, h);

                } else if (tool.equals("triangle")) {
                    int[] xp = {startX, endX, (startX + endX) / 2};
                    int[] yp = {endY,   endY,  startY};
                    g2d.drawPolygon(xp, yp, 3);
                }

                repaint();
            }
        });

        // Move
        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent e) {
                int curX = e.getX();
                int curY = e.getY();

                if (tool.equals("pencil")) {
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(brushSize,
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(lastX, lastY, curX, curY);
                    repaint();
                }

                if (tool.equals("eraser")) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(brushSize * 5,
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(lastX, lastY, curX, curY);
                    repaint();
                }

                // 规则图形：拖动时画到临时画布上预览
                if (tool.equals("line") || tool.equals("rect") ||
                        tool.equals("circle") || tool.equals("triangle")) {

                    // 把主画布内容复制到临时画布
                    Graphics2D tg = tempCanvas.createGraphics();
                    tg.drawImage(canvas, 0, 0, null);

                    // 在临时画布上画预览
                    tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    tg.setColor(color);
                    tg.setStroke(new BasicStroke(brushSize));

                    if (tool.equals("line")) {
                        tg.drawLine(startX, startY, curX, curY);

                    } else if (tool.equals("rect")) {
                        int x = Math.min(startX, curX);
                        int y = Math.min(startY, curY);
                        int w = Math.abs(curX - startX);
                        int h = Math.abs(curY - startY);
                        tg.drawRect(x, y, w, h);

                    } else if (tool.equals("circle")) {
                        int x = Math.min(startX, curX);
                        int y = Math.min(startY, curY);
                        int w = Math.abs(curX - startX);
                        int h = Math.abs(curY - startY);
                        tg.drawOval(x, y, w, h);

                    } else if (tool.equals("triangle")) {
                        int[] xp = {startX, curX, (startX + curX) / 2};
                        int[] yp = {curY,   curY,  startY};
                        tg.drawPolygon(xp, yp, 3);
                    }

                    tg.dispose();
                    repaint();
                }

                lastX = curX;
                lastY = curY;
            }
        });
    }

    public void drawText(String text, int x, int y) {
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.PLAIN, brushSize*5));
        g2d.drawString(text, x, y);
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 如果正在拖动规则图形，显示临时画布；否则显示主画布
        if (tool.equals("line") || tool.equals("rect") ||
                tool.equals("circle") || tool.equals("triangle")) {
            g.drawImage(tempCanvas, 0, 0, null);
        } else {
            g.drawImage(canvas, 0, 0, null);
        }
    }

    // 把画布转成 Base64 字符串，用于通过 RMI 传输
    public String getCanvasAsBase64() {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(canvas, "png", bos);
            byte[] bytes = bos.toByteArray();
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 收到 Base64 字符串，还原成图片覆盖画布
    public void setCanvasFromBase64(String base64) {
        try {
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes);
            BufferedImage img = javax.imageio.ImageIO.read(bis);
            g2d.drawImage(img, 0, 0, null);
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 清空画布（File→New用）
    public void clearCanvas() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 500, 400);
        g2d.setColor(color);
        repaint();
    }

    // 把外部图片设置为画布内容（File→Open用）
    public void setCanvasImage(BufferedImage img) {
        g2d.drawImage(img, 0, 0, 500, 400, null);
        repaint();
    }
}