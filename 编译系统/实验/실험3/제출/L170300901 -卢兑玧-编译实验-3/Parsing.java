package bank.system;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parsing {

	int rowNumber; 							// 用于输出语法分析树中的结点对应的成分在输入文件中的行号
	List<String[]> stack; 					// PDA的栈结构，栈元素格式为<元素，语法树中的层数>
	List<String[]> inputCache; 				// PDA的输入缓冲区,词法分析的结果，格式为<种别码，属性值，行号>或<种别码，行号>
	List<String> parsingResult; 			// 语法分析的结果，格式为“符号（行号）”或“种别码 ：属性值（行号）”，缩进格数为：2*节点深度
	List<String[]> errorMessages; 			// 语法分析的结果，格式为“符号（行号）”或“种别码 ：属性值（行号）”，缩进格数为：2*节点深度
	Map<String, String> predictionTable; 	// 预测分析表，格式为<产生式左部-输入符号,产生式右部>


	// ------------------------------Construct function------------------------------
	public Parsing(List<String[]> inputCache) {
		this.stack = new ArrayList<String[]>();
		this.rowNumber = 0;
		this.inputCache = inputCache;
		this.parsingResult = new ArrayList<String>();
		this.errorMessages = new ArrayList<String[]>();
		this.predictionTable = new HashMap<String, String>();
		raedPredictTable();
		System.out.println("语法规则为：");
		for (String grammar : this.predictionTable.keySet()) {
			System.out.println(grammar+" - "+this.predictionTable.get(grammar));
		}
	}
	// ------------------------------------------------------------------------------




	// ------------------------------test function------------------------------
	public static void main(String[] args) {
		List<String> program = new ArrayList<String>();
		// add source code here,end with "\n"
		program.add("int main(){}\n");
		//program.add("bbb\n");
		//program.add(";\n");
		Scanning scanning = new Scanning(program);
		scanning.DFA();
		System.out.println("词法分析结果：");
		for (String[] a : scanning.getToken_Parser_Input()) {
			for(String b : a){
				System.out.print(b+" ");
			}
			System.out.println();
		}
		Parsing test = new Parsing(scanning.getToken_Parser_Input());
		test.PDA();
		System.out.println("语法分析结果：");
		for (String testResult : test.getResult()) {
			System.out.println(testResult);
		}
		System.out.println("错误信息：");
		for (String[] testResult : test.errorMessages) {
			System.out.println(testResult[0]+" "+testResult[1]);
		}
	}
	// -------------------------------------------------------------------------




	// ------------------------------assistant function------------------------------
	public void popPrint(int treeDepth, String[] args) {
		String s = "";
		// 按要求进行缩进处理
		for (int i = 0; i < treeDepth; i++) {
			s += "  ";
		}
		// 输出的为某个非终结符(产生式左部),args格式为<符号>
		if (args.length == 1) {
			s = s + args[0] + " (" + String.valueOf(rowNumber) + ")";
		}
		// 输出的为没有属性值的符号,args格式为<种别码，行号>
		else if (args.length == 2) {
			rowNumber = Integer.valueOf(args[1]);
			s = s + args[0] + " (" + String.valueOf(rowNumber) + ")";
		}
		// 输出的为具有属性值的符号,args格式为<种别码，属性值，行号>
		else if (args.length == 3) {
			rowNumber = Integer.valueOf(args[2]);
			s = s + args[0] + " :" + args[1] + " (" + String.valueOf(rowNumber) + ")";
		} else {
			System.out.println("ERROR!");
		}
		parsingResult.add(s);
	}

	// 获得预测分析表中的产生式以及对应的select集,存储格式为：<产生式左部-当前输入符号，产生式右部>
	public void raedPredictTable() {
		String textLine;
		try {
			predictionTable = new HashMap<String, String>();
			BufferedReader bufReader = new BufferedReader(new FileReader(new File("prediction_table.txt")));
			while ((textLine = bufReader.readLine()) != null) {
				String productionLeft   = textLine.split("#")[0];
				String inputSymbol 		= (textLine.split("#")[1]).split("->")[0].trim();
				String productionRight  = (textLine.split("#")[1]).split("->")[1].trim();
				predictionTable.put(productionLeft + "-" + inputSymbol, productionRight);
			}
			bufReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getResult(){
		return new ArrayList<String>(parsingResult);
	}

	public List<String[]> getError(){
		return new ArrayList<String[]>(errorMessages);
	}
	// ------------------------------------------------------------------------------




	// --------------------------------------PDA--------------------------------------
	//执行语法分析的PDA，在符号出栈时打印输出
	public void PDA() {
		// 初始符号压入栈
		stack.add(new String[] { "Program", "0" });
		while (stack.size() > 0 && inputCache.size() > 0) {
			System.out.println("栈顶元素："+stack.get(stack.size() - 1)[0]+"  当前输入信号:"+inputCache.get(0)[0]);
			// 输入缓冲区与栈顶终结符相等时，栈顶符号出栈,输入指针指向下一个输入符号
			if (inputCache.get(0)[0].equals(stack.get(stack.size() - 1)[0])) {
				popPrint(Integer.valueOf(stack.get(stack.size() - 1)[1]), inputCache.get(0));
				inputCache.remove(0);
				stack.remove(stack.size() - 1);
				continue;
			}
			else {
				// 根据当前栈顶字符和输入字符，在预测分析表中查找是否有对应产生式
				String productionRights;
				String productionLeft_Input = stack.get(stack.size() - 1)[0] + "-" + inputCache.get(0)[0];
				// 能够在预测分析表中找到匹配的产生式
				if ((productionRights = predictionTable.get(productionLeft_Input)) != null) {
					// 栈顶非终结符出栈
					int treeDepth = Integer.valueOf(stack.get(stack.size() - 1)[1]);
					popPrint(treeDepth, new String[] { stack.get(stack.size() - 1)[0] });
					stack.remove(stack.size() - 1);
					//产生式右部逆序入栈(即产生式右部最末的符号最先进栈)
					if (!productionRights.equals("$")) {
						String[] productionRight = productionRights.split(" ");
						for (int i = productionRight.length - 1; i > -1; i--) {
							stack.add(new String[] { productionRight[i], String.valueOf(treeDepth + 1) });
						}
					}
				}
				// 不能在预测分析表中找到匹配的产生式的话，报错
				else {
					errorMessages.add(new String[]{"Error at Line"+rowNumber,
								stack.get(stack.size() - 1)[0] + "无法通过文法规则转换成" + inputCache.get(0)[0]});
					inputCache.remove(0);
				}
			}
		}
	}
	// -------------------------------------------------------------------------------
}