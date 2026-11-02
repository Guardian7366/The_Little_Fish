package main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    public static JFrame window;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::openGame);
    }

    private static void openGame() {
        window = new JFrame("The little fish");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setResizable(false);
        GamePanel gamePanel = new GamePanel();
        gamePanel.config.loadConfig();
        window.setUndecorated(gamePanel.fullScreenOn);
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        gamePanel.setupGame();
        window.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) { gamePanel.stopGame(); }
        });
        window.setVisible(true);
        gamePanel.requestFocusInWindow();
        gamePanel.startGameThread();
    }
}
