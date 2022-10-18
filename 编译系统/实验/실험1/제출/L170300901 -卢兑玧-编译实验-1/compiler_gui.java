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
	       
        //框架和面板初始化
        JFrame jframe = new JFrame("compiler");
        jframe.setSize(1000, 800);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel jpanel = new JPanel(null);

        // 文件输入按键
        JButton button1 = new JButton("文件读入");
        button1.setBounds(900, 50, 175, 30);
        jpanel.add(button1);

        //词法分析按键
        JButton button2 = new JButton("词法分析");
        button2.setBounds(900, 100, 175, 30);
        jpanel.add(button2);

        //语法分析按键
        JButton button3 = new JButton("语法分析");
        button3.setBounds(900, 150, 175, 30);
        jpanel.add(button3);

        //语义分析按键
        JButton button4 = new JButton("语义分析");
        button4.setBounds(900, 200, 175, 30);
        jpanel.add(button4);

        //代码文本输入区(text)
        JLabel jlabel_text = new JLabel("输入代码");
        jlabel_text.setBounds(50, 30, 600, 20);
        jpanel.add(jlabel_text);
        JTextArea jtextarea_text = new JTextArea();
        JScrollPane jscrollpane_text = new JScrollPane(jtextarea_text);
        jscrollpane_text.setBounds(50, 50, 600, 400);
        jscrollpane_text.setRowHeaderView(new LineNumberHeaderView());
        jpanel.add(jscrollpane_text);

        //词法分析结果打印区(token)
        JLabel jlabel_token = new JLabel("token序列");
        jlabel_token.setBounds(700, 30, 200, 20);
        jpanel.add(jlabel_token);
        DefaultTableModel tablemodule_token = new DefaultTableModel(null, new String[] { "种别码", "属性值" });
        JTable table_token = new JTable(tablemodule_token);
        table_token.setEnabled(false);
        JScrollPane scrollpane_token = new JScrollPane(table_token);
        scrollpane_token.setBounds(700, 50, 200, 400);
        jpanel.add(scrollpane_token);

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
        
        //打开文件
        FileDialog openDia = openDia = new FileDialog(jframe, "打开", FileDialog.LOAD);
        
        //GUI可视化
        jframe.setContentPane(jpanel);
        jframe.setVisible(true);
        //-----------------------------------------------------------------------


       
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              
                openDia.setVisible(true);//显示打开文件对话框
                String dirpath = openDia.getDirectory();//获取打开文件路径并保存到字符串中。
                String fileName = openDia.getFile();//获取打开文件名称并保存到字符串中
                
                if (dirpath == null || fileName == null)//判断路径和文件是否为空
                    return;
                else
                  jtextarea_text.setText(null);//文件不为空，清空原来文件内容。
                File file = new File(dirpath, fileName);//创建新的路径和名称

                try {
                    BufferedReader bufr = new BufferedReader(new FileReader(file));//尝试从文件中读东西
                    String line = null;//变量字符串初始化为空
                    while ((line = bufr.readLine()) != null) {
                      jtextarea_text.append(line + "\r\n");//显示每一行内容
                    }
                    bufr.close();//关闭文件
                } catch (FileNotFoundException e1) {
                    // 抛出文件路径找不到异常
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // 抛出IO异常
                    e1.printStackTrace();
                }
                
            }
        });

        
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        //-----------------------------------------------------------------------
    }
}
