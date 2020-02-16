package javadash;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public final static MainFrame MAIN_FRAME;

    static {

        MAIN_FRAME = new MainFrame();
        /*
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        device.setFullScreenWindow(MAIN_FRAME);
        MAIN_FRAME.setVisible(true);

         */
    }
}
