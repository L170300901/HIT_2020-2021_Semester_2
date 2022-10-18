package bank.system;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class compiler_gui {

    public static void main(String[] args) {
        //------------------------------GUI��������------------------------------
        //��ܺ�����ʼ��
        JFrame jframe = new JFrame("compiler");
        jframe.setSize(1000, 800);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel jpanel = new JPanel(null);

        // �ļ����밴��
        JButton button1 = new JButton("�ļ�����");
        button1.setBounds(950, 50, 175, 30);
        jpanel.add(button1);

        //�ʷ���������
        JButton button2 = new JButton("�ʷ�����");
        button2.setBounds(950, 150, 175, 30);
        jpanel.add(button2);

        //�﷨��������
        JButton button3 = new JButton("�﷨����");
        button3.setBounds(950, 250, 175, 30);
        jpanel.add(button3);

        //�����������
        JButton button4 = new JButton("�����ķ�");
        button4.setBounds(950, 350, 175, 30);
        jpanel.add(button4);

        //�����ı�������(text)
        JLabel jlabel_text = new JLabel("�������");
        jlabel_text.setBounds(50, 30, 600, 20);
        jpanel.add(jlabel_text);
        JTextArea jtextarea_text = new JTextArea();
        JScrollPane jscrollpane_text = new JScrollPane(jtextarea_text);
        jscrollpane_text.setBounds(50, 50, 600, 400);
        jscrollpane_text.setRowHeaderView(new LineNumberHeaderView());
        jpanel.add(jscrollpane_text);

        //�﷨���������ӡ��
        JLabel jlabel_token = new JLabel("�������");
        jlabel_token.setBounds(700, 30, 200, 20);
        jpanel.add(jlabel_token);
        DefaultTableModel tablemodule_token = new DefaultTableModel(null, new String[] { "�����Ϣ" });
        JTable table_token = new JTable(tablemodule_token);
        table_token.setEnabled(false);
        JScrollPane scrollpane_token = new JScrollPane(table_token);
        scrollpane_token.setBounds(700, 50, 200, 400);
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

        //���ļ�
        FileDialog openDia = openDia = new FileDialog(jframe, "��", FileDialog.LOAD);

        //GUI���ӻ�
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
        //-----------------------------------------------------------------------


        //------------------------------��ť�¼�����------------------------------
        //1.�ļ����밴ť��
        //�����Ի���Ҫ�������ļ�·����Ĭ��Ϊ��C:/Users/Cosmos/Desktop/parser/1.txt
        //������󵯳����浯����������ȷ���ȡ�ļ��е��ı����Ǵ���������
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDia.setVisible(true);//��ʾ���ļ��Ի���
                String dirpath = openDia.getDirectory();//��ȡ���ļ�·�������浽�ַ����С�
                String fileName = openDia.getFile();//��ȡ���ļ����Ʋ����浽�ַ�����
                if (dirpath == null || fileName == null)//�ж�·�����ļ��Ƿ�Ϊ��
                    return;
                else
                jtextarea_text.setText(null);//�ļ���Ϊ�գ����ԭ���ļ����ݡ�
                File file = new File(dirpath, fileName);//�����µ�·��������
                try {
                    BufferedReader bufr = new BufferedReader(new FileReader(file));//���Դ��ļ��ж�����
                    String line = null;//�����ַ�����ʼ��Ϊ��
                    while ((line = bufr.readLine()) != null) {
                    jtextarea_text.append(line + "\r\n");//��ʾÿһ������
                    }
                    bufr.close();//�ر��ļ�
                } catch (FileNotFoundException e1) {
                    // �׳��ļ�·���Ҳ����쳣
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // �׳�IO�쳣
                    e1.printStackTrace();
                }
            }
        });

        //2.�ʷ�������ť��
        //����������������ı�������scanning�е�DFA���дʷ�����
        //���������ʾ�ڷ���������ʹ�����Ϣ��
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                //��ӡ�������
                for (String[] token : scanning.getToken()) {
                    tablemodule_token.addRow(token);
                }
                //��ӡ������Ϣ
                for (String[] error : scanning.getError()) {
                    tablemodule_error.addRow(error);
                }
            }
        });

        //�﷨������ť��
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                System.out.println("�ʷ����������");
                for (String[] a : inputCache) {
                    for(String b : a){
                        System.out.print(b+" ");
                    }
                    System.out.println();
                }

                if(scanning.getError().size()!=0){
                    JOptionPane.showMessageDialog(jpanel, "�ʷ������׶γ��ִ���,�޷������﷨������", "��ʾ", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    //����PDA�����﷨����
                    Parsing parser =new Parsing(inputCache);
                    parser.PDA();
                    //��ӡ�������
                    for (String result : parser.getResult()) {
                        tablemodule_token.addRow(new String[]{result});
                    }
                    for (String[] error : parser.getError()) {
                        tablemodule_error.addRow(error);
                    }
                }
            }
        });

        //TODO:���������ť��
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                programar pro=new programar();
                pro.setVisible(true);
            }
        });
        //-----------------------------------------------------------------------
    }
}