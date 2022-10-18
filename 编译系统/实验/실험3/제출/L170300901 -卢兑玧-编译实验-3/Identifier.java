package bank.system;

import java.util.List;
import java.util.ArrayList;

public class Identifier {
	private String 			name;				//���ű��б�ʶ������
	private String 			type;				//���ű��б�ʶ����������
	private int 			offset;				//���ű��б�ʶ�����ڴ��е���ʼ��ַ
	private int 	        length;				//���ű��б�ʶ���ĳ���
	public List<Integer>    arr_list;			//�����ʶ���ĸ�ά�±�
	public List<Identifier> members;			//�ṹ���ʶ���ĳ�Ա�����б�

	public Identifier(String name, String type, int offset, int length) {
		super();
		this.name 	  = name;
		this.type 	  = type;
		this.offset   = offset;
		this.length   = length;
		this.arr_list = new ArrayList<Integer>();
		this.members  = new ArrayList<Identifier>();
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

}
