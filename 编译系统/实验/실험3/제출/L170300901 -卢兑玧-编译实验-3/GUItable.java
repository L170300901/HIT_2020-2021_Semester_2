package bank.system;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import java.awt.Font;

public class GUItable extends JFrame {
  String [] head;
  List<String[]> tab=new ArrayList<>();
  private JPanel contentPane;
  private JTable table;
  String[][] ta=new String[0][4];
  DefaultTableModel tablemodule_ans;
  String sql;
 
  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          String[] head=new String[2];
          head[0]="123";
          head[1]="23";
          String[][] data=new String[2][2];
          data[0][0]="kff";
          data[0][1]="jjj";
              GUItable frame = new GUItable(data,head);
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
  public GUItable(Object[][] data,String[] head) {
    this.sql=sql;
    this.head=head;
//    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 1208, 642);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);
    
    JPanel panel_3 = new JPanel();
    contentPane.add(panel_3, BorderLayout.CENTER);
    panel_3.setLayout(null);
    
    tablemodule_ans = new DefaultTableModel(data, head);
    table = new JTable(tablemodule_ans); 
    JScrollPane scroll = new JScrollPane(table);  
    scroll.setLocation(0, 65);
    scroll.setSize(1166, 507);  
//    table.setBounds(0, 203, 591, 196);
    panel_3.add(scroll);
    
    JLabel label = new JLabel("Ô¤²â·ÖÎö±í");
    label.setFont(new Font("ËÎÌå", Font.PLAIN, 48));
    label.setBounds(483, 0, 498, 72);
    panel_3.add(label);
    
   
 
    
  }
}

