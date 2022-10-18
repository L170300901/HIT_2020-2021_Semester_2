package bank.system;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parsing {

	int rowNumber; 							// ��������﷨�������еĽ���Ӧ�ĳɷ��������ļ��е��к�
	List<String[]> stack; 					// PDA��ջ�ṹ��ջԪ�ظ�ʽΪ<Ԫ�أ��﷨���еĲ���>
	List<String[]> inputCache; 				// PDA�����뻺����,�ʷ������Ľ������ʽΪ<�ֱ��룬����ֵ���к�>��<�ֱ��룬�к�>
	List<String> parsingResult; 			// �﷨�����Ľ������ʽΪ�����ţ��кţ������ֱ��� ������ֵ���кţ�������������Ϊ��2*�ڵ����
	List<String[]> errorMessages; 			// �﷨�����Ľ������ʽΪ�����ţ��кţ������ֱ��� ������ֵ���кţ�������������Ϊ��2*�ڵ����
	Map<String, String> predictionTable; 	// Ԥ���������ʽΪ<����ʽ��-�������,����ʽ�Ҳ�>


	// ------------------------------Construct function------------------------------
	public Parsing(List<String[]> inputCache) {
		this.stack = new ArrayList<String[]>();
		this.rowNumber = 0;
		this.inputCache = inputCache;
		this.parsingResult = new ArrayList<String>();
		this.errorMessages = new ArrayList<String[]>();
		this.predictionTable = new HashMap<String, String>();
		raedPredictTable();
		System.out.println("�﷨����Ϊ��");
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
		System.out.println("�ʷ����������");
		for (String[] a : scanning.getToken_Parser_Input()) {
			for(String b : a){
				System.out.print(b+" ");
			}
			System.out.println();
		}
		Parsing test = new Parsing(scanning.getToken_Parser_Input());
		test.PDA();
		System.out.println("�﷨���������");
		for (String testResult : test.getResult()) {
			System.out.println(testResult);
		}
		System.out.println("������Ϣ��");
		for (String[] testResult : test.errorMessages) {
			System.out.println(testResult[0]+" "+testResult[1]);
		}
	}
	// -------------------------------------------------------------------------




	// ------------------------------assistant function------------------------------
	public void popPrint(int treeDepth, String[] args) {
		String s = "";
		// ��Ҫ�������������
		for (int i = 0; i < treeDepth; i++) {
			s += "  ";
		}
		// �����Ϊĳ�����ս��(����ʽ��),args��ʽΪ<����>
		if (args.length == 1) {
			s = s + args[0] + " (" + String.valueOf(rowNumber) + ")";
		}
		// �����Ϊû������ֵ�ķ���,args��ʽΪ<�ֱ��룬�к�>
		else if (args.length == 2) {
			rowNumber = Integer.valueOf(args[1]);
			s = s + args[0] + " (" + String.valueOf(rowNumber) + ")";
		}
		// �����Ϊ��������ֵ�ķ���,args��ʽΪ<�ֱ��룬����ֵ���к�>
		else if (args.length == 3) {
			rowNumber = Integer.valueOf(args[2]);
			s = s + args[0] + " :" + args[1] + " (" + String.valueOf(rowNumber) + ")";
		} else {
			System.out.println("ERROR!");
		}
		parsingResult.add(s);
	}

	// ���Ԥ��������еĲ���ʽ�Լ���Ӧ��select��,�洢��ʽΪ��<����ʽ��-��ǰ������ţ�����ʽ�Ҳ�>
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
	//ִ���﷨������PDA���ڷ��ų�ջʱ��ӡ���
	public void PDA() {
		// ��ʼ����ѹ��ջ
		stack.add(new String[] { "Program", "0" });
		while (stack.size() > 0 && inputCache.size() > 0) {
			System.out.println("ջ��Ԫ�أ�"+stack.get(stack.size() - 1)[0]+"  ��ǰ�����ź�:"+inputCache.get(0)[0]);
			// ���뻺������ջ���ս�����ʱ��ջ�����ų�ջ,����ָ��ָ����һ���������
			if (inputCache.get(0)[0].equals(stack.get(stack.size() - 1)[0])) {
				popPrint(Integer.valueOf(stack.get(stack.size() - 1)[1]), inputCache.get(0));
				inputCache.remove(0);
				stack.remove(stack.size() - 1);
				continue;
			}
			else {
				// ���ݵ�ǰջ���ַ��������ַ�����Ԥ��������в����Ƿ��ж�Ӧ����ʽ
				String productionRights;
				String productionLeft_Input = stack.get(stack.size() - 1)[0] + "-" + inputCache.get(0)[0];
				// �ܹ���Ԥ����������ҵ�ƥ��Ĳ���ʽ
				if ((productionRights = predictionTable.get(productionLeft_Input)) != null) {
					// ջ�����ս����ջ
					int treeDepth = Integer.valueOf(stack.get(stack.size() - 1)[1]);
					popPrint(treeDepth, new String[] { stack.get(stack.size() - 1)[0] });
					stack.remove(stack.size() - 1);
					//����ʽ�Ҳ�������ջ(������ʽ�Ҳ���ĩ�ķ������Ƚ�ջ)
					if (!productionRights.equals("$")) {
						String[] productionRight = productionRights.split(" ");
						for (int i = productionRight.length - 1; i > -1; i--) {
							stack.add(new String[] { productionRight[i], String.valueOf(treeDepth + 1) });
						}
					}
				}
				// ������Ԥ����������ҵ�ƥ��Ĳ���ʽ�Ļ�������
				else {
					errorMessages.add(new String[]{"Error at Line"+rowNumber,
								stack.get(stack.size() - 1)[0] + "�޷�ͨ���ķ�����ת����" + inputCache.get(0)[0]});
					inputCache.remove(0);
				}
			}
		}
	}
	// -------------------------------------------------------------------------------
}