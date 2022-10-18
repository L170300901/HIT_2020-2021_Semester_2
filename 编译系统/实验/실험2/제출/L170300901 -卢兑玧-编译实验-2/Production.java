package bank.system;
import java.util.ArrayList;

public class Production {
    String left;
	String[] right;
	
	ArrayList<String> selectSet = new ArrayList<String>();
	public Production(String left, String[] right){
		this.left = left;
		this.right = right;
	}

	public String[] getRights(){
		return right;
	}

	public String getLeft(){
		return left;
	}
}