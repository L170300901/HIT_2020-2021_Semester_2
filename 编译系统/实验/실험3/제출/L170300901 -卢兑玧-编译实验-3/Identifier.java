package bank.system;

import java.util.List;
import java.util.ArrayList;

public class Identifier {
	private String 			name;				//符号表中标识符名称
	private String 			type;				//符号表中标识符基本类型
	private int 			offset;				//符号表中标识符在内存中的起始地址
	private int 	        length;				//符号表中标识符的长度
	public List<Integer>    arr_list;			//数组标识符的各维下标
	public List<Identifier> members;			//结构体标识符的成员变量列表

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
