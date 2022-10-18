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
        //------------------------------GUI布局设置------------------------------
        //框架和面板初始化
        JFrame jframe = new JFrame("compiler");
        jframe.setSize(1000, 800);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel jpanel = new JPanel(null);

        //文件输入按键
        JButton button1 = new JButton("文件读入");
        button1.setBounds(950, 50, 175, 30);
        jpanel.add(button1);

        //词法分析按键
        JButton button2 = new JButton("词法分析");
        button2.setBounds(950, 150, 175, 30);
        jpanel.add(button2);

        //语法分析按键
        JButton button3 = new JButton("语法分析");
        button3.setBounds(950, 250, 175, 30);
        jpanel.add(button3);

        //语义分析按键
        JButton button4 = new JButton("语义分析");
        button4.setBounds(950, 350, 175, 30);
        jpanel.add(button4);
        
        //语义分析规则
        JButton button5 = new JButton("语义规则");
        button5.setBounds(725, 630, 175, 30);
        jpanel.add(button5);
        
        //代码文本输入区(text)
        JLabel jlabel_text = new JLabel("输入代码");
        jlabel_text.setBounds(50, 30, 850, 20);
        jpanel.add(jlabel_text);
        JTextArea jtextarea_text = new JTextArea();
        JScrollPane jscrollpane_text = new JScrollPane(jtextarea_text);
        jscrollpane_text.setBounds(50, 50, 850, 400);
        jscrollpane_text.setRowHeaderView(new LineNumberHeaderView());
        jpanel.add(jscrollpane_text);

        //GUI可视化
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
        //-----------------------------------------------------------------------


        //------------------------------按钮事件设置------------------------------
        //1.文件输入按钮：
        //弹出对话框要求输入文件路径
        //输入错误弹出警告弹窗，输入正确则读取文件中的文本覆盖代码输入区
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ReadFileGui(jframe,jtextarea_text);
            }
        });

        //2.词法分析按钮：
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ScannerGui(jtextarea_text);
            }
        });

        //3.语法分析按钮：
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ParserGui(jtextarea_text);
            }
        });

        //4.语义分析按钮：
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SemanticGui(jtextarea_text);
            }
        });
        
      //5.语义规则按钮：
        button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SemanticSDTGui();
            }
        });
        //-----------------------------------------------------------------------
    }
}