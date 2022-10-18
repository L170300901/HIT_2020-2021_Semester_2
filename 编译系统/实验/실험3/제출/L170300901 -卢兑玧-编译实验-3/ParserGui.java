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

public class ParserGui {
    public ParserGui(JTextArea jtextarea_text){
        JFrame jframe = new JFrame("�﷨�������");
        jframe.setSize(1000, 800);
        jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel jpanel = new JPanel(null);

        //�﷨���������ӡ��
        JLabel jlabel_token = new JLabel("�������");
        jlabel_token.setBounds(50, 30, 850, 20);
        jpanel.add(jlabel_token);
        DefaultTableModel tablemodule_token = new DefaultTableModel(null, new String[] { "�����Ϣ" });
        JTable table_token = new JTable(tablemodule_token);
        table_token.setEnabled(false);
        JScrollPane scrollpane_token = new JScrollPane(table_token);
        scrollpane_token.setBounds(50, 50, 850, 400);
        jpanel.add(scrollpane_token);

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
            //����PDA�����﷨����
            Parser parser =new Parser(inputCache);
            parser.PDA();
            //��ӡ�������
            for (String result : parser.getResult()) {
                tablemodule_token.addRow(new String[]{result});
            }
            for (String[] error : parser.getError()) {
                tablemodule_error.addRow(error);
            }
        }

        //GUI���ӻ�
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
    }
}