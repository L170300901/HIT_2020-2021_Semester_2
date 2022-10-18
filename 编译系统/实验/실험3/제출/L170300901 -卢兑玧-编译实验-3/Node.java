package bank.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//���ڹ���ע�ͷ����������洢�ڵ���Ϣ����
public class Node {
	private Node 	           father;			//ע�ͷ������У����ڵ�ĸ��׽ڵ�
	private String 	  	       name;			//ע�ͷ������У����ڵ������
	public  List<Node>	       sons;			//ע�ͷ������У����ڵ���ӽڵ��б��������﷨�������ս�������������ս����
	public  Map<String,String> attribute;		//ע�ͷ������У����ڵ�ӵ�е������б�

	public Node(String symbolName, Node father) {
		super();
		this.name 	   = symbolName;
		this.father    = father;
		this.sons	   = new ArrayList<Node>();
		this.attribute = new HashMap<String,String>();
	}

	public String getName() {
		return name;
	}

	public Node getFather() {
		return father;
	}
}
