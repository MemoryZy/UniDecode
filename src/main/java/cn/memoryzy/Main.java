package cn.memoryzy;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.regex.*;

public class Main extends JFrame {
    private JTextArea chineseArea;
    private JTextArea unicodeArea;

    public Main() {
        initializeUI();
    }

    private void initializeUI() {
        FlatIntelliJLaf.installLafInfo();
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        setTitle("中文/Unicode双向转换工具");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(scale(600), scale(500));
        setLocationRelativeTo(null);

        Image icon = new ImageIcon(
                Objects.requireNonNull(Main.class.getResource("/logo.png"))
        ).getImage();
        setIconImage(icon);

        // 初始化组件
        chineseArea = createTextArea();
        unicodeArea = createTextArea();

        JButton toUnicodeBtn = createSmallButton("中文 → Unicode");
        JButton toChineseBtn = createSmallButton("Unicode → 中文");
        JButton clearBtn = createSmallButton("清空");

        // 布局设置
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();

        // 中文输入区域
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(createTitledPanel("中文输入框", chineseArea), gbc);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(toUnicodeBtn);
        buttonPanel.add(toChineseBtn);
        buttonPanel.add(clearBtn);

        gbc.gridy = 1;
        gbc.weighty = 0;
        mainPanel.add(buttonPanel, gbc);

        // Unicode输入区域
        gbc.gridy = 2;
        gbc.weighty = 0.4;
        mainPanel.add(createTitledPanel("Unicode输入框", unicodeArea), gbc);

        add(mainPanel);

        // 事件处理
        toUnicodeBtn.addActionListener(e -> convertToUnicode());
        toChineseBtn.addActionListener(e -> convertToChinese());
        clearBtn.addActionListener(e -> clearTextArea());
    }

    private void clearTextArea() {
        chineseArea.setText("");
        unicodeArea.setText("");
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        area.setFont(new Font("Monospaced", Font.PLAIN, scale(14)));
        return area;
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
//        button.setPreferredSize(new Dimension(150, 30));
        button.setFont(new Font("微软雅黑", Font.PLAIN, scale(12)));
        return button;
    }

    private JPanel createTitledPanel(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(component));
        return panel;
    }

    private void convertToUnicode() {
        String text = chineseArea.getText();
        unicodeArea.setText(chineseToUnicode(text));
    }

    private void convertToChinese() {
        String text = unicodeArea.getText();
        chineseArea.setText(unicodeToChinese(text));
    }

    // 保持原有转换方法不变
    private String chineseToUnicode(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (isChinese(c)) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String unicodeToChinese(String input) {
        Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            char c = (char) Integer.parseInt(hex, 16);
            matcher.appendReplacement(sb, Character.toString(c));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    private static final int BASE_DENSITY = 96; // 96dpi为100%缩放基准

    public static int scale(int pixels) {
        float dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return Math.round(pixels * dpi / BASE_DENSITY);
    }

    public static void main(String[] args) {
        // 启用高DPI支持（必须放在Swing初始化前）
        System.setProperty("sun.java2d.uiScale", "1"); // 关闭系统强制缩放
        System.setProperty("swing.aatext", "true");     // 抗锯齿
        System.setProperty("awt.useSystemAAFontSettings", "on");

        SwingUtilities.invokeLater(() -> {
            Main converter = new Main();
            converter.setVisible(true);
        });
    }
}