package bank.system;

import java.util.ArrayList;

public class Production {
    String   		  left;								//����ʽ�󲿷��ս��
	String[]		  right;							//����ʽ�Ҳ��ķ���������
	public  ArrayList<String> selectSet;						//�ò���ʽ��Ӧ��select��

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