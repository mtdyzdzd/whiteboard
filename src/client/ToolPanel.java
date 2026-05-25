package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ToolPanel extends JPanel {

    WhiteboardPanel wb;

    Color[] colors = {
            new Color(0, 0, 0),       new Color(80, 80, 80),
            new Color(160, 160, 160), new Color(255, 255, 255),
            new Color(214, 39, 40),   new Color(255, 102, 0),
            new Color(255, 165, 0),   new Color(255, 215, 0),
            new Color(31, 119, 180),  new Color(23, 190, 207),
            new Color(44, 160, 44),   new Color(152, 223, 138),
            new Color(148, 103, 189), new Color(247, 182, 210),
            new Color(140, 86, 75),   new Color(255, 187, 120)
    };

    public ToolPanel(WhiteboardPanel wb) {
        this.wb = wb;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addToolButtons(toolRow);

        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addColorButtons(colorRow);
        addBrushSize(colorRow);

        add(toolRow);
        add(colorRow);
    }

    void addToolButtons(JPanel panel) {
        String[] labels = {"pen", "eraser", "line", "rectangle", "circle", "triangle", "text"};
        String[] tools  = {"pencil", "eraser", "line", "rect", "circle", "triangle", "text"};

        for (int i = 0; i < labels.length; i++) {
            JButton btn = new JButton(labels[i]);
            final String t = tools[i];
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    wb.tool = t;
                }
            });
            panel.add(btn);
        }
    }

    void addColorButtons(JPanel panel) {
        panel.add(new JLabel("color:"));
        for (int i = 0; i < colors.length; i++) {
            final Color c = colors[i];
            JPanel colorBlock = new JPanel();
            colorBlock.setBackground(c);
            colorBlock.setPreferredSize(new Dimension(22, 22));
            colorBlock.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            colorBlock.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    wb.color = c;
                }
            });
            panel.add(colorBlock);
        }
    }

    void addBrushSize(JPanel panel) {
        panel.add(new JLabel("  size:"));
        Integer[] sizes = {1, 2, 4, 6, 10, 15};
        JComboBox<Integer> box = new JComboBox<>(sizes);
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                wb.brushSize = (Integer) box.getSelectedItem();
            }
        });
        panel.add(box);
    }
}