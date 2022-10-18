package bank.system;

import java.io.File;
import java.io.FileReader;
import javax.swing.JFrame;
import java.awt.FileDialog;
import javax.swing.JTextArea;
import java.io.BufferedReader;

public class ReadFileGui {
    public ReadFileGui(JFrame jframe,JTextArea jtextarea_text){
        FileDialog openDia = new FileDialog(jframe, "´ò¿ª", FileDialog.LOAD);
        openDia.setVisible(true);
        try {
            BufferedReader bufr = new BufferedReader(new FileReader(new File(openDia.getDirectory(), openDia.getFile())));
            jtextarea_text.setText(null);
            while (bufr.ready()) {
                jtextarea_text.append(bufr.readLine() + "\r\n");
            }
            bufr.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}