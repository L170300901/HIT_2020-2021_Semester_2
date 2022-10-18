package bank.system;

import java.util.Map;
import java.util.List;
import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public class SemanticAnalyser {

	// ---------语法分析变量-------------
	int 				rowNumber; 			// 用于输出语法分析树中的结点对应的成分在输入文件中的行号
	List<String> 		stack; 				// PDA的栈结构，栈元素格式为<元素>
	List<String[]> 		inputCache; 		// PDA的输入缓冲区,词法分析的结果，格式为<种别码，属性值，行号>或<种别码，行号>
	Map<String, String>	predictionTable; 	// 预测分析表，格式为<产生式左部-输入符号,产生式右部>
	Grammar_handler 	grammar;			// 用于产生预测分析表的变量
	// ---------语义分析变量-------------
	int 			 	addrOffset;			// 为当前变量进行内存分配的起始地址(即相对于总内存开始位置的偏移量)
	int 			 	tempVariableNum;	// 指令翻译过程中用到的中间变量的个数
	int              	boolVariableNum;	// 指令翻译过程中用到的布尔变量的个数
	int              	inputCachePointer;	// 当前输入缓冲区的输入指针
	Stack<Node>      	node_stack; 		// 语义分析栈，用于存储节点相应属性，构建相应的注释分析树
	List<String[]>     	instructions; 		// 语义分析的得到的指令序列，格式为<四元式序列，三地址指令>
	List<Function>     	functions;			// 语义分析得到的函数名列表
	List<Identifier> 	symbolsTable;		// 语义分析得到的符号表
	List<String[]>   	errorMessages; 		// 语义分析产生的错误信息，格式为<行号，错误信息>
	// ---------错误处理变量-------------
	boolean	            isExpressionError;  // 用于判断在推导表达式时是否发生错误，在表达式推导完成时恢复false
	boolean	            isFuncError;		// 用于判断函数调用时是否发生错误，在函数调用完成时恢复false



	// ------------------------------Construct function------------------------------
	public SemanticAnalyser(List<String[]> inputCache) {
		this.rowNumber 			= 0;
		this.addrOffset 		= 0;
		this.tempVariableNum 	= 0;
		this.boolVariableNum 	= 0;
		this.inputCachePointer 	= 0;
		this.isExpressionError	= false;
		this.isFuncError		= false;
		this.inputCache      	= inputCache;
		this.node_stack			= new Stack<Node>();
		this.instructions		= new ArrayList<String[]>();
		this.functions			= new ArrayList<Function>();
		this.stack 				= new ArrayList<String>();
		this.errorMessages   	= new ArrayList<String[]>();
		this.symbolsTable		= new ArrayList<Identifier>();
		this.predictionTable 	= new HashMap<String, String>();
		this.grammar 			= new Grammar_handler();
		raedPredictTable();
	}
	// ------------------------------------------------------------------------------




	// ------------------------------test function------------------------------
	public static void main(String[] args) {
		List<String> program = new ArrayList<String>();
		// add source code here,end with "\n"
		program.add("void k(int p,char m){return 3;}int main(){struct bb{int d; int w; int k;}; "+
					"int a[3][3]; a[1]=main(); a=3+a; a[1]=mianc(); bb.m=3; dfa=9;}\n");
		//词法分析
		Scanning scanning = new Scanning(program);
		scanning.DFA();
		SemanticAnalyser test = new SemanticAnalyser(scanning.getToken_Parser_Input());
		//语法制导翻译
		test.PDA();
		System.out.println("语义分析结果：");
		System.out.println("code有："+test.instructions.size());
		for(String[] code : test.instructions){
			System.out.println(code[0]+"   "+code[1]);
		}
		System.out.println("id有："+test.symbolsTable.size());
		for(Identifier id : test.symbolsTable){
			System.out.println(id.getName()+" "+id.getLength()+" "+id.getOffset()+" "+id.getType()+" ");
		}
		System.out.println("函数有：");
		for (Function function : test.functions) {
			System.out.print(function.getReturnType()+" "+function.getName()+"( ");
			for(String type : function.paramTypes){
				System.out.print(type+" ");
			}
			System.out.println(")");
		}
		System.out.println("错误信息：");
		for (String[] testResult : test.errorMessages) {
			System.out.println(testResult[0]+" "+testResult[1]);
		}
	}
	// -------------------------------------------------------------------------




	// ------------------------------assistant function------------------------------
	// 获得预测分析表中的产生式以及对应的select集,存储格式为：<产生式左部-当前输入符号，产生式右部>
	public void raedPredictTable() {
		String textLine;
		try {
			predictionTable = new HashMap<String, String>();
			BufferedReader bufReader    = new BufferedReader(new FileReader(new File("E://2020fall/NOHTAEYUN/Bank/src/bank/system/prediction_table.txt")));
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

	//在符号出栈时处理行号
	public void rowNumberHandler(String[] args) {
		// 出栈的为没有属性值的符号,args格式为<种别码，行号>
		if (args.length == 2) {
			rowNumber = Integer.valueOf(args[1]);
		}
		// 出栈的为具有属性值的符号,args格式为<种别码，属性值，行号>
		else if (args.length == 3) {
			rowNumber = Integer.valueOf(args[2]);
		}
	}

	//判断当前的非终结符是否是一个语义动作符号
	public boolean isSemanticAction(String nonTerminal){
		if(nonTerminal.charAt(0) == 'M'){
			return true;
		}
		return false;
	}

	//判断输入的文法符号Symbol是否为一个非终结符
	public boolean isnonTerminal(String symbol){
		boolean isnonTerminal = false;
		for(String nonTerminal : grammar.nonterminals){
			if(symbol.equals(nonTerminal)){
				isnonTerminal = true;
				break;
			}
		}
		return isnonTerminal;
	}

	//根据名称获取当前符号表中的标识符
	private Identifier getIdentifier(String name){
		for(int i=0;i<symbolsTable.size();i++){
			if(symbolsTable.get(i).getName().equals(name))
				return symbolsTable.get(i);
		}
		return null;
	}

	//检查对应名称的标识符是否在当前符号表中
	private boolean isDeclaredSymbol(String name){
		for(int i=0;i<symbolsTable.size();i++){
			if(symbolsTable.get(i).getName().equals(name))
				return true;
		}
		return false;
	}

	//检查对应名称的函数是否在已声明函数列表中
	private boolean isDeclaredFunction(String FunctionName) {
		for(Function function : functions) {
			if(function.getName().equals(FunctionName)){
				return true;
			}
		}
		return false;
	}

	//根据名称获取当前已声明函数列表中的函数
	private Function getFunction(String FunctionName){
		for(Function function : functions) {
			if(function.getName().equals(FunctionName)){
				return function;
			}
		}
		return null;
	}

	//获取输入缓冲区中pos位置的token的属性值，null表示该token没有属性值
	private String getTokenValue(int pos){
		if(inputCache.get(pos).length == 3){
			return inputCache.get(pos)[1];
		}
		return null;
	}

	//获取语义分析产生的符号表
	public List<String[]> getSymbolTable(){
		ArrayList<String[]> symbols = new ArrayList<String[]>();
		for(Identifier id : symbolsTable){
			symbols.add(new String[]{id.getName(),id.getType(),String.valueOf(id.getLength()),String.valueOf(id.getOffset())});
		}
		return symbols;
	}

	//获取语义分析产生的指令序列
	public List<String[]> getInstructions(){
		return new ArrayList<String[]>(instructions);
	}

	//获取语义分析产生的错误信息
	public List<String[]> getError(){
		return new ArrayList<String[]>(errorMessages);
	}
	// ------------------------------------------------------------------------------




	// --------------------------------------PDA--------------------------------------
	// 执行语法分析的PDA，在过程中进行语法制导翻译
	// 其中，语法分析栈在分析过程中的可压进所有文法符号，而语义分析栈仅压入非终结符
	// 注释分析树中只包括不是语义动作的非终结符
	// PDA执行流程{
	//		建立语法分析栈和语义分析栈，并将初始符号压入到两个栈中
	//		while(语法制导翻译未完成){
	//			若当前语法分析栈顶节点为非终结符，则语义分析栈弹栈，为语义动作做准备
	//			if(当前语法分析节点与输入缓冲区输入内容匹配【说明终结符匹配成功】){
	//				语法分析栈栈顶节点出栈，输入指针向下移动
	//			}
	//			else(当前语法分析节点与输入缓冲区输入内容不匹配【说明是非终结符 或 终结符匹配失败】){
	//				查找预测分析表，选择对应的产生式对当前非终结符进行替换
	//				if(预测分析表查找成功【当前非终结符可被替换】){
	//					若当前非终结符为语义动作，则执行相应的语义动作
	//					语法分析栈的栈顶符号出栈
	//					找到的产生式的右部所有符号逆序进语法分析栈，所有非终结符逆序进语义分析栈
	//					注释分析树中添加相应的父子节点关系，只添加不是语义动作的非终结符
	// 					【是语义动作的非终结符 不会被添加进任何一个node的son列表中，但语义动作的node可以具有father】
	//				}
	//				else(预测分析表查找失败【当前文法符号不可被替换】){
	//					错误处理
	//				}
	//			}
	//  	}
	// }
	public void PDA() {
		// 初始符号压入栈
		node_stack.push(new Node("Program",null));
		stack.add("Program");
		//开始进行语法制导翻译，当(语法分析符号栈空)或(输入缓冲区指针指向结尾)时，终止分析。
		while (stack.size() > 0 && inputCachePointer<inputCache.size()) {
			//当栈顶符号是非终结符时，弹出语法树中相应的节点(二者name字段相同)，为语义动作的处理做准备
			Node node = null;
			try{
				if(isnonTerminal(stack.get(stack.size() - 1))){
					node=node_stack.pop();
				}
			}catch(Exception e){
				//符号栈已空，输入栈仍然有字符存在，则终止当前语法分析并报错
				errorMessages.add(new String[]{String.valueOf(rowNumber),"存在多余的输入内容，语法分析已终止"});
				break;
			}
			// 输入缓冲区的终结符与栈顶终结符相等时，栈顶符号匹配成功
			// 栈顶符号出栈,输入指针指向下一个输入符号
			if (inputCache.get(inputCachePointer)[0].equals(stack.get(stack.size() - 1))) {
				rowNumberHandler(inputCache.get(inputCachePointer));
				stack.remove(stack.size() - 1);
				inputCachePointer++;
			}
			else {
				// 根据当前栈顶字符和输入字符，在预测分析表中查找是否有对应产生式
				String productionRights;
				String productionLeft_Input = stack.get(stack.size() - 1) + "-" + inputCache.get(inputCachePointer)[0];
				// 能够在预测分析表中找到匹配的产生式
				if ((productionRights = predictionTable.get(productionLeft_Input)) != null) {
					// 栈顶非终结符如果是语义动作，则在出栈前执行相应代码片段
					if(isSemanticAction(stack.get(stack.size() - 1))){
						semanticAction(stack.get(stack.size() - 1),node);
					}
					// 栈顶非终结符出栈
					rowNumberHandler(new String[] { stack.get(stack.size() - 1) });
					stack.remove(stack.size() - 1);
					// 产生式右部逆序入栈(即产生式右部最末的符号最先进栈)，“空”符号$不进栈
					// 同时将产生式右部所有非终结符都逆序添加到语义分析栈中，构造注释分析树
					if (!productionRights.equals("$")) {
						String[] productionRight = productionRights.split(" ");
						for (int i = productionRight.length - 1; i > -1; i--) {
							stack.add(productionRight[i]);
							// 如果产生式右部出现了非终结符，则为其建立语义分析节点并语义分析栈中(语义分析栈中包含语法动作)
							// 同时添加该节点在注释分析树中的父节点和子节点(注释分析树中不含语法动作)
							if(isnonTerminal(productionRight[i])){
								Node sonNode=new Node(productionRight[i], node);
								if(sonNode.getName().charAt(0)!='M'){
									node.sons.add(0,sonNode);
								}
								node_stack.push(sonNode);
							}
						}
					}
				}
				// 不能在预测分析表中找到匹配的产生式的话，报错
				// 通过在GUI中先调用语法分析器并检查错误列表的方式，可避免语法制导翻译中出现此类错误
				else {
					/*errorMessages.add(new String[]{"Error at Line"+rowNumber,
								stack.get(stack.size() - 1) + "无法通过文法规则转换成" + inputCache.get(inputCachePointer)[0]});*/
					errorMessages.add(new String[]{""+rowNumber, "语法分析阶段存在错误，请使用语法分析进行检查"});
					inputCachePointer++;
				}
			}
		}
	}
	// -------------------------------------------------------------------------------




	// ---------------------------------------语义动作---------------------------------
	// 在执行semanticAction函数时：（以产生式const -> INT M74_2为例）
	// node为相应的语义动作：M74_2
	// node.getFather()为语义动作M74_2对应的产生式左部非终结符const
	// 函数说明：
	// gen(s):生成指令s
	// newTemp():生成新的中间变量
	// newBool():生成新的布尔变量
	// backpatch(x, s):向指令序列中x处回填入指令s
	// removeSymbol(x):从符号表中自底向上移除x个符号
	// latesetSymbol(): 表示符号表中最后一个符号(即：最新生成的符号)
	// enter(name,type,offset,length):向符号表中插入具有四个属性的新符号
	// lookup(X.lexeme)：表示在输入缓冲区中查找词法分析器得到的Token符号X的属性值
	public void semanticAction(String nonTerminal,Node node){

		// 含有A1的文法：
		// Define_Sentense -> Type ID M_A1 Array M_A2 Indentifiers ; M_E2
		// List_Member -> Type ID M_A1 Array ; M_A4 List_Member'
		// List_Member' -> Type ID M_A1 Array ; M_A5 List_Member'
		// A1内容：
		// Array.name=lookup(ID.lexeme); Array.type=Type.type; Array.length=Type.length; Array.dimension=0;
		if(nonTerminal.equals("M_A1")){
			Node father=node.getFather();
			father.sons.get(1).attribute.put("name", getTokenValue(inputCachePointer-1));
			father.sons.get(1).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.sons.get(1).attribute.put("length", father.sons.get(0).attribute.get("length"));
			father.sons.get(1).attribute.put("dimension", "0");
		}

		// 含有A2的文法：
		// Define_Sentense -> Type ID M_A1 Array M_A2 Indentifiers ; M_E2
		// A2内容：
		// Indentifiers.type=Type.type; Indentifiers.length=Type.length;
		else if(nonTerminal.equals("M_A2")){
			Node father=node.getFather();
			father.sons.get(2).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.sons.get(2).attribute.put("length", father.sons.get(0).attribute.get("length"));
		}

		// 含有A3的文法：
		// Define_Sentense -> struct ID M_A3 { List_Member } ; M_E2
		// A3内容：
		// List_Member.name=lookup(ID.lexeme); List_Member.type="struct"
		else if(nonTerminal.equals("M_A3")){
			Node father=node.getFather();
			father.sons.get(0).attribute.put("name", getTokenValue(inputCachePointer-1));
			father.sons.get(0).attribute.put("type", "struct");
		}

		// 含有A4的文法：
		// List_Member -> Type ID M_A1 Array ; M_A4 List_Member'
		// A4内容：
		// List_Member'.name=List_Member.name; List_Member'.type=struct"+"("+Type.type;
		// List_Member'.length=latesetSymbol().length; List_Member'.member_num=1;
		else if(nonTerminal.equals("M_A4")){
			Node father=node.getFather();
			father.sons.get(2).attribute.put("name", father.attribute.get("name"));
			father.sons.get(2).attribute.put("type", "struct"+"("+symbolsTable.get(symbolsTable.size()-1).getType());
			father.sons.get(2).attribute.put("length", String.valueOf(symbolsTable.get(symbolsTable.size()-1).getLength()));
			father.sons.get(2).attribute.put("member_num", String.valueOf(1));
		}

		// 含有A5的文法：
		// List_Member' -> Type ID M_A1 Array ; M_A5 List_Member'
		// A5内容：
		// List_Member'1.name=List_Member'.name; List_Member'1.type=List_Member'.type+","+Type.type;
		// List_Member'1.length=List_Member'.length+Type.length; List_Member'1.member_num=List_Member'.member_num+1;
		else if(nonTerminal.equals("M_A5")){
			Node father=node.getFather();
			father.sons.get(2).attribute.put("name", father.attribute.get("name"));
			father.sons.get(2).attribute.put("type",father.attribute.get("type")+","+symbolsTable.get(symbolsTable.size()-1).getType());
			father.sons.get(2).attribute.put("length",
					String.valueOf(Integer.valueOf(father.attribute.get("length"))+symbolsTable.get(symbolsTable.size()-1).getLength()));
			father.sons.get(2).attribute.put("member_num", String.valueOf(Integer.valueOf(father.attribute.get("member_num"))+1));
		}

		// 含有A6的文法：
		// List_Member' -> M_A6
		// A6内容：
		// removeSymbol(List_Member'.member_num);
		// enter(List_Member'.name, List_Member'.type+")", offset-List_Member'.length, List_Member'.length);
		else if(nonTerminal.equals("M_A6")){
			Node father=node.getFather();
			String type=father.attribute.get("type")+")";
			int length=Integer.parseInt(father.attribute.get("length"));
			int structStartOffset=symbolsTable.get(symbolsTable.size()-Integer.valueOf(father.attribute.get("member_num"))).getOffset();
			Identifier id=new Identifier(father.attribute.get("name"),type, structStartOffset, length);
			int startIndex =symbolsTable.size()-Integer.valueOf(father.attribute.get("member_num"));
			for(int i=0;i<Integer.valueOf(father.attribute.get("member_num"));i++){
				Identifier idRemove = symbolsTable.get(startIndex);
				id.members.add(idRemove);
				symbolsTable.remove(startIndex);
			}
			symbolsTable.add(id);
		}

		// 含有A7的文法：
		// Array -> M_A7
		// A7内容：
		// 若为定义语句中的数组,则使用该产生式说明数组/变量定义完成，只需向符号表中增加该数组/变量
		// enter(array.name, array.type, offset, array.length); offset+=array.length;
		// 若为执行语句中的数组
		// array维度为0   : array.value=array.name array.value=array.name
		// array维度不为0 :
		// 偏移地址=arr1*w1+arr2*w2+…+arrk*wk;  array.value=newtemp(ti);
		// array.val=name[偏移地址];  gen(ti := array.name+[偏移地址]);
		else if(nonTerminal.equals("M_A7")){
			Node father=node.getFather();
			//定义语句中的数组
			if(father.attribute.get("length")!=null){
				int length=Integer.parseInt(father.attribute.get("length"));
				Identifier id=new Identifier(father.attribute.get("name"),father.attribute.get("type"), addrOffset, length);
				addrOffset+=length;
				//若为数组类型，则向id中增加数组各维的下标
				int dimension=Integer.parseInt(father.attribute.get("dimension"));
				for(int i=0;i<dimension;i++){
					id.arr_list.add(Integer.parseInt(father.attribute.get("arr"+i)));
				}
				symbolsTable.add(id);
			}
			//执行语句中的数组
			else{
				String name=father.attribute.get("name");
				if(isDeclaredSymbol(name)){
					Identifier id=getIdentifier(name);
					String type=id.getType();
					if(id.arr_list.size()>0){
						String t="t"+(tempVariableNum++);
						if(id.arr_list.size() == Integer.valueOf(father.attribute.get("dimension"))){
							int dimension=id.arr_list.size();
							int ofst=0;
							int width=1;
							for(int i=dimension-1;i>=0;i--){
								int arr=Integer.parseInt(father.attribute.get("arr"+i));
								ofst+=arr*width;
								width*=id.arr_list.get(i);
							}
							if(type.equals("int")||type.equals("long")||type.equals("float"))
								ofst*=4;
							else if(type.equals("double"))
								ofst*=8;
							else if(type.equals("short"))
								ofst*=2;
							instructions.add(new String[]{"(:=,"+name+"["+ofst+"],_,"+t+")" , t+" := "+name+"["+ofst+"]"});
							father.attribute.put("value", t);
							father.attribute.put("val", name+"["+ofst+"]");
						}
						else{
							errorMessages.add(new String[]{String.valueOf(rowNumber),"数组"+name+"的维度被定义为"+
								id.arr_list.size()+"，不能以"+father.attribute.get("dimension")+"维数组的方式调用"});
							isExpressionError = true;
						}
					}else{
						father.attribute.put("value", name);
					}
				}
			}
		}

		// 含有A8的文法：
		// Array -> [ INT ] M_A8 Array M_A9
		// A8内容：
		// Array1.name=Array.name;  Array1.type=Array.type;  Array1.length=Array.length*lookup(INT.lexeme);
		// Array1.dimension=Array.dimension+1;  Array1.arr(Array.dimension)=INT;  Array1.arr(i)=Array.arr(i);
		// arr(i)属性为数组第i维的下标。(int a[3][4]则arr0=3，arr1=4)
		else if(nonTerminal.equals("M_A8")){
			Node father=node.getFather();
			int num=Integer.parseInt(getTokenValue(inputCachePointer-2));
			int father_dimension=Integer.parseInt(father.attribute.get("dimension"));
			father.sons.get(0).attribute.put("name", father.attribute.get("name"));
			father.sons.get(0).attribute.put("type", father.attribute.get("type"));
			father.sons.get(0).attribute.put("dimension", (father_dimension + 1) + "");
			father.sons.get(0).attribute.put("arr"+ father_dimension, ""+num);
			for(int i=0;i<father_dimension;i++){
				father.sons.get(0).attribute.put("arr"+ i, ""+father.attribute.get("arr"+i));
			}
			if(father.attribute.get("length")!=null){//定义语句中的数组
				father.sons.get(0).attribute.put("length", Integer.parseInt(father.attribute.get("length")) * num + "");
			}
		}

		// 含有A9的文法：
		// Factor_Multi -> ID M_A40 Call M_A9
		// Factor_Multi -> Constant M_A9
		// Call -> M_A42 Array M_A9 M_E3
		// A9内容：
		// 对于文法：Factor_Multi -> ID M_A40 Call M_A9
		// 	内容为：Factor_Multi.value=call.value; 		Factor_Multi.val=call.val;;		Factor_Multi.type=call.type;
		// 对于文法：Factor_Multi -> Constant M_A9
		// 	内容为：Factor_Multi.value=Constant.value; 	Factor_Multi.val=Constant.val;	Factor_Multi.type=Constant.type;
		// 对于文法：Call -> M_A42 Array M_A9 M_E3
		//	内容为：Call.value=Array.value; 			Call.val=Array.val;				Call.type=Array.type;
		else if(nonTerminal.equals("M_A9")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.attribute.put("val", father.sons.get(0).attribute.get("val"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// 含有A9_1的文法：
		// Array -> [ INT ] M_A8 Array M_A9
		// A9_1内容：
		// 对于文法：Array -> [ INT ] M_A8 Array M_A9
		//	内容为：Array.value=Array1.value; 			Array.val=Array1.val;           Array.type=Array1.type+"[]";
		else if(nonTerminal.equals("M_A9_1")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.attribute.put("val", father.sons.get(0).attribute.get("val"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type")/*+"[]"*/ );
		}

		// 含有A10的文法：
		// Indentifiers -> , ID M_A10 Array M_A11 Indentifiers
		// A10内容：
		// Array.type=Indentifiers.type;  Array.length=type.length;
		// Array.name=lookup(ID.lexeme);  Array.dimension=0;
		else if(nonTerminal.equals("M_A10")){
			Node father=node.getFather();
			father.sons.get(0).attribute.put("type", father.attribute.get("type"));
			father.sons.get(0).attribute.put("length", father.attribute.get("length"));
			father.sons.get(0).attribute.put("name", getTokenValue(inputCachePointer-1));
			father.sons.get(0).attribute.put("dimension", "0");
		}

		// 含有A11的文法：
		// Indentifiers -> , ID M_A10 Array M_A11 Indentifiers
		// A11内容：
		// Indentifiers1.type=Array.type;  Indentifiers1.length=Array.length;
		else if(nonTerminal.equals("M_A11")){
			Node father=node.getFather();
			father.sons.get(1).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.sons.get(1).attribute.put("length", father.sons.get(0).attribute.get("length"));
		}

		// 含有A12的文法：
		// Sentense -> if ( Expression ) M_A12 Block M_A13 Else_Body M_A14
		// Sentense -> while ( M_A15 Expression ) M_A12 Block M_A16
		// A12内容：
		// gen(if "+E.value+" goto "+(instructions.size()+2)); Sentense.backpatch=instructions.size(); gen(null);
		else if(nonTerminal.equals("M_A12")){
			Node father=node.getFather();
			String b=father.sons.get(0).attribute.get("value");
			instructions.add(new String[]{"(goto,"+b+",_,"+(instructions.size()+2)+")" , "if "+b+" goto "+(instructions.size()+2)});
			father.attribute.put("backpatch", ""+instructions.size());
			instructions.add(null);
		}

		// 含有A13的文法：
		// Sentense -> if ( Expression ) M_A12 Block M_A13 Else_Body M_A14
		// A13内容：
		// gen(null);  backpatch(Sentense.backpatch, "goto "+instructions.size());  Sentense.backpatch=instructions.size()-1;
		else if(nonTerminal.equals("M_A13")){
			Node father=node.getFather();
			int backpatch=Integer.parseInt(father.attribute.get("backpatch"));
			instructions.add(null);
			instructions.set(backpatch, new String[]{"(goto,_,_,"+instructions.size()+")" , "goto "+instructions.size()});
			father.attribute.put("backpatch", ""+(instructions.size()-1));
		}

		// 含有A14的文法：
		// Sentense -> if ( Expression ) M_A12 Block M_A13 Else_Body M_A14
		// A14内容：
		// backpatch(Sentense.backpatch, "goto "+instructions.size());
		else if(nonTerminal.equals("M_A14")){
			Node father=node.getFather();
			int backpatch=Integer.parseInt(father.attribute.get("backpatch"));
			instructions.set(backpatch, new String[]{"(goto,_,_,"+instructions.size()+")" , "goto "+instructions.size()});
		}

		// 含有A15的文法：
		// Sentense -> while ( M_A15 Expression ) M_A12 Block M_A16
		// A15内容：
		// Sentense.backto=instructions.size();
		else if(nonTerminal.equals("M_A15")){
			Node father=node.getFather();
			father.attribute.put("backto", ""+instructions.size());
		}

		// 含有A16的文法：
		// Sentense -> while ( M_A15 Expression ) M_A12 Block M_A16
		// A16内容：
		// gen(goto Sentense.backto);  backpatch(Sentense.backpatch, "goto "+instructions.size());
		else if(nonTerminal.equals("M_A16")){
			Node father=node.getFather();
			int backpatch=Integer.parseInt(father.attribute.get("backpatch"));
			int backto=Integer.parseInt(father.attribute.get("backto"));
			instructions.add(new String[]{"(goto,_,_,"+backto+")" , "goto "+backto});
			instructions.set(backpatch, new String[]{"(goto,_,_,"+instructions.size()+")" , "goto "+instructions.size()});//回填
		}

		// 含有A17的文法：
		// Expression -> Value M_A17 Expression' M_A18
		// Value -> Add_Item M_A17 Add_Items M_A18
		// Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// A17内容：
		// 对于文法：Expression -> Value M_A17 Expression' M_A18
		// 	内容为： Expression'.value=Value.value;  Expression'.val=Value.val; Expression'.type=Value.type;
		// 对于文法：Value -> Add_Item M_A17 Add_Items M_A18,
		// 	内容为：Add_Items.value=Add_Item.value; Add_Items.val=Add_Item.val; Add_Items.type=Add_Item.type;
		// 对于文法：Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// 	内容为：Factor_Multis.value=Factor_Multi.value; Factor_Multis.val=Factor_Multi.val; Factor_Multis.type=Factor_Multi.type;
		else if(nonTerminal.equals("M_A17")){
			Node father=node.getFather();
			father.sons.get(1).attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.sons.get(1).attribute.put("val", father.sons.get(0).attribute.get("val"));
			father.sons.get(1).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// 含有A18的文法：
		// Expression -> Value M_A17 Expression' M_A18
		// Value -> Add_Item M_A17 Add_Items M_A18
		// Add_Items -> + Add_Item M_A31 Add_Items M_A18
		// Add_Items -> - Add_Item M_A32 Add_Items M_A18
		// Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// Factor_Multis -> * Factor_Multi M_A33 Factor_Multis M_A18
		// Factor_Multis -> / Factor_Multi M_A34 Factor_Multis M_A18
		// Factor_Multis -> % Factor_Multi M_A35 Factor_Multis M_A18
		// A18内容：
		// 对于文法：Expression -> Value M_A17 Expression' M_A18
		// 	内容为： Expression.value=Expression'.value; Expression.val=Expression'.val;
		// 对于文法：Value -> Add_Item M_A17 Add_Items M_A18,
		// 	内容为：Value.value=Add_Items.value; Value.val=Add_Items.val;
		// 对于文法：Add_Items -> + Add_Item M_A31 Add_Items M_A18 和 Add_Items -> - Add_Item M_A32 Add_Items M_A18
		// 	内容为：Add_Items.value=Add_Items1.value; Add_Items.val=Add_Items1.val;
		// 对于文法：Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// 	内容为：Add_Items.value=Factor_Multis.value; Add_Items.val=Factor_Multis.val;
		// 对于文法：Factor_Multis -> * Factor_Multi M_A33 Factor_Multis M_A18
		// 			Factor_Multis -> / Factor_Multi M_A34 Factor_Multis M_A18
		// 		    Factor_Multis -> % Factor_Multi M_A35 Factor_Multis M_A18
		// 	内容为：Factor_Multis.value=Factor_Multis.value; Factor_Multis.val=Factor_Multis.val;
		else if(nonTerminal.equals("M_A18")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(1).attribute.get("value"));
			father.attribute.put("val", father.sons.get(1).attribute.get("val"));
		}

		// 含有A19的文法：
		// Expression' -> < Value M_A19
		// A19内容：
		// b=newBool();  gen(bi+" := "+Expression'.value+" < "+Value.value);  Expression'.value=bi;
		else if(nonTerminal.equals("M_A19")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String b="b"+(boolVariableNum++);
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(<,"+inh+","+value+","+b+")" , b+" := "+inh+" < "+value});
			}
			father.attribute.put("value", b);
		}

		// 含有A20的文法：
		// Expression' -> <= Value M_A20
		// A20内容：
		// b=newBool(); gen(bi+" := "+Expression'.value+" <= "+Value.value); Expression'.value=bi;
		else if(nonTerminal.equals("M_A20")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String b="b"+(boolVariableNum++);
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(<=,"+inh+","+value+","+b+")" , b+" := "+inh+" <= "+value});
			}
			father.attribute.put("value", b);
		}

		// 含有A21的文法：
		// Expression' -> > Value M_A21
		// A21内容：
		// b=newBool(); gen(bi+" := "+Expression'.value+" > "+Value.value); Expression'.value=bi;
		else if(nonTerminal.equals("M_A21")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String b="b"+(boolVariableNum++);
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(>,"+inh+","+value+","+b+")" , b+" := "+inh+" > "+value});
			}
			father.attribute.put("value", b);
		}

		// 含有A22的文法：
		// Expression' -> >= Value M_A22
		// A22内容：
		// b=newBool(); gen(bi+" := "+Expression'.value+" >= "+Value.value); Expression'.value=bi;
		else if(nonTerminal.equals("M_A22")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String b="b"+(boolVariableNum++);
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(>=,"+inh+","+value+","+b+")" , b+" := "+inh+" >= "+value});
			}
			father.attribute.put("value", b);
		}

		// 含有A23的文法：
		// Expression' -> == Value M_A23
		// A23内容：
		// b=newBool(); gen(bi+" := "+Expression'.value+" == "+Value.value); Expression'.value=bi;
		else if(nonTerminal.equals("M_A23")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String b="b"+(boolVariableNum++);
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(==,"+inh+","+value+","+b+")" , b+" := "+inh+" == "+value});
			}
			father.attribute.put("value", b);
		}

		// 含有A24的文法：
		// Expression' -> != Value M_A24
		// A24内容：
		// b=newBool(); gen(bi+" := "+Expression'.value+" != "+Value.value); Expression'.value=bi;
		else if(nonTerminal.equals("M_A24")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String b="b"+(boolVariableNum++);
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(!=,"+inh+","+value+","+b+")" , b+" := "+inh+" != "+value});
			}
			father.attribute.put("value", b);
		}

		// 含有A25的文法：
		// Expression' -> = Value M_A25
		// A25内容：
		// Value.val不为空: remove(instruction.startsWith(Expression'.value)); gen(Expression'.val+" := "+Value.value);
		//					Expression'.value=Expression'.val;
		// Value.val为空  : gen(Expression'.value+" := "+Value.value);
		else if(nonTerminal.equals("M_A25")){
			Node father=node.getFather();
			String inh=father.attribute.get("val");
			String value=father.sons.get(0).attribute.get("value");
			if(inh==null || inh.equals("null")){
				inh=father.attribute.get("value");
			}
			else{
				String temp=father.attribute.get("value");
				for(int i=instructions.size()-1;i>=0;i--){
					if(instructions.get(i)!=null&&instructions.get(i)[1].startsWith(temp)){
						instructions.remove(i);
					}
				}
			}
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(=,"+value+",_,"+inh+")" , inh+" := "+value});
			}
			father.attribute.put("value", inh);
		}

		// 含有A26的文法：
		// Expression' -> += Value M_A26
		// A26内容：
		// gen(Expression'.value+" := "+Expression'.value+" + "+Value.value); Expression'.value=Expression'.value+Value.value;
		else if(nonTerminal.equals("M_A26")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(+,"+inh+","+value+","+inh+")" , inh+" := "+inh+" + "+value});
			}
			father.attribute.put("value", inh);
		}

		// 含有A27的文法：
		// Expression' -> -= Value M_A27
		// A27内容：
		// gen(Expression'.value+" := "+Expression'.value+" - "+Value.value); Expression'.value=Expression'.value-Value.value;
		else if(nonTerminal.equals("M_A27")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(-,"+inh+","+value+","+inh+")" , inh+" := "+inh+" - "+value});
			}
			father.attribute.put("value", inh);
		}

		// 含有A28的文法：
		// Expression' -> *= Value M_A28
		// A28内容：
		// gen(Expression'.value+" := "+Expression'.value+" * "+Value.value); Expression'.value=Expression'.value*Value.value;
		else if(nonTerminal.equals("M_A28")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(*,"+inh+","+value+","+inh+")" , inh+" := "+inh+" * "+value});
			}
			father.attribute.put("value", inh);
		}

		// 含有A29的文法：
		// Expression' -> /= Value M_A29
		// A29内容：
		// gen(Expression'.value+" := "+Expression'.value+" / "+Value.value); Expression'.value=Expression'.value/Value.value;
		else if(nonTerminal.equals("M_A29")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(/,"+inh+","+value+","+inh+")" , inh+" := "+inh+" / "+value});
			}
			father.attribute.put("value", inh);
		}

		// 含有A30的文法：
		// Expression' -> %= Value M_A30
		// A30内容：
		// gen(Expression'.value+" := "+Expression'.value+" % "+Value.value); Expression'.value=Expression'.value%Value.value;
		else if(nonTerminal.equals("M_A30")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			if(isExpressionError){
				isExpressionError = false;
			}
			else{
				instructions.add(new String[]{"(%,"+inh+","+value+","+inh+")" , inh+" := "+inh+" % "+value});
			}
			father.attribute.put("value", inh);
		}

		// 含有A31的文法：
		// Add_Items -> + Add_Item M_A31 Add_Items M_A18
		// A31内容：
		// gen(newTemp(ti)+" := "+Add_Items.value+" + "+Add_Item.value); Add_Items1.value=ti;
		else if(nonTerminal.equals("M_A31")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String t="t"+(tempVariableNum++);
			if(!isExpressionError){
				instructions.add(new String[]{"(+,"+inh+","+value+","+t+")" , t+" := "+inh+" + "+value});
			}
			father.sons.get(1).attribute.put("value", t);
			father.sons.get(1).attribute.put("type", father.attribute.get("type"));
		}

		// 含有A32的文法：
		// Add_Items -> - Add_Item M_A32 Add_Items M_A18
		// A32内容：
		// gen(newTemp(ti)+" := "+Add_Items.value+" - "+Add_Item.value); Add_Items1.value=ti;
		else if(nonTerminal.equals("M_A32")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String t="t"+(tempVariableNum++);
			if(!isExpressionError){
				instructions.add(new String[]{"(-,"+inh+","+value+","+t+")" , t+" := "+inh+" - "+value});
			}
			father.sons.get(1).attribute.put("value", t);
			father.sons.get(1).attribute.put("type", father.attribute.get("type"));
		}

		// 含有A33的文法：
		// Factor_Multis -> * Factor_Multi M_A33 Factor_Multis M_A18
		// A33内容：
		// gen(newTemp(ti)+" := "+Factor_Multis.value+" * "+Factor_Multi.value); Factor_Multis1.value=ti;
		// Factor_Multis1.type=Factor_Multis.type;
		else if(nonTerminal.equals("M_A33")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String t="t"+(tempVariableNum++);
			if(!isExpressionError){
				instructions.add(new String[]{"(*,"+inh+","+value+","+t+")" , t+" := "+inh+" * "+value});
			}
			father.sons.get(1).attribute.put("value", t);
			father.sons.get(1).attribute.put("type", father.attribute.get("type"));
		}

		// 含有A34的文法：
		// Factor_Multis -> / Factor_Multi M_A34 Factor_Multis M_A18
		// A34内容：
		// gen(newTemp(ti)+" := "+Factor_Multis.value+" / "+Factor_Multi.value); Factor_Multis1.value=ti;
		// Factor_Multis1.type=Factor_Multis.type;
		else if(nonTerminal.equals("M_A34")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String t="t"+(tempVariableNum++);
			if(!isExpressionError){
				instructions.add(new String[]{"(/,"+inh+","+value+","+t+")" , t+" := "+inh+" / "+value});
			}
			father.sons.get(1).attribute.put("value", t);
			father.sons.get(1).attribute.put("type", father.attribute.get("type"));
		}

		// 含有A35的文法：
		// Factor_Multis -> % Factor_Multi M_A35 Factor_Multis M_A18
		// A35内容：
		// gen(newTemp(ti)+" := "+Factor_Multis.value+" % "+Factor_Multi.value); Factor_Multis1.value=ti;
		// Factor_Multis1.type=Factor_Multis.type;
		else if(nonTerminal.equals("M_A35")){
			Node father=node.getFather();
			String inh=father.attribute.get("value");
			String value=father.sons.get(0).attribute.get("value");
			String t="t"+(tempVariableNum++);
			if(!isExpressionError){
				instructions.add(new String[]{"(%,"+inh+","+value+","+t+")" , t+" := "+inh+" % "+value});
			}
			father.sons.get(1).attribute.put("value", t);
			father.sons.get(1).attribute.put("type", father.attribute.get("type"));
		}

		// 含有A36的文法：
		// Factor_Multi -> ! Factor_Multi M_A36
		// A36内容：
		// Factor_Multis.value=newBool(bi); Factor_Multi.type=Factor_Multi1.type; gen(bi+" := ~"+Factor_Multis1.value);
		else if(nonTerminal.equals("M_A36")){
			Node father=node.getFather();
			String f1="b"+(boolVariableNum++);
			String f2=father.sons.get(0).attribute.get("value");
			if(!isExpressionError){
				instructions.add(new String[]{"(=~,"+f2+",_,"+f1+")" , f1+" := ~"+f2});
			}
			father.attribute.put("value", f1);
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// 含有A37的文法：
		// Factor_Multi -> ++ Factor_Multi M_A37
		// A37内容：
		// Factor_Multis.value=newTemp(ti); Factor_Multi.type=Factor_Multi1.type; gen(ti+" := "+Factor_Multis1.value+" + 1");
		else if(nonTerminal.equals("M_A37")){
			Node father=node.getFather();
			String f1="t"+(tempVariableNum++);
			String f2=father.sons.get(0).attribute.get("value");
			if(!isExpressionError){
				instructions.add(new String[]{"(+,"+f2+",1,"+f1+")" , f1+" := "+f2+" + 1"});
			}
			father.attribute.put("value", f1);
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// 含有A38的文法：
		// Factor_Multi -> -- Factor_Multi M_A38
		// A38内容：
		// Factor_Multis.value=newTemp(ti); Factor_Multi.type=Factor_Multi1.type; gen(ti+" := "+Factor_Multis1.value+" - 1");
		else if(nonTerminal.equals("M_A38")){
			Node father=node.getFather();
			String f1="t"+(tempVariableNum++);
			String f2=father.sons.get(0).attribute.get("value");
			if(!isExpressionError){
				instructions.add(new String[]{"(-,"+f2+",1,"+f1+")" , f1+" := "+f2+" - 1"});
			}
			father.attribute.put("value", f1);
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// 含有A39的文法：
		// Factor_Multi -> ( Expression ) M_A39
		// A39内容：
		// Factor_Multis.value=Expression.value; Factor_Multi.type=Expression.type;
		else if(nonTerminal.equals("M_A39")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// 含有A40的文法：
		// Factor_Multi -> ID M_A40 Call M_A9
		// A40内容：
		// Call.name=lookup(ID.lexeme);;
		else if(nonTerminal.equals("M_A40")){
			Node father=node.getFather();
			father.sons.get(0).attribute.put("name", getTokenValue(inputCachePointer-1));
		}

		// 含有A41的文法：
		// Factor_Multi -> - Factor_Multi M_A41
		// A41内容：
		// Factor_Multi.value=newTemp(ti); Factor_Multi.type=Factor_Multi1.type; gen(ti+" := 0 - "+Factor_Multi1.value);
		else if(nonTerminal.equals("M_A41")){
			Node father=node.getFather();
			String f1="t"+(tempVariableNum++);
			String f2=father.sons.get(0).attribute.get("value");
			instructions.add(new String[]{"(-,0,"+f2+","+f1+")" , f1+" := 0 - "+f2});
			father.attribute.put("value", f1);
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// 含有A42的文法：
		// Call -> M_E3 M_A42 Array M_A9
		// A42内容：
		// Array.name=Call.name; Array.dimension=0;
		else if(nonTerminal.equals("M_A42")){
			Node father=node.getFather();
			if(!isExpressionError){
				father.sons.get(0).attribute.put("type", getIdentifier(getTokenValue(inputCachePointer-1)).getType());
				father.sons.get(0).attribute.put("name", father.attribute.get("name"));
				father.sons.get(0).attribute.put("dimension", "0");
			}
		}

		// 含有A43的文法：
		// Call -> M_E4 ( Pass_Parameters ) M_A43
		// A43内容：
		// Call.value=return(Call.name); Call.type=Function(Call.name).type;
		else if(nonTerminal.equals("M_A43")){
			//call -> ( Es ) M102_1
			Node father=node.getFather();
			if(!isExpressionError){
				father.attribute.put("value","return("+father.attribute.get("name")+")");
				father.attribute.put("type",getFunction(father.attribute.get("name")).getReturnType());
			}
		}

		// 含有A44的文法：
		// Call -> . ID M_A44 M_E5
		// A44内容：
		// Call.val= Call.name+"{"+lookup(ID.lexeme)+"}"; Call.value=Call.name{offset}; Call.type=Struct(Call.name).lookup(ID.lexeme);
		else if(nonTerminal.equals("M_A44")){
			Node father=node.getFather();
			for(Identifier id : symbolsTable){
				if(id.getName().equals(father.attribute.get("name"))){
					for(Identifier memberId :id.members){
						if(memberId.getName().equals(getTokenValue(inputCachePointer-1))){
							father.attribute.put("val", father.attribute.get("name")+"{"+(memberId.getOffset()-id.getOffset())+"}");
							father.attribute.put("value", father.attribute.get("name")+"{"+(memberId.getOffset()-id.getOffset())+"}");
							father.attribute.put("type", memberId.getType());
						}
					}
				}
			}
		}

		// 含有A45的文法：
		// Pass_Parameters -> Expression M_A45 Pass_Parameters' M_A46
		// Pass_Parameters' -> , Expression M_A45 Pass_Parameters' M_A46
		// A45内容：
		// gen("param "+Expression.value);  Pass_Parameters'1.name=Pass_Parameters'.name; Pass_Parameters'1.name=Pass_Parameters'.name;
		else if(nonTerminal.equals("M_A45")){
			Node father=node.getFather();
			father.sons.get(1).attribute.put("name", node.getFather().attribute.get("name"));
			father.sons.get(1).attribute.put("paramNum",String.valueOf(Integer.valueOf(father.attribute.get("paramNum"))+1));
			if(!isFuncError){
				instructions.add(new String[]{"(param,_,_,"+father.sons.get(0).attribute.get("value")+")"
								, "param "+father.sons.get(0).attribute.get("value")});
			}
		}

		// 含有A46的文法：
		// Pass_Parameters -> Expression M_A45 Pass_Parameters' M_A46
		// Pass_Parameters' -> , Expression M_A45 Pass_Parameters' M_A46
		// A46内容：
		// Pass_Parameters.paramNum=Pass_Parameters'.paramNum;
		else if(nonTerminal.equals("M_A46")){
			//改变生成调用函数的指令后，A46不再使用
		}

		// 含有A47的文法：
		// Pass_Parameters -> M_A47
		// Pass_Parameters' -> M_A47
		// A47内容：
		// 对于文法：Pass_Parameters -> M_A47  内容为： gen("call"+Call.name+","+Pass_Parameters.paramNum);
		// 对于文法：Pass_Parameters' -> M_A47 内容为： gen("call"+Call.name+","+Pass_Parameters'.paramNum);
		else if(nonTerminal.equals("M_A47")){
			Node father=node.getFather();
			if(isFuncError){
				isFuncError = false;
				isExpressionError = true;
			}
			else{
				instructions.add(new String[]{"(call,"+father.attribute.get("name")+","+father.attribute.get("paramNum")+",_)"
					, "call "+father.attribute.get("name")+","+father.attribute.get("paramNum")});
			}
		}

		// 含有A48的文法：
		// Type -> char M_A48
		// A48内容：
		// Type.type = char; Type.length = 1;
		else if(nonTerminal.equals("M_A48")){
			node.getFather().attribute.put("type", "char");
			node.getFather().attribute.put("length", "1");
		}

		// 含有A49的文法：
		// Type -> int M_A49
		// A49内容：
		// Type.type = int; Type.length = 4;
		else if(nonTerminal.equals("M_A49")){
			node.getFather().attribute.put("type", "int");
			node.getFather().attribute.put("length", "4");
		}

		// 含有A50的文法：
		// Type -> long M_A50
		// A50内容：
		// Type.type = long; Type.length = 4;
		else if(nonTerminal.equals("M_A50")){
			node.getFather().attribute.put("type", "long");
			node.getFather().attribute.put("length", "4");
		}
		// 含有A51的文法：
		// Type -> short M_A51
		// A51内容：
		// Type.type = short; Type.length = 2;
		else if(nonTerminal.equals("M_A51")){
			node.getFather().attribute.put("type", "short");
			node.getFather().attribute.put("length", "2");
		}

		// 含有A52的文法：
		// Type -> float M_A52
		// A52内容：
		// Type.type = float; Type.length = 4;
		else if(nonTerminal.equals("M_A52")){
			node.getFather().attribute.put("type", "float");
			node.getFather().attribute.put("length", "4");
		}

		// 含有A53的文法：
		// Type -> double M_A53
		// A53内容：
		// Type.type = char; Type.length = 8;
		else if(nonTerminal.equals("M_A53")){
			node.getFather().attribute.put("type", "double");
			node.getFather().attribute.put("length", "8");
		}

		// 含有A54的文法：
		// Constant -> INT M_A54
		// Constant -> FLOAT M_A54
		// Constant -> DOUBLE M_A54
		// Constant -> CHAR M_A54
		// A54内容：
		// const.value=lookup(INT/FLOAT/DOUBLE/CHAR.lexeme); const.type=int/float/double/char;
		else if(nonTerminal.equals("M_A54")){
			Node father=node.getFather();
			father.attribute.put("value", getTokenValue(inputCachePointer-1));
			String type;
			if(inputCache.get(inputCachePointer-1)[0].equals("INT")){
				type = "int";
			}
			else if(inputCache.get(inputCachePointer-1)[0].equals("FLOAT")){
				type = "float";
			}
			else if(inputCache.get(inputCachePointer-1)[0].equals("DOUBLE")){
				type = "double";
			}
			else if(inputCache.get(inputCachePointer-1)[0].equals("CHAR")){
				type = "char";
			}
			else{
				type = "";
			}
			father.attribute.put("type", type);
		}

		// 含有A55的文法：
		// Sentense -> return Expression M_A55 ;
		// A55内容：
		// gen(return Expression.value)
		else if(nonTerminal.equals("M_A55")){
			Node father=node.getFather();
			instructions.add(new String[]{"(return,_,_,"+father.sons.get(0).attribute.get("value")+")"
				, "return "+father.sons.get(0).attribute.get("value")});
		}

		// 错误处理1：判断函数重复声明
		else if(nonTerminal.equals("M_E1")){
			if(isDeclaredFunction(getTokenValue(inputCachePointer-1))){
				errorMessages.add(new String[]{String.valueOf(rowNumber),"函数"+getTokenValue(inputCachePointer-1)+"已声明过"});
			}
			else{
				String returnType = "";
				String FunctionName = getTokenValue(inputCachePointer-1);
				if(node.getFather().sons.size()==2){
					returnType = "void";
					node.getFather().sons.get(0).attribute.put("name",FunctionName);
				}
				if(node.getFather().sons.size()==3){
					returnType = node.getFather().sons.get(0).attribute.get("type");
					node.getFather().sons.get(1).attribute.put("name",FunctionName);
				}
				functions.add(new Function(FunctionName,returnType,new ArrayList<String>()));
				node.getFather().attribute.put("name",FunctionName);
			}
		}

		// 错误处理2：判断变量重复声明
		else if(nonTerminal.equals("M_E2")){
			for(int i=0;i<symbolsTable.size()-1;i++){
				if(symbolsTable.get(i).getName().equals(symbolsTable.get(symbolsTable.size()-1).getName())){
					errorMessages.add(new String[]{String.valueOf(rowNumber),
						"变量"+symbolsTable.get(symbolsTable.size()-1).getName()+"已声明过，忽略本次的重复声明"});
					symbolsTable.remove(symbolsTable.size()-1);
					break;
				}
			}
		}

		// 错误处理3：识别出未定义的变量名
		else if(nonTerminal.equals("M_E3")){
			boolean isDefined = false;
			for(int i=0;i<symbolsTable.size();i++){
				if(symbolsTable.get(i).getName().equals(node.getFather().attribute.get("name"))){
					isDefined = true;
					break;
				}
			}
			if(!isDefined){
				errorMessages.add(new String[]{String.valueOf(rowNumber),"变量"+node.getFather().attribute.get("name")+"未定义"});
				isExpressionError = true;
			}
		}

		// 错误处理4：识别出未定义的函数名,则删除已产生的param指令和call指令
		// 产生式：Call -> M_E4 ( Pass_Parameters ) M_A43
		else if(nonTerminal.equals("M_E4")){
			boolean isDefined = false;
			for(int i=0;i<symbolsTable.size();i++){
				if(isDeclaredFunction(node.getFather().attribute.get("name"))){
					isDefined = true;
					break;
				}
			}
			if(isDefined){
				node.getFather().sons.get(0).attribute.put("name", node.getFather().attribute.get("name"));
				node.getFather().sons.get(0).attribute.put("paramNum","0");
			}
			else {
				errorMessages.add(new String[]{String.valueOf(rowNumber),"函数"+node.getFather().attribute.get("name")+"未定义"});
				isFuncError = true;
			}
		}

		// 错误处理5：识别出结构体中不存在的成员变量
		// 产生式：Call -> . ID M_A44 M_E5
		else if(nonTerminal.equals("M_E5")){
			boolean isDefined = false;
			for(int i=0;i<symbolsTable.size();i++){
				if(symbolsTable.get(i).getName().equals(node.getFather().attribute.get("name"))){
					for(Identifier id : symbolsTable.get(i).members){
						if(id.getName().equals(getTokenValue(inputCachePointer-1))){
							isDefined = true;
							break;
						}
					}
					if(isDefined == true){
						break;
					}
				}
			}
			if(!isDefined){
				errorMessages.add(new String[]{String.valueOf(rowNumber),
					"结构体"+node.getFather().attribute.get("name")+"不存在成员变量"+getTokenValue(inputCachePointer-1)});
					isExpressionError = true;
			}
		}

		// 错误处理6：结构体/数组/函数返回值有错误时，终止生成赋值指令和中间变量指令，直至本次表达式解析完成。已生成的中间变量不删除。
		else if(nonTerminal.equals("M_E6")){
			isExpressionError = false;
		}

		// 错误处理7：运算符和运算分量之间的类型不匹配
		else if(nonTerminal.equals("M_E7")){
			if(!isExpressionError){
				String fatherType = node.getFather().attribute.get("type");
				String sonType    = node.getFather().sons.get(0).attribute.get("type");
				boolean isTypeMatch = fatherType.equals(sonType);
				if(!isTypeMatch){
					String expression = node.getFather().sons.get(0).attribute.get("value");
					if(expression.startsWith("return(")){
						String functionName = expression.substring(7, expression.length()-1);
						errorMessages.add(new String[]{String.valueOf(rowNumber),"函数“"+functionName+"”的返回值类型为："+sonType
																			+"和表达式要求的类型"+fatherType+"不匹配"});
					}
					else{
						errorMessages.add(new String[]{String.valueOf(rowNumber),"表达式“"+expression+"”的类型为"+sonType
																			+",和表达式要求的类型"+fatherType+"不匹配"});
					}
					isExpressionError = true;
				}
			}
		}

		// 错误处理8：辅助函数，向函数的参数列表中添加形参类型
		// 产生式：Parameters -> Type ID M_E8 Parameters'
		// 产生式：Parameters' -> , Type ID M_E8 Parameters'
		else if(nonTerminal.equals("M_E8")){
			String paramType = node.getFather().sons.get(0).attribute.get("type");
			String functionName = node.getFather().attribute.get("name");
			getFunction(functionName).paramTypes.add(paramType);
			node.getFather().sons.get(1).attribute.put("name",functionName);
		}

		// 错误处理9：过程调用时实参与形参类型不匹配
		else if(nonTerminal.equals("M_E9")){
			if(!isFuncError){
				String functionName = node.getFather().attribute.get("name");
				int paramIndex = Integer.valueOf(node.getFather().attribute.get("paramNum"));
				int paramNumber = Integer.valueOf(getFunction(functionName).paramTypes.size());
				if(paramIndex<paramNumber){
					String realParamType = node.getFather().sons.get(0).attribute.get("type");
					String formalParamType = getFunction(functionName).paramTypes.get(paramIndex);
					if(!realParamType.equals(formalParamType)){
						errorMessages.add(new String[]{String.valueOf(rowNumber),
							"参数"+node.getFather().sons.get(0).attribute.get("value")+
							"的类型为"+realParamType+",和函数"+functionName+"要求的参数类型"+formalParamType+"不匹配"});
						for(int i=0;i<paramIndex;i++){
							for(int j=instructions.size()-1;j>=0;j--){
								if(instructions.get(j)[1].startsWith("param")){
									instructions.remove(j);
									break;
								}
							}
						}
						isFuncError = true;
					}
				}
			}
		}

		// 错误处理10：过程调用时实参与形参数目不匹配
		else if(nonTerminal.equals("M_E10")){
			if(!isFuncError){
				String functionName = node.getFather().attribute.get("name");
				int realParamNumber = Integer.valueOf(node.getFather().attribute.get("paramNum"));
				//System.out.println(node.getFather().attribute.get("paramNum"));
				int formalParamNumber = Integer.valueOf(getFunction(functionName).paramTypes.size());
				if(realParamNumber != formalParamNumber){
					errorMessages.add(new String[]{String.valueOf(rowNumber),
						"调用"+functionName+"函数时传递了"+realParamNumber+"个参数，和声明中要求的参数个数"+formalParamNumber+"不匹配"});
					for(int i=0;i<realParamNumber;i++){
						for(int j=instructions.size()-1;j>=0;j--){
							if(instructions.get(j)[1].startsWith("param")){
								instructions.remove(j);
								break;
							}
						}
					}
					isFuncError = true;
				}
			}
		}
	}
	// -------------------------------------------------------------------------------
}