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

	// ---------�﷨��������-------------
	int 				rowNumber; 			// ��������﷨�������еĽ���Ӧ�ĳɷ��������ļ��е��к�
	List<String> 		stack; 				// PDA��ջ�ṹ��ջԪ�ظ�ʽΪ<Ԫ��>
	List<String[]> 		inputCache; 		// PDA�����뻺����,�ʷ������Ľ������ʽΪ<�ֱ��룬����ֵ���к�>��<�ֱ��룬�к�>
	Map<String, String>	predictionTable; 	// Ԥ���������ʽΪ<����ʽ��-�������,����ʽ�Ҳ�>
	Grammar_handler 	grammar;			// ���ڲ���Ԥ�������ı���
	// ---------�����������-------------
	int 			 	addrOffset;			// Ϊ��ǰ���������ڴ�������ʼ��ַ(����������ڴ濪ʼλ�õ�ƫ����)
	int 			 	tempVariableNum;	// ָ���������õ����м�����ĸ���
	int              	boolVariableNum;	// ָ���������õ��Ĳ��������ĸ���
	int              	inputCachePointer;	// ��ǰ���뻺����������ָ��
	Stack<Node>      	node_stack; 		// �������ջ�����ڴ洢�ڵ���Ӧ���ԣ�������Ӧ��ע�ͷ�����
	List<String[]>     	instructions; 		// ��������ĵõ���ָ�����У���ʽΪ<��Ԫʽ���У�����ַָ��>
	List<Function>     	functions;			// ��������õ��ĺ������б�
	List<Identifier> 	symbolsTable;		// ��������õ��ķ��ű�
	List<String[]>   	errorMessages; 		// ������������Ĵ�����Ϣ����ʽΪ<�кţ�������Ϣ>
	// ---------���������-------------
	boolean	            isExpressionError;  // �����ж����Ƶ����ʽʱ�Ƿ��������ڱ��ʽ�Ƶ����ʱ�ָ�false
	boolean	            isFuncError;		// �����жϺ�������ʱ�Ƿ��������ں����������ʱ�ָ�false



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
		//�ʷ�����
		Scanning scanning = new Scanning(program);
		scanning.DFA();
		SemanticAnalyser test = new SemanticAnalyser(scanning.getToken_Parser_Input());
		//�﷨�Ƶ�����
		test.PDA();
		System.out.println("������������");
		System.out.println("code�У�"+test.instructions.size());
		for(String[] code : test.instructions){
			System.out.println(code[0]+"   "+code[1]);
		}
		System.out.println("id�У�"+test.symbolsTable.size());
		for(Identifier id : test.symbolsTable){
			System.out.println(id.getName()+" "+id.getLength()+" "+id.getOffset()+" "+id.getType()+" ");
		}
		System.out.println("�����У�");
		for (Function function : test.functions) {
			System.out.print(function.getReturnType()+" "+function.getName()+"( ");
			for(String type : function.paramTypes){
				System.out.print(type+" ");
			}
			System.out.println(")");
		}
		System.out.println("������Ϣ��");
		for (String[] testResult : test.errorMessages) {
			System.out.println(testResult[0]+" "+testResult[1]);
		}
	}
	// -------------------------------------------------------------------------




	// ------------------------------assistant function------------------------------
	// ���Ԥ��������еĲ���ʽ�Լ���Ӧ��select��,�洢��ʽΪ��<����ʽ��-��ǰ������ţ�����ʽ�Ҳ�>
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

	//�ڷ��ų�ջʱ�����к�
	public void rowNumberHandler(String[] args) {
		// ��ջ��Ϊû������ֵ�ķ���,args��ʽΪ<�ֱ��룬�к�>
		if (args.length == 2) {
			rowNumber = Integer.valueOf(args[1]);
		}
		// ��ջ��Ϊ��������ֵ�ķ���,args��ʽΪ<�ֱ��룬����ֵ���к�>
		else if (args.length == 3) {
			rowNumber = Integer.valueOf(args[2]);
		}
	}

	//�жϵ�ǰ�ķ��ս���Ƿ���һ�����嶯������
	public boolean isSemanticAction(String nonTerminal){
		if(nonTerminal.charAt(0) == 'M'){
			return true;
		}
		return false;
	}

	//�ж�������ķ�����Symbol�Ƿ�Ϊһ�����ս��
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

	//�������ƻ�ȡ��ǰ���ű��еı�ʶ��
	private Identifier getIdentifier(String name){
		for(int i=0;i<symbolsTable.size();i++){
			if(symbolsTable.get(i).getName().equals(name))
				return symbolsTable.get(i);
		}
		return null;
	}

	//����Ӧ���Ƶı�ʶ���Ƿ��ڵ�ǰ���ű���
	private boolean isDeclaredSymbol(String name){
		for(int i=0;i<symbolsTable.size();i++){
			if(symbolsTable.get(i).getName().equals(name))
				return true;
		}
		return false;
	}

	//����Ӧ���Ƶĺ����Ƿ��������������б���
	private boolean isDeclaredFunction(String FunctionName) {
		for(Function function : functions) {
			if(function.getName().equals(FunctionName)){
				return true;
			}
		}
		return false;
	}

	//�������ƻ�ȡ��ǰ�����������б��еĺ���
	private Function getFunction(String FunctionName){
		for(Function function : functions) {
			if(function.getName().equals(FunctionName)){
				return function;
			}
		}
		return null;
	}

	//��ȡ���뻺������posλ�õ�token������ֵ��null��ʾ��tokenû������ֵ
	private String getTokenValue(int pos){
		if(inputCache.get(pos).length == 3){
			return inputCache.get(pos)[1];
		}
		return null;
	}

	//��ȡ������������ķ��ű�
	public List<String[]> getSymbolTable(){
		ArrayList<String[]> symbols = new ArrayList<String[]>();
		for(Identifier id : symbolsTable){
			symbols.add(new String[]{id.getName(),id.getType(),String.valueOf(id.getLength()),String.valueOf(id.getOffset())});
		}
		return symbols;
	}

	//��ȡ�������������ָ������
	public List<String[]> getInstructions(){
		return new ArrayList<String[]>(instructions);
	}

	//��ȡ������������Ĵ�����Ϣ
	public List<String[]> getError(){
		return new ArrayList<String[]>(errorMessages);
	}
	// ------------------------------------------------------------------------------




	// --------------------------------------PDA--------------------------------------
	// ִ���﷨������PDA���ڹ����н����﷨�Ƶ�����
	// ���У��﷨����ջ�ڷ��������еĿ�ѹ�������ķ����ţ����������ջ��ѹ����ս��
	// ע�ͷ�������ֻ�����������嶯���ķ��ս��
	// PDAִ������{
	//		�����﷨����ջ���������ջ��������ʼ����ѹ�뵽����ջ��
	//		while(�﷨�Ƶ�����δ���){
	//			����ǰ�﷨����ջ���ڵ�Ϊ���ս�������������ջ��ջ��Ϊ���嶯����׼��
	//			if(��ǰ�﷨�����ڵ������뻺������������ƥ�䡾˵���ս��ƥ��ɹ���){
	//				�﷨����ջջ���ڵ��ջ������ָ�������ƶ�
	//			}
	//			else(��ǰ�﷨�����ڵ������뻺�����������ݲ�ƥ�䡾˵���Ƿ��ս�� �� �ս��ƥ��ʧ�ܡ�){
	//				����Ԥ�������ѡ���Ӧ�Ĳ���ʽ�Ե�ǰ���ս�������滻
	//				if(Ԥ���������ҳɹ�����ǰ���ս���ɱ��滻��){
	//					����ǰ���ս��Ϊ���嶯������ִ����Ӧ�����嶯��
	//					�﷨����ջ��ջ�����ų�ջ
	//					�ҵ��Ĳ���ʽ���Ҳ����з���������﷨����ջ�����з��ս��������������ջ
	//					ע�ͷ������������Ӧ�ĸ��ӽڵ��ϵ��ֻ��Ӳ������嶯���ķ��ս��
	// 					�������嶯���ķ��ս�� ���ᱻ��ӽ��κ�һ��node��son�б��У������嶯����node���Ծ���father��
	//				}
	//				else(Ԥ����������ʧ�ܡ���ǰ�ķ����Ų��ɱ��滻��){
	//					������
	//				}
	//			}
	//  	}
	// }
	public void PDA() {
		// ��ʼ����ѹ��ջ
		node_stack.push(new Node("Program",null));
		stack.add("Program");
		//��ʼ�����﷨�Ƶ����룬��(�﷨��������ջ��)��(���뻺����ָ��ָ���β)ʱ����ֹ������
		while (stack.size() > 0 && inputCachePointer<inputCache.size()) {
			//��ջ�������Ƿ��ս��ʱ�������﷨������Ӧ�Ľڵ�(����name�ֶ���ͬ)��Ϊ���嶯���Ĵ�����׼��
			Node node = null;
			try{
				if(isnonTerminal(stack.get(stack.size() - 1))){
					node=node_stack.pop();
				}
			}catch(Exception e){
				//����ջ�ѿգ�����ջ��Ȼ���ַ����ڣ�����ֹ��ǰ�﷨����������
				errorMessages.add(new String[]{String.valueOf(rowNumber),"���ڶ�����������ݣ��﷨��������ֹ"});
				break;
			}
			// ���뻺�������ս����ջ���ս�����ʱ��ջ������ƥ��ɹ�
			// ջ�����ų�ջ,����ָ��ָ����һ���������
			if (inputCache.get(inputCachePointer)[0].equals(stack.get(stack.size() - 1))) {
				rowNumberHandler(inputCache.get(inputCachePointer));
				stack.remove(stack.size() - 1);
				inputCachePointer++;
			}
			else {
				// ���ݵ�ǰջ���ַ��������ַ�����Ԥ��������в����Ƿ��ж�Ӧ����ʽ
				String productionRights;
				String productionLeft_Input = stack.get(stack.size() - 1) + "-" + inputCache.get(inputCachePointer)[0];
				// �ܹ���Ԥ����������ҵ�ƥ��Ĳ���ʽ
				if ((productionRights = predictionTable.get(productionLeft_Input)) != null) {
					// ջ�����ս����������嶯�������ڳ�ջǰִ����Ӧ����Ƭ��
					if(isSemanticAction(stack.get(stack.size() - 1))){
						semanticAction(stack.get(stack.size() - 1),node);
					}
					// ջ�����ս����ջ
					rowNumberHandler(new String[] { stack.get(stack.size() - 1) });
					stack.remove(stack.size() - 1);
					// ����ʽ�Ҳ�������ջ(������ʽ�Ҳ���ĩ�ķ������Ƚ�ջ)�����ա�����$����ջ
					// ͬʱ������ʽ�Ҳ����з��ս����������ӵ��������ջ�У�����ע�ͷ�����
					if (!productionRights.equals("$")) {
						String[] productionRight = productionRights.split(" ");
						for (int i = productionRight.length - 1; i > -1; i--) {
							stack.add(productionRight[i]);
							// �������ʽ�Ҳ������˷��ս������Ϊ�佨����������ڵ㲢�������ջ��(�������ջ�а����﷨����)
							// ͬʱ��Ӹýڵ���ע�ͷ������еĸ��ڵ���ӽڵ�(ע�ͷ������в����﷨����)
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
				// ������Ԥ����������ҵ�ƥ��Ĳ���ʽ�Ļ�������
				// ͨ����GUI���ȵ����﷨���������������б�ķ�ʽ���ɱ����﷨�Ƶ������г��ִ������
				else {
					/*errorMessages.add(new String[]{"Error at Line"+rowNumber,
								stack.get(stack.size() - 1) + "�޷�ͨ���ķ�����ת����" + inputCache.get(inputCachePointer)[0]});*/
					errorMessages.add(new String[]{""+rowNumber, "�﷨�����׶δ��ڴ�����ʹ���﷨�������м��"});
					inputCachePointer++;
				}
			}
		}
	}
	// -------------------------------------------------------------------------------




	// ---------------------------------------���嶯��---------------------------------
	// ��ִ��semanticAction����ʱ�����Բ���ʽconst -> INT M74_2Ϊ����
	// nodeΪ��Ӧ�����嶯����M74_2
	// node.getFather()Ϊ���嶯��M74_2��Ӧ�Ĳ���ʽ�󲿷��ս��const
	// ����˵����
	// gen(s):����ָ��s
	// newTemp():�����µ��м����
	// newBool():�����µĲ�������
	// backpatch(x, s):��ָ��������x��������ָ��s
	// removeSymbol(x):�ӷ��ű����Ե������Ƴ�x������
	// latesetSymbol(): ��ʾ���ű������һ������(�����������ɵķ���)
	// enter(name,type,offset,length):����ű��в�������ĸ����Ե��·���
	// lookup(X.lexeme)����ʾ�����뻺�����в��Ҵʷ��������õ���Token����X������ֵ
	public void semanticAction(String nonTerminal,Node node){

		// ����A1���ķ���
		// Define_Sentense -> Type ID M_A1 Array M_A2 Indentifiers ; M_E2
		// List_Member -> Type ID M_A1 Array ; M_A4 List_Member'
		// List_Member' -> Type ID M_A1 Array ; M_A5 List_Member'
		// A1���ݣ�
		// Array.name=lookup(ID.lexeme); Array.type=Type.type; Array.length=Type.length; Array.dimension=0;
		if(nonTerminal.equals("M_A1")){
			Node father=node.getFather();
			father.sons.get(1).attribute.put("name", getTokenValue(inputCachePointer-1));
			father.sons.get(1).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.sons.get(1).attribute.put("length", father.sons.get(0).attribute.get("length"));
			father.sons.get(1).attribute.put("dimension", "0");
		}

		// ����A2���ķ���
		// Define_Sentense -> Type ID M_A1 Array M_A2 Indentifiers ; M_E2
		// A2���ݣ�
		// Indentifiers.type=Type.type; Indentifiers.length=Type.length;
		else if(nonTerminal.equals("M_A2")){
			Node father=node.getFather();
			father.sons.get(2).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.sons.get(2).attribute.put("length", father.sons.get(0).attribute.get("length"));
		}

		// ����A3���ķ���
		// Define_Sentense -> struct ID M_A3 { List_Member } ; M_E2
		// A3���ݣ�
		// List_Member.name=lookup(ID.lexeme); List_Member.type="struct"
		else if(nonTerminal.equals("M_A3")){
			Node father=node.getFather();
			father.sons.get(0).attribute.put("name", getTokenValue(inputCachePointer-1));
			father.sons.get(0).attribute.put("type", "struct");
		}

		// ����A4���ķ���
		// List_Member -> Type ID M_A1 Array ; M_A4 List_Member'
		// A4���ݣ�
		// List_Member'.name=List_Member.name; List_Member'.type=struct"+"("+Type.type;
		// List_Member'.length=latesetSymbol().length; List_Member'.member_num=1;
		else if(nonTerminal.equals("M_A4")){
			Node father=node.getFather();
			father.sons.get(2).attribute.put("name", father.attribute.get("name"));
			father.sons.get(2).attribute.put("type", "struct"+"("+symbolsTable.get(symbolsTable.size()-1).getType());
			father.sons.get(2).attribute.put("length", String.valueOf(symbolsTable.get(symbolsTable.size()-1).getLength()));
			father.sons.get(2).attribute.put("member_num", String.valueOf(1));
		}

		// ����A5���ķ���
		// List_Member' -> Type ID M_A1 Array ; M_A5 List_Member'
		// A5���ݣ�
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

		// ����A6���ķ���
		// List_Member' -> M_A6
		// A6���ݣ�
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

		// ����A7���ķ���
		// Array -> M_A7
		// A7���ݣ�
		// ��Ϊ��������е�����,��ʹ�øò���ʽ˵������/����������ɣ�ֻ������ű������Ӹ�����/����
		// enter(array.name, array.type, offset, array.length); offset+=array.length;
		// ��Ϊִ������е�����
		// arrayά��Ϊ0   : array.value=array.name array.value=array.name
		// arrayά�Ȳ�Ϊ0 :
		// ƫ�Ƶ�ַ=arr1*w1+arr2*w2+��+arrk*wk;  array.value=newtemp(ti);
		// array.val=name[ƫ�Ƶ�ַ];  gen(ti := array.name+[ƫ�Ƶ�ַ]);
		else if(nonTerminal.equals("M_A7")){
			Node father=node.getFather();
			//��������е�����
			if(father.attribute.get("length")!=null){
				int length=Integer.parseInt(father.attribute.get("length"));
				Identifier id=new Identifier(father.attribute.get("name"),father.attribute.get("type"), addrOffset, length);
				addrOffset+=length;
				//��Ϊ�������ͣ�����id�����������ά���±�
				int dimension=Integer.parseInt(father.attribute.get("dimension"));
				for(int i=0;i<dimension;i++){
					id.arr_list.add(Integer.parseInt(father.attribute.get("arr"+i)));
				}
				symbolsTable.add(id);
			}
			//ִ������е�����
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
							errorMessages.add(new String[]{String.valueOf(rowNumber),"����"+name+"��ά�ȱ�����Ϊ"+
								id.arr_list.size()+"��������"+father.attribute.get("dimension")+"ά����ķ�ʽ����"});
							isExpressionError = true;
						}
					}else{
						father.attribute.put("value", name);
					}
				}
			}
		}

		// ����A8���ķ���
		// Array -> [ INT ] M_A8 Array M_A9
		// A8���ݣ�
		// Array1.name=Array.name;  Array1.type=Array.type;  Array1.length=Array.length*lookup(INT.lexeme);
		// Array1.dimension=Array.dimension+1;  Array1.arr(Array.dimension)=INT;  Array1.arr(i)=Array.arr(i);
		// arr(i)����Ϊ�����iά���±ꡣ(int a[3][4]��arr0=3��arr1=4)
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
			if(father.attribute.get("length")!=null){//��������е�����
				father.sons.get(0).attribute.put("length", Integer.parseInt(father.attribute.get("length")) * num + "");
			}
		}

		// ����A9���ķ���
		// Factor_Multi -> ID M_A40 Call M_A9
		// Factor_Multi -> Constant M_A9
		// Call -> M_A42 Array M_A9 M_E3
		// A9���ݣ�
		// �����ķ���Factor_Multi -> ID M_A40 Call M_A9
		// 	����Ϊ��Factor_Multi.value=call.value; 		Factor_Multi.val=call.val;;		Factor_Multi.type=call.type;
		// �����ķ���Factor_Multi -> Constant M_A9
		// 	����Ϊ��Factor_Multi.value=Constant.value; 	Factor_Multi.val=Constant.val;	Factor_Multi.type=Constant.type;
		// �����ķ���Call -> M_A42 Array M_A9 M_E3
		//	����Ϊ��Call.value=Array.value; 			Call.val=Array.val;				Call.type=Array.type;
		else if(nonTerminal.equals("M_A9")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.attribute.put("val", father.sons.get(0).attribute.get("val"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// ����A9_1���ķ���
		// Array -> [ INT ] M_A8 Array M_A9
		// A9_1���ݣ�
		// �����ķ���Array -> [ INT ] M_A8 Array M_A9
		//	����Ϊ��Array.value=Array1.value; 			Array.val=Array1.val;           Array.type=Array1.type+"[]";
		else if(nonTerminal.equals("M_A9_1")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.attribute.put("val", father.sons.get(0).attribute.get("val"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type")/*+"[]"*/ );
		}

		// ����A10���ķ���
		// Indentifiers -> , ID M_A10 Array M_A11 Indentifiers
		// A10���ݣ�
		// Array.type=Indentifiers.type;  Array.length=type.length;
		// Array.name=lookup(ID.lexeme);  Array.dimension=0;
		else if(nonTerminal.equals("M_A10")){
			Node father=node.getFather();
			father.sons.get(0).attribute.put("type", father.attribute.get("type"));
			father.sons.get(0).attribute.put("length", father.attribute.get("length"));
			father.sons.get(0).attribute.put("name", getTokenValue(inputCachePointer-1));
			father.sons.get(0).attribute.put("dimension", "0");
		}

		// ����A11���ķ���
		// Indentifiers -> , ID M_A10 Array M_A11 Indentifiers
		// A11���ݣ�
		// Indentifiers1.type=Array.type;  Indentifiers1.length=Array.length;
		else if(nonTerminal.equals("M_A11")){
			Node father=node.getFather();
			father.sons.get(1).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.sons.get(1).attribute.put("length", father.sons.get(0).attribute.get("length"));
		}

		// ����A12���ķ���
		// Sentense -> if ( Expression ) M_A12 Block M_A13 Else_Body M_A14
		// Sentense -> while ( M_A15 Expression ) M_A12 Block M_A16
		// A12���ݣ�
		// gen(if "+E.value+" goto "+(instructions.size()+2)); Sentense.backpatch=instructions.size(); gen(null);
		else if(nonTerminal.equals("M_A12")){
			Node father=node.getFather();
			String b=father.sons.get(0).attribute.get("value");
			instructions.add(new String[]{"(goto,"+b+",_,"+(instructions.size()+2)+")" , "if "+b+" goto "+(instructions.size()+2)});
			father.attribute.put("backpatch", ""+instructions.size());
			instructions.add(null);
		}

		// ����A13���ķ���
		// Sentense -> if ( Expression ) M_A12 Block M_A13 Else_Body M_A14
		// A13���ݣ�
		// gen(null);  backpatch(Sentense.backpatch, "goto "+instructions.size());  Sentense.backpatch=instructions.size()-1;
		else if(nonTerminal.equals("M_A13")){
			Node father=node.getFather();
			int backpatch=Integer.parseInt(father.attribute.get("backpatch"));
			instructions.add(null);
			instructions.set(backpatch, new String[]{"(goto,_,_,"+instructions.size()+")" , "goto "+instructions.size()});
			father.attribute.put("backpatch", ""+(instructions.size()-1));
		}

		// ����A14���ķ���
		// Sentense -> if ( Expression ) M_A12 Block M_A13 Else_Body M_A14
		// A14���ݣ�
		// backpatch(Sentense.backpatch, "goto "+instructions.size());
		else if(nonTerminal.equals("M_A14")){
			Node father=node.getFather();
			int backpatch=Integer.parseInt(father.attribute.get("backpatch"));
			instructions.set(backpatch, new String[]{"(goto,_,_,"+instructions.size()+")" , "goto "+instructions.size()});
		}

		// ����A15���ķ���
		// Sentense -> while ( M_A15 Expression ) M_A12 Block M_A16
		// A15���ݣ�
		// Sentense.backto=instructions.size();
		else if(nonTerminal.equals("M_A15")){
			Node father=node.getFather();
			father.attribute.put("backto", ""+instructions.size());
		}

		// ����A16���ķ���
		// Sentense -> while ( M_A15 Expression ) M_A12 Block M_A16
		// A16���ݣ�
		// gen(goto Sentense.backto);  backpatch(Sentense.backpatch, "goto "+instructions.size());
		else if(nonTerminal.equals("M_A16")){
			Node father=node.getFather();
			int backpatch=Integer.parseInt(father.attribute.get("backpatch"));
			int backto=Integer.parseInt(father.attribute.get("backto"));
			instructions.add(new String[]{"(goto,_,_,"+backto+")" , "goto "+backto});
			instructions.set(backpatch, new String[]{"(goto,_,_,"+instructions.size()+")" , "goto "+instructions.size()});//����
		}

		// ����A17���ķ���
		// Expression -> Value M_A17 Expression' M_A18
		// Value -> Add_Item M_A17 Add_Items M_A18
		// Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// A17���ݣ�
		// �����ķ���Expression -> Value M_A17 Expression' M_A18
		// 	����Ϊ�� Expression'.value=Value.value;  Expression'.val=Value.val; Expression'.type=Value.type;
		// �����ķ���Value -> Add_Item M_A17 Add_Items M_A18,
		// 	����Ϊ��Add_Items.value=Add_Item.value; Add_Items.val=Add_Item.val; Add_Items.type=Add_Item.type;
		// �����ķ���Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// 	����Ϊ��Factor_Multis.value=Factor_Multi.value; Factor_Multis.val=Factor_Multi.val; Factor_Multis.type=Factor_Multi.type;
		else if(nonTerminal.equals("M_A17")){
			Node father=node.getFather();
			father.sons.get(1).attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.sons.get(1).attribute.put("val", father.sons.get(0).attribute.get("val"));
			father.sons.get(1).attribute.put("type", father.sons.get(0).attribute.get("type"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// ����A18���ķ���
		// Expression -> Value M_A17 Expression' M_A18
		// Value -> Add_Item M_A17 Add_Items M_A18
		// Add_Items -> + Add_Item M_A31 Add_Items M_A18
		// Add_Items -> - Add_Item M_A32 Add_Items M_A18
		// Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// Factor_Multis -> * Factor_Multi M_A33 Factor_Multis M_A18
		// Factor_Multis -> / Factor_Multi M_A34 Factor_Multis M_A18
		// Factor_Multis -> % Factor_Multi M_A35 Factor_Multis M_A18
		// A18���ݣ�
		// �����ķ���Expression -> Value M_A17 Expression' M_A18
		// 	����Ϊ�� Expression.value=Expression'.value; Expression.val=Expression'.val;
		// �����ķ���Value -> Add_Item M_A17 Add_Items M_A18,
		// 	����Ϊ��Value.value=Add_Items.value; Value.val=Add_Items.val;
		// �����ķ���Add_Items -> + Add_Item M_A31 Add_Items M_A18 �� Add_Items -> - Add_Item M_A32 Add_Items M_A18
		// 	����Ϊ��Add_Items.value=Add_Items1.value; Add_Items.val=Add_Items1.val;
		// �����ķ���Add_Item -> Factor_Multi M_A17 Factor_Multis M_A18
		// 	����Ϊ��Add_Items.value=Factor_Multis.value; Add_Items.val=Factor_Multis.val;
		// �����ķ���Factor_Multis -> * Factor_Multi M_A33 Factor_Multis M_A18
		// 			Factor_Multis -> / Factor_Multi M_A34 Factor_Multis M_A18
		// 		    Factor_Multis -> % Factor_Multi M_A35 Factor_Multis M_A18
		// 	����Ϊ��Factor_Multis.value=Factor_Multis.value; Factor_Multis.val=Factor_Multis.val;
		else if(nonTerminal.equals("M_A18")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(1).attribute.get("value"));
			father.attribute.put("val", father.sons.get(1).attribute.get("val"));
		}

		// ����A19���ķ���
		// Expression' -> < Value M_A19
		// A19���ݣ�
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

		// ����A20���ķ���
		// Expression' -> <= Value M_A20
		// A20���ݣ�
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

		// ����A21���ķ���
		// Expression' -> > Value M_A21
		// A21���ݣ�
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

		// ����A22���ķ���
		// Expression' -> >= Value M_A22
		// A22���ݣ�
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

		// ����A23���ķ���
		// Expression' -> == Value M_A23
		// A23���ݣ�
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

		// ����A24���ķ���
		// Expression' -> != Value M_A24
		// A24���ݣ�
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

		// ����A25���ķ���
		// Expression' -> = Value M_A25
		// A25���ݣ�
		// Value.val��Ϊ��: remove(instruction.startsWith(Expression'.value)); gen(Expression'.val+" := "+Value.value);
		//					Expression'.value=Expression'.val;
		// Value.valΪ��  : gen(Expression'.value+" := "+Value.value);
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

		// ����A26���ķ���
		// Expression' -> += Value M_A26
		// A26���ݣ�
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

		// ����A27���ķ���
		// Expression' -> -= Value M_A27
		// A27���ݣ�
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

		// ����A28���ķ���
		// Expression' -> *= Value M_A28
		// A28���ݣ�
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

		// ����A29���ķ���
		// Expression' -> /= Value M_A29
		// A29���ݣ�
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

		// ����A30���ķ���
		// Expression' -> %= Value M_A30
		// A30���ݣ�
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

		// ����A31���ķ���
		// Add_Items -> + Add_Item M_A31 Add_Items M_A18
		// A31���ݣ�
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

		// ����A32���ķ���
		// Add_Items -> - Add_Item M_A32 Add_Items M_A18
		// A32���ݣ�
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

		// ����A33���ķ���
		// Factor_Multis -> * Factor_Multi M_A33 Factor_Multis M_A18
		// A33���ݣ�
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

		// ����A34���ķ���
		// Factor_Multis -> / Factor_Multi M_A34 Factor_Multis M_A18
		// A34���ݣ�
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

		// ����A35���ķ���
		// Factor_Multis -> % Factor_Multi M_A35 Factor_Multis M_A18
		// A35���ݣ�
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

		// ����A36���ķ���
		// Factor_Multi -> ! Factor_Multi M_A36
		// A36���ݣ�
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

		// ����A37���ķ���
		// Factor_Multi -> ++ Factor_Multi M_A37
		// A37���ݣ�
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

		// ����A38���ķ���
		// Factor_Multi -> -- Factor_Multi M_A38
		// A38���ݣ�
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

		// ����A39���ķ���
		// Factor_Multi -> ( Expression ) M_A39
		// A39���ݣ�
		// Factor_Multis.value=Expression.value; Factor_Multi.type=Expression.type;
		else if(nonTerminal.equals("M_A39")){
			Node father=node.getFather();
			father.attribute.put("value", father.sons.get(0).attribute.get("value"));
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// ����A40���ķ���
		// Factor_Multi -> ID M_A40 Call M_A9
		// A40���ݣ�
		// Call.name=lookup(ID.lexeme);;
		else if(nonTerminal.equals("M_A40")){
			Node father=node.getFather();
			father.sons.get(0).attribute.put("name", getTokenValue(inputCachePointer-1));
		}

		// ����A41���ķ���
		// Factor_Multi -> - Factor_Multi M_A41
		// A41���ݣ�
		// Factor_Multi.value=newTemp(ti); Factor_Multi.type=Factor_Multi1.type; gen(ti+" := 0 - "+Factor_Multi1.value);
		else if(nonTerminal.equals("M_A41")){
			Node father=node.getFather();
			String f1="t"+(tempVariableNum++);
			String f2=father.sons.get(0).attribute.get("value");
			instructions.add(new String[]{"(-,0,"+f2+","+f1+")" , f1+" := 0 - "+f2});
			father.attribute.put("value", f1);
			father.attribute.put("type", father.sons.get(0).attribute.get("type"));
		}

		// ����A42���ķ���
		// Call -> M_E3 M_A42 Array M_A9
		// A42���ݣ�
		// Array.name=Call.name; Array.dimension=0;
		else if(nonTerminal.equals("M_A42")){
			Node father=node.getFather();
			if(!isExpressionError){
				father.sons.get(0).attribute.put("type", getIdentifier(getTokenValue(inputCachePointer-1)).getType());
				father.sons.get(0).attribute.put("name", father.attribute.get("name"));
				father.sons.get(0).attribute.put("dimension", "0");
			}
		}

		// ����A43���ķ���
		// Call -> M_E4 ( Pass_Parameters ) M_A43
		// A43���ݣ�
		// Call.value=return(Call.name); Call.type=Function(Call.name).type;
		else if(nonTerminal.equals("M_A43")){
			//call -> ( Es ) M102_1
			Node father=node.getFather();
			if(!isExpressionError){
				father.attribute.put("value","return("+father.attribute.get("name")+")");
				father.attribute.put("type",getFunction(father.attribute.get("name")).getReturnType());
			}
		}

		// ����A44���ķ���
		// Call -> . ID M_A44 M_E5
		// A44���ݣ�
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

		// ����A45���ķ���
		// Pass_Parameters -> Expression M_A45 Pass_Parameters' M_A46
		// Pass_Parameters' -> , Expression M_A45 Pass_Parameters' M_A46
		// A45���ݣ�
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

		// ����A46���ķ���
		// Pass_Parameters -> Expression M_A45 Pass_Parameters' M_A46
		// Pass_Parameters' -> , Expression M_A45 Pass_Parameters' M_A46
		// A46���ݣ�
		// Pass_Parameters.paramNum=Pass_Parameters'.paramNum;
		else if(nonTerminal.equals("M_A46")){
			//�ı����ɵ��ú�����ָ���A46����ʹ��
		}

		// ����A47���ķ���
		// Pass_Parameters -> M_A47
		// Pass_Parameters' -> M_A47
		// A47���ݣ�
		// �����ķ���Pass_Parameters -> M_A47  ����Ϊ�� gen("call"+Call.name+","+Pass_Parameters.paramNum);
		// �����ķ���Pass_Parameters' -> M_A47 ����Ϊ�� gen("call"+Call.name+","+Pass_Parameters'.paramNum);
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

		// ����A48���ķ���
		// Type -> char M_A48
		// A48���ݣ�
		// Type.type = char; Type.length = 1;
		else if(nonTerminal.equals("M_A48")){
			node.getFather().attribute.put("type", "char");
			node.getFather().attribute.put("length", "1");
		}

		// ����A49���ķ���
		// Type -> int M_A49
		// A49���ݣ�
		// Type.type = int; Type.length = 4;
		else if(nonTerminal.equals("M_A49")){
			node.getFather().attribute.put("type", "int");
			node.getFather().attribute.put("length", "4");
		}

		// ����A50���ķ���
		// Type -> long M_A50
		// A50���ݣ�
		// Type.type = long; Type.length = 4;
		else if(nonTerminal.equals("M_A50")){
			node.getFather().attribute.put("type", "long");
			node.getFather().attribute.put("length", "4");
		}
		// ����A51���ķ���
		// Type -> short M_A51
		// A51���ݣ�
		// Type.type = short; Type.length = 2;
		else if(nonTerminal.equals("M_A51")){
			node.getFather().attribute.put("type", "short");
			node.getFather().attribute.put("length", "2");
		}

		// ����A52���ķ���
		// Type -> float M_A52
		// A52���ݣ�
		// Type.type = float; Type.length = 4;
		else if(nonTerminal.equals("M_A52")){
			node.getFather().attribute.put("type", "float");
			node.getFather().attribute.put("length", "4");
		}

		// ����A53���ķ���
		// Type -> double M_A53
		// A53���ݣ�
		// Type.type = char; Type.length = 8;
		else if(nonTerminal.equals("M_A53")){
			node.getFather().attribute.put("type", "double");
			node.getFather().attribute.put("length", "8");
		}

		// ����A54���ķ���
		// Constant -> INT M_A54
		// Constant -> FLOAT M_A54
		// Constant -> DOUBLE M_A54
		// Constant -> CHAR M_A54
		// A54���ݣ�
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

		// ����A55���ķ���
		// Sentense -> return Expression M_A55 ;
		// A55���ݣ�
		// gen(return Expression.value)
		else if(nonTerminal.equals("M_A55")){
			Node father=node.getFather();
			instructions.add(new String[]{"(return,_,_,"+father.sons.get(0).attribute.get("value")+")"
				, "return "+father.sons.get(0).attribute.get("value")});
		}

		// ������1���жϺ����ظ�����
		else if(nonTerminal.equals("M_E1")){
			if(isDeclaredFunction(getTokenValue(inputCachePointer-1))){
				errorMessages.add(new String[]{String.valueOf(rowNumber),"����"+getTokenValue(inputCachePointer-1)+"��������"});
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

		// ������2���жϱ����ظ�����
		else if(nonTerminal.equals("M_E2")){
			for(int i=0;i<symbolsTable.size()-1;i++){
				if(symbolsTable.get(i).getName().equals(symbolsTable.get(symbolsTable.size()-1).getName())){
					errorMessages.add(new String[]{String.valueOf(rowNumber),
						"����"+symbolsTable.get(symbolsTable.size()-1).getName()+"�������������Ա��ε��ظ�����"});
					symbolsTable.remove(symbolsTable.size()-1);
					break;
				}
			}
		}

		// ������3��ʶ���δ����ı�����
		else if(nonTerminal.equals("M_E3")){
			boolean isDefined = false;
			for(int i=0;i<symbolsTable.size();i++){
				if(symbolsTable.get(i).getName().equals(node.getFather().attribute.get("name"))){
					isDefined = true;
					break;
				}
			}
			if(!isDefined){
				errorMessages.add(new String[]{String.valueOf(rowNumber),"����"+node.getFather().attribute.get("name")+"δ����"});
				isExpressionError = true;
			}
		}

		// ������4��ʶ���δ����ĺ�����,��ɾ���Ѳ�����paramָ���callָ��
		// ����ʽ��Call -> M_E4 ( Pass_Parameters ) M_A43
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
				errorMessages.add(new String[]{String.valueOf(rowNumber),"����"+node.getFather().attribute.get("name")+"δ����"});
				isFuncError = true;
			}
		}

		// ������5��ʶ����ṹ���в����ڵĳ�Ա����
		// ����ʽ��Call -> . ID M_A44 M_E5
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
					"�ṹ��"+node.getFather().attribute.get("name")+"�����ڳ�Ա����"+getTokenValue(inputCachePointer-1)});
					isExpressionError = true;
			}
		}

		// ������6���ṹ��/����/��������ֵ�д���ʱ����ֹ���ɸ�ֵָ����м����ָ�ֱ�����α��ʽ������ɡ������ɵ��м������ɾ����
		else if(nonTerminal.equals("M_E6")){
			isExpressionError = false;
		}

		// ������7����������������֮������Ͳ�ƥ��
		else if(nonTerminal.equals("M_E7")){
			if(!isExpressionError){
				String fatherType = node.getFather().attribute.get("type");
				String sonType    = node.getFather().sons.get(0).attribute.get("type");
				boolean isTypeMatch = fatherType.equals(sonType);
				if(!isTypeMatch){
					String expression = node.getFather().sons.get(0).attribute.get("value");
					if(expression.startsWith("return(")){
						String functionName = expression.substring(7, expression.length()-1);
						errorMessages.add(new String[]{String.valueOf(rowNumber),"������"+functionName+"���ķ���ֵ����Ϊ��"+sonType
																			+"�ͱ��ʽҪ�������"+fatherType+"��ƥ��"});
					}
					else{
						errorMessages.add(new String[]{String.valueOf(rowNumber),"���ʽ��"+expression+"��������Ϊ"+sonType
																			+",�ͱ��ʽҪ�������"+fatherType+"��ƥ��"});
					}
					isExpressionError = true;
				}
			}
		}

		// ������8�����������������Ĳ����б�������β�����
		// ����ʽ��Parameters -> Type ID M_E8 Parameters'
		// ����ʽ��Parameters' -> , Type ID M_E8 Parameters'
		else if(nonTerminal.equals("M_E8")){
			String paramType = node.getFather().sons.get(0).attribute.get("type");
			String functionName = node.getFather().attribute.get("name");
			getFunction(functionName).paramTypes.add(paramType);
			node.getFather().sons.get(1).attribute.put("name",functionName);
		}

		// ������9�����̵���ʱʵ�����β����Ͳ�ƥ��
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
							"����"+node.getFather().sons.get(0).attribute.get("value")+
							"������Ϊ"+realParamType+",�ͺ���"+functionName+"Ҫ��Ĳ�������"+formalParamType+"��ƥ��"});
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

		// ������10�����̵���ʱʵ�����β���Ŀ��ƥ��
		else if(nonTerminal.equals("M_E10")){
			if(!isFuncError){
				String functionName = node.getFather().attribute.get("name");
				int realParamNumber = Integer.valueOf(node.getFather().attribute.get("paramNum"));
				//System.out.println(node.getFather().attribute.get("paramNum"));
				int formalParamNumber = Integer.valueOf(getFunction(functionName).paramTypes.size());
				if(realParamNumber != formalParamNumber){
					errorMessages.add(new String[]{String.valueOf(rowNumber),
						"����"+functionName+"����ʱ������"+realParamNumber+"����������������Ҫ��Ĳ�������"+formalParamNumber+"��ƥ��"});
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