package bank.system;


import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class programar extends JFrame {

  private JPanel contentPane;
  private JTextArea tx1;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          programar frame = new programar();
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   */
  public programar() {
   
    setBounds(100, 100, 1785, 705);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    JTextArea tx2 = new JTextArea();
    tx2.setBounds(400, 13, 200, 564);
    contentPane.add(tx2);
    JScrollPane scrollPane_2 = new JScrollPane((Component) null);
    scrollPane_2.setBounds(400, 60, 200, 566);
    contentPane.add(scrollPane_2);
    
    JTextArea tx3 = new JTextArea();
    scrollPane_2.setViewportView(tx3);
    tx3.setBounds(200, 13, 200, 564);
    JScrollPane scrollPane_1 = new JScrollPane(tx2);
    scrollPane_1.setBounds(200, 60, 200, 566);
    contentPane.add(scrollPane_1);
    FileDialog openDia = new FileDialog(this, "打开", FileDialog.LOAD);
    
    JScrollPane scrollPane_3 = new JScrollPane((Component) null);
    scrollPane_3.setBounds(600, 60, 200, 566);
    contentPane.add(scrollPane_3);
    
    JTextArea tx4 = new JTextArea();
    tx4.setBounds(600, 13, 200, 564);
    scrollPane_3.setViewportView(tx4);
    
    tx1 = new JTextArea();
    tx1.setBounds(10, 13, 190, 494);
    contentPane.add(tx1);
    tx1.setColumns(10);
    
    JScrollPane scrollPane = new JScrollPane(tx1);
    scrollPane.setBounds(10, 60, 190, 566);
    contentPane.add(scrollPane);
    
    JLabel lblNewLabel = new JLabel("文法");
    lblNewLabel.setFont(new Font("宋体", Font.PLAIN, 24));
    lblNewLabel.setBounds(10, 17, 218, 30);
    contentPane.add(lblNewLabel);
    
    JLabel lblFirst = new JLabel("first集");
    lblFirst.setFont(new Font("宋体", Font.PLAIN, 24));
    lblFirst.setBounds(200, 17, 205, 30);
    contentPane.add(lblFirst);
    
    
    
    JLabel lblFollow = new JLabel("follow集");
    lblFollow.setFont(new Font("宋体", Font.PLAIN, 24));
    lblFollow.setBounds(400, 17, 189, 30);
    contentPane.add(lblFollow);
    
    JLabel lblSelect = new JLabel("select集");
    lblSelect.setFont(new Font("宋体", Font.PLAIN, 24));
    lblSelect.setBounds(600, 17, 197, 37);
    contentPane.add(lblSelect);
    
    JPanel panel = new JPanel();
    panel.setBounds(900, 221, 248, 300);
    contentPane.add(panel);
    panel.setLayout(new GridLayout(0, 1, 0, 0));
    
    JButton file_1 = new JButton("从文件中导入文法");
    file_1.setFont(new Font("宋体", Font.PLAIN, 24));
    file_1.setBounds(950, 50, 175, 30);
    panel.add(file_1);
    
    JButton first_1 = new JButton("生成first集");
    first_1.setFont(new Font("宋体", Font.PLAIN, 24));
    first_1.setBounds(950, 100, 175, 30);
    panel.add(first_1);
    
    JButton follow = new JButton("生成follow集");
    follow.setFont(new Font("宋体", Font.PLAIN, 24));
    follow.setBounds(950, 150, 175, 30);
    panel.add(follow);
    
    JButton select_1 = new JButton("生成select集");
    select_1.setFont(new Font("宋体", Font.PLAIN, 24));
    select_1.setBounds(950, 200, 175, 30);
    panel.add(select_1);
    
    JButton button_2 = new JButton("生成预测分析表");
    button_2.setFont(new Font("宋体", Font.PLAIN, 24));
    button_2.setBounds(950, 250, 175, 30);
    panel.add(button_2);
    button_2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          BufferedReader bufReader = new BufferedReader(new FileReader(new File("prediction_table.txt")));
          String textLine;
          Set<String> items=new HashSet<String>(); 
          Map<String,Map<String,String>> m=new HashMap<String, Map<String,String>>();
          while ((textLine = bufReader.readLine()) != null) {
              String productionLeft   = textLine.split("#")[0];
              String inputSymbol      = (textLine.split("#")[1]).split("->")[0].trim();
              String productionRight  = (textLine.split("#")[1]).split("->")[1].trim();
              items.add(inputSymbol);
              if (m.containsKey(productionLeft)) {
                Map<String,String> o=m.get(productionLeft);
                if (!o.containsKey(inputSymbol)) 
                    o.put(inputSymbol, productionLeft+" -> "+productionRight);
              }else {
                Map<String,String> o=new HashMap<String, String>();
                o.put(inputSymbol, productionLeft+" -> "+productionRight);
                m.put(productionLeft, o);
              }
              
//              predictionTable.put(productionLeft + "-" + inputSymbol, productionRight);
          }
          String[] head = new String[items.size()];
//          head[0]=" ";
          int i=0;
          Object[][] map=new Object[m.keySet().size()][head.length];
          for (String h:items) head[i++]=h;
          int ii=0;
          for (String left:m.keySet()) 
          {
//            map[ii][0]=left;
            for (int j=0;j<head.length;j++)
            {
              map[ii][j]=m.get(left).get(head[j]);
//              System.out.print(map[ii][j]+" ");
            }
//            System.out.println();
            ii++;
          }
          bufReader.close();
          GUItable go=new GUItable(map, head);
          go.setVisible(true);
      } catch (Exception e) {
          e.printStackTrace();
      }
      }
    });
    select_1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String[] gra=tx1.getText().split("\r\n");
        Grammar_handler gg=new Grammar_handler(gra);
        ArrayList<Production> productions=gg.productions;
        for (Production pro:productions) {
          List<String> select=pro.selectSet;
          String k=" "+pro.left+" -> ";
          for (String ri:pro.right) k=k+ri;
          while (k.length()<50) k=k+" ";
          tx4.append(k+" : "+select.toString()+"\n");
        }
      }
    });
    follow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String[] gra=tx1.getText().split("\r\n");
        Grammar_handler gg=new Grammar_handler(gra);
        HashMap<String, ArrayList<String>> first=gg.followSets;
        for (String key:first.keySet()) {
          String k=" "+key;
          while (k.length()<30) k=k+" ";
          tx3.append(k+" : "+first.get(key).toString()+"\n");
        }
      }
    });
    first_1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String[] gra=tx1.getText().split("\r\n");
        Grammar_handler gg=new Grammar_handler(gra);
        HashMap<String, ArrayList<String>> first=gg.firstSets;
        for (String key:first.keySet()) {
          String k=" "+key;
          while (k.length()<30) k=k+" ";
          tx2.append(k+" : "+first.get(key).toString()+"\n");
        }
      }
    });
    file_1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openDia.setVisible(true);//显示打开文件对话框
        String dirpath = openDia.getDirectory();//获取打开文件路径并保存到字符串中。
        String fileName = openDia.getFile();//获取打开文件名称并保存到字符串中
        if (dirpath == null || fileName == null)//判断路径和文件是否为空
            return;
        else
        tx1.setText(null);//文件不为空，清空原来文件内容。
        File file = new File(dirpath, fileName);//创建新的路径和名称
        try {
      
            BufferedReader bufr = new BufferedReader(new FileReader(file));//尝试从文件中读东西
            String line = null;//变量字符串初始化为空
            while ((line = bufr.readLine()) != null) {
            tx1.append(line + "\r\n");//显示每一行内容
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
  }
}
