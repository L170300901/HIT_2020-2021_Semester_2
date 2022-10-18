package bank.system;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.awt.Font;


public class SemanticSDTGui extends JFrame {

	  private JPanel contentPane;

	  /**
	   * Launch the application.
	   */
	  public static void main(String[] args) {
	    EventQueue.invokeLater(new Runnable() {
	      public void run() {
	        try {
	          SemanticSDTGui frame = new SemanticSDTGui();
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
	  public SemanticSDTGui() {
	    setBounds(100, 100, 1031, 708);
	    contentPane = new JPanel();
	    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	    contentPane.setLayout(new BorderLayout(0, 0));
	    setContentPane(contentPane);
	    
	    JPanel panel = new JPanel();
	    contentPane.add(panel, BorderLayout.CENTER);
	    panel.setLayout(null);
	    
	    JScrollPane scrollPane = new JScrollPane();
	    scrollPane.setBounds(14, 67, 970, 573);
	    panel.add(scrollPane);
	    
	    JTextArea tx1 = new JTextArea();
	    scrollPane.setViewportView(tx1);
	    
	    JLabel lblSdt = new JLabel("翻译方案SDT");
	    lblSdt.setFont(new Font("宋体", Font.PLAIN, 30));
	    lblSdt.setBounds(408, 13, 199, 54);
	    panel.add(lblSdt);
	    
	    String line;
	    try {
	      BufferedReader bufReader    = new BufferedReader(new FileReader(new File("E://2020fall/NOHTAEYUN/Bank/src/bank/system/SDT.txt")));
	      while ((line=bufReader.readLine())!=null)
	      {
	        tx1.append(line);
	        tx1.append("\n");
	      }
	    }catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	  //GUI可视化
//	    setContentPane(panel);
	    setVisible(true);
	  }
	}
