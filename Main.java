// import javax.swing.*;
// public class Main {
//     public static void main(String[] args) {
//         AppGUI window = new AppGUI();
//         window.pack();
//         window.setVisible(true);
//         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//     }
// }
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Always launch Swing UI on the Event Dispatch Thread (best practice)
        SwingUtilities.invokeLater(() -> {
            // Create and show the main application window
            new AppGUI(); 
        });
    }
}
