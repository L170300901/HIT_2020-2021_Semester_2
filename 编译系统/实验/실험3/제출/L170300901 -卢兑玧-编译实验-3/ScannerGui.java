package bank.system;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;


public class ScannerGui{

    public ScannerGui(JTextArea jtextarea_text){
        JFrame jframe = new JFrame("词法分析结果");
        jframe.setSize(1000, 800);
        jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel jpanel = new JPanel(null);

        //词法分析结果打印区(token)
        JLabel jlabel_token = new JLabel("token序列");
        jlabel_token.setBounds(50, 30, 850, 20);
        jpanel.add(jlabel_token);
        DefaultTableModel tablemodule_token = new DefaultTableModel(null, new String[] { "种别码", "属性值" });
        JTable table_token = new JTable(tablemodule_token);
        table_token.setEnabled(false);
        JScrollPane scrollpane_token = new JScrollPane(table_token);
        scrollpane_token.setBounds(50, 50, 850, 400);
        jpanel.add(scrollpane_token);

        //错误信息打印区(error)
        JLabel jlabel_error = new JLabel("错误信息");
        jlabel_error.setBounds(50, 480, 850, 20);
        jpanel.add(jlabel_error);
        DefaultTableModel tablemodule_error = new DefaultTableModel(null, new String[] { "错误行号", "错误信息" });
        JTable table_error = new JTable(tablemodule_error);
        table_error.setEnabled(false);
        JScrollPane scrollpane_error = new JScrollPane(table_error);
        scrollpane_error.setBounds(50, 500, 850, 150);
        jpanel.add(scrollpane_error);

        //清空上一次编译时，赋值到表中的分析结果
        int rowCount = tablemodule_token.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            tablemodule_token.removeRow(0);
        }
        table_token.updateUI();
        int rowCount3 = tablemodule_error.getRowCount();
        for (int i = 0; i < rowCount3; i++) {
            tablemodule_error.removeRow(0);
        }
        table_error.updateUI();
        //从文本框里读取代码文本
        List<String> program = new ArrayList<String>();
        for (String sentense : jtextarea_text.getText().split("\n")) {
            program.add(sentense + "\n");
        }
        //调用DFA进行词法分析
        Scanning scanning = new Scanning(program);
        scanning.DFA();
        //打印分析结果
        for (String[] token : scanning.getToken()) {
            tablemodule_token.addRow(token);
        }
        //打印错误信息
        for (String[] error : scanning.getError()) {
            tablemodule_error.addRow(error);
        }

        //GUI可视化
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
    }
}