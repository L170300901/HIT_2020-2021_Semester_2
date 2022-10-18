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
        JFrame jframe = new JFrame("����������");
        jframe.setSize(1000, 800);
        jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel jpanel = new JPanel(null);

        //�﷨�������Ŵ�ӡ��
        JLabel jlabel_symbol = new JLabel("���ű�");
        jlabel_symbol.setBounds(50, 30, 400, 20);
        jpanel.add(jlabel_symbol);
        DefaultTableModel tablemodule_symbol = new DefaultTableModel(null, new String[] { "��������", "��������", "����", "�ڴ��ַ" });
        JTable table_symbol = new JTable(tablemodule_symbol);
        table_symbol.setEnabled(false);
        JScrollPane scrollpane_symbol = new JScrollPane(table_symbol);
        scrollpane_symbol.setBounds(50, 50, 400, 400);
        jpanel.add(scrollpane_symbol);


        //�﷨��������ַָ���ӡ��
        JLabel jlabel_triple = new JLabel("ָ������");
        jlabel_triple.setBounds(500, 30, 400, 20);
        jpanel.add(jlabel_triple);
        DefaultTableModel tablemodule_instruction = new DefaultTableModel(null, new String[] { "���","��Ԫʽ","����ַ��" });
        JTable table_triple = new JTable(tablemodule_instruction);
        table_triple.setEnabled(false);
        JScrollPane scrollpane_triple = new JScrollPane(table_triple);
        scrollpane_triple.setBounds(500, 50, 400, 400);
        jpanel.add(scrollpane_triple);

        //������Ϣ��ӡ��(error)
        JLabel jlabel_error = new JLabel("������Ϣ");
        jlabel_error.setBounds(50, 480, 800, 20);
        jpanel.add(jlabel_error);
        DefaultTableModel tablemodule_error = new DefaultTableModel(null, new String[] { "�����к�", "������Ϣ" });
        JTable table_error = new JTable(tablemodule_error);
        table_error.setEnabled(false);
        JScrollPane scrollpane_error = new JScrollPane(table_error);
        scrollpane_error.setBounds(50, 500, 850, 150);
        jpanel.add(scrollpane_error);

        //�����һ�α���ʱ����ֵ�����еķ������
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
        //���ı������ȡ�����ı�
        List<String> program = new ArrayList<String>();
        for (String sentense : jtextarea_text.getText().split("\n")) {
            program.add(sentense + "\n");
        }
        //����DFA���дʷ�����
        Scanning scanning = new Scanning(program);
        scanning.DFA();
        List<String[]> inputCache = scanning.getToken_Parser_Input();

        if(scanning.getError().size()!=0){
            JOptionPane.showMessageDialog(jpanel, "�ʷ������׶γ��ִ���,�޷������﷨������", "��ʾ", JOptionPane.ERROR_MESSAGE);
            return;
        }
        else {
            //����PDA�����﷨�Ƶ�����
            SemanticAnalyser analyser = new SemanticAnalyser(inputCache);
            analyser.PDA();

            //��ӡ�������
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

        //GUI���ӻ�
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
    }
}
