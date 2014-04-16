package histogram;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by sviatik on 3/12/14.
 */
public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        HistogramDemo panel = new HistogramDemo();
        frame.getContentPane().add(panel,"Center");
        frame.setSize(new Dimension(400, 600));
        frame.setVisible(true);
    }
}
