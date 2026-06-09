package co.eci.matrix.app;

import co.eci.matrix.ui.Setup;
import javax.swing.*;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
    int boardSize = Integer.parseInt(JOptionPane.showInputDialog("Board size (10-30):"));
    boardSize = Math.max(10, Math.min(30, boardSize));
    Setup.launch(boardSize);

}
}