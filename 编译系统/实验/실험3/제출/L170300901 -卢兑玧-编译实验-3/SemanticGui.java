package bank.system;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class SemanticGui {
    public SemanticGui(JTextArea jtextarea_text){
        JFrame jframe = new JFrame("语义分析结果");
        jframe.setSize(1000, 800);
        jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel jpanel = new JPanel(null);

        //语法分析符号打印区
        JLabel jlabel_symbol = new JLabel("符号表");
        jlabel_symbol.setBounds(50, 30, 400, 20);
        jpanel.add(jlabel_symbol);
        DefaultTableModel tablemodule_symbol = new DefaultTableModel(null, new String[] { "变量名称", "所属类型", "长度", "内存地址" });
        JTable table_symbol = new JTable(tablemodule_symbol);
        table_symbol.setEnabled(false);
        JScrollPane scrollpane_symbol = new JScrollPane(table_symbol);
        scrollpane_symbol.setBounds(50, 50, 400, 400);
        jpanel.add(scrollpane_symbol);


        //语法分析三地址指令打印区
        JLabel jlabel_triple = new JLabel("指令序列");
        jlabel_triple.setBounds(500, 30, 400, 20);
        jpanel.add(jlabel_triple);
        DefaultTableModel tablemodule_instruction = new DefaultTableModel(null, new String[] { "序号","四元式","三地址码" });
        JTable table_triple = new JTable(tablemodule_instruction);
        table_triple.setEnabled(false);
        JScrollPane scrollpane_triple = new JScrollPane(table_triple);
        scrollpane_triple.setBounds(500, 50, 400, 400);
        jpanel.add(scrollpane_triple);

        //错误信息打印区(error)
        JLabel jlabel_error = new JLabel("错误信息");
        jlabel_error.setBounds(50, 480, 800, 20);
        jpanel.add(jlabel_error);
        DefaultTableModel tablemodule_error = new DefaultTableModel(null, new String[] { "错误行号", "错误信息" });
        JTable table_error = new JTable(tablemodule_error);
        table_error.setEnabled(false);
        JScrollPane scrollpane_error = new JScrollPane(table_error);
        scrollpane_error.setBounds(50, 500, 850, 150);
        jpanel.add(scrollpane_error);

        //清空上一次编译时，赋值到表中的分析结果
        int rowCount = tablemodule_symbol.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            tablemodule_symbol.removeRow(0);
        }
        table_triple.updateUI();
        int rowCount1 = tablemodule_instruction.getRowCount();
        for (int i = 0; i < rowCount1; i++) {
            tablemodule_instruction.removeRow(0);
        }
        table_triple.updateUI();
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
        List<String[]> inputCache = scanning.getToken_Parser_Input();

        if(scanning.getError().size()!=0){
            JOptionPane.showMessageDialog(jpanel, "词法分析阶段出现错误,无法进行语法分析！", "提示", JOptionPane.ERROR_MESSAGE);
            return;
        }
        else {
            //调用PDA进行语法制导翻译
            SemanticAnalyser analyser = new SemanticAnalyser(inputCache);
            analyser.PDA();

            //打印分析结果
            for (String[] symbol : analyser.getSymbolTable()) {
                tablemodule_symbol.addRow(symbol);
            }
            int index=0;
            for (String[] instruction : analyser.getInstructions()) {
                tablemodule_instruction.addRow(new String[]{String.valueOf(index), instruction[0], instruction[1]});
                index++;
            }
            for (String[] error : analyser.getError()) {
                tablemodule_error.addRow(error);
            }
        }

        //GUI可视化
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
    }
}
