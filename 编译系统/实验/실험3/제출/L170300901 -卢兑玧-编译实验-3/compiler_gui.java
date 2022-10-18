package bank.system;



import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
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

        //�ļ����밴��
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
        JButton button4 = new JButton("�������");
        button4.setBounds(950, 350, 175, 30);
        jpanel.add(button4);
        
        //�����������
        JButton button5 = new JButton("�������");
        button5.setBounds(725, 630, 175, 30);
        jpanel.add(button5);
        
        //�����ı�������(text)
        JLabel jlabel_text = new JLabel("�������");
        jlabel_text.setBounds(50, 30, 850, 20);
        jpanel.add(jlabel_text);
        JTextArea jtextarea_text = new JTextArea();
        JScrollPane jscrollpane_text = new JScrollPane(jtextarea_text);
        jscrollpane_text.setBounds(50, 50, 850, 400);
        jscrollpane_text.setRowHeaderView(new LineNumberHeaderView());
        jpanel.add(jscrollpane_text);

        //GUI���ӻ�
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
        //-----------------------------------------------------------------------


        //------------------------------��ť�¼�����------------------------------
        //1.�ļ����밴ť��
        //�����Ի���Ҫ�������ļ�·��
        //������󵯳����浯����������ȷ���ȡ�ļ��е��ı����Ǵ���������
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ReadFileGui(jframe,jtextarea_text);
            }
        });

        //2.�ʷ�������ť��
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ScannerGui(jtextarea_text);
            }
        });

        //3.�﷨������ť��
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ParserGui(jtextarea_text);
            }
        });

        //4.���������ť��
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SemanticGui(jtextarea_text);
            }
        });
        
      //5.�������ť��
        button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SemanticSDTGui();
            }
        });
        //-----------------------------------------------------------------------
    }
}