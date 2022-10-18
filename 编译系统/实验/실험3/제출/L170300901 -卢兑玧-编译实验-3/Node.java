package bank.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//用于构建注释分析树，并存储节点信息的类
public class Node {
	private Node 	           father;			//注释分析树中，本节点的父亲节点
	private String 	  	       name;			//注释分析树中，本节点的名称
	public  List<Node>	       sons;			//注释分析树中，本节点的子节点列表（不包含语法动作和终结符，仅包含非终结符）
	public  Map<String,String> attribute;		//注释分析树中，本节点拥有的属性列表

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
