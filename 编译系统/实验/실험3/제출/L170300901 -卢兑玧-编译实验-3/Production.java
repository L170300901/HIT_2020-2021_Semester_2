package bank.system;

import java.util.ArrayList;

public class Production {
    String   		  left;								//产生式左部非终结符
	String[]		  right;							//产生式右部文法符号序列
	public  ArrayList<String> selectSet;						//该产生式对应的select集

	public Production(String left, String[] right){
		this.left = left;
		this.right = right;
		this.selectSet = new ArrayList<String>();
	}

	public String[] getRights(){
		return right;
	}

	public String getLeft(){
		return left;
	}
}