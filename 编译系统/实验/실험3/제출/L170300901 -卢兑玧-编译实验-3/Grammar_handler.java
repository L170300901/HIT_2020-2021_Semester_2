package bank.system;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Grammar_handler {

	ArrayList<String> terminals 	  = new ArrayList<String>();								//�ķ����ս������
	ArrayList<String> nonterminals 	  = new ArrayList<String>();								//�ķ��ķ��ս������
	ArrayList<Production> productions = new ArrayList<Production>();							//�ķ��Ĳ���ʽ����
	HashMap<String, ArrayList<String>> firstSets = new HashMap<String, ArrayList<String>>();	//FIRST���ļ��� ��ʽ:<�ķ����ţ�FIRST��>
	HashMap<String, ArrayList<String>> followSets = new HashMap<String, ArrayList<String>>();	//FOLLOW���ļ��� ��ʽ:<���ս����FOLLOW��>


	//------------------------------Construct function------------------------------
	public Grammar_handler(){
		readGrammarFromFile();
		nonTerminalSet();
		TerminalSet();
		getFirstSets();
		getFollowSets();
		selectSet();
		getPredictionTable();
	}
	public Grammar_handler(String[] ss){
		readGrammarFromString(ss);
		nonTerminalSet();
		TerminalSet();
		getFirstSets();
		getFollowSets();
		selectSet();
		getPredictionTable();
	}
	//------------------------------------------------------------------------------




	//------------------------------test function------------------------------
	public static void main(String[] args) {
		Grammar_handler a = new Grammar_handler();
		System.out.println("first��Ϊ��");
		for(String nonterminal : a.nonterminals){
			System.out.println(nonterminal+" "+a.firstSets.get(nonterminal));
		}
		System.out.println("follow��Ϊ��");
		for(String nonterminal : a.nonterminals){
			System.out.println(nonterminal+" "+a.followSets.get(nonterminal));
		}
		for (Production production : a.productions) {
			System.out.print("����ʽ"+production.getLeft()+" ->");
			for(String productionRight: production.getRights()){
				System.out.print(" "+productionRight);
			}
			System.out.println("��SELECT��Ϊ��");
			for(String selectSetElement: production.selectSet){
				System.out.print(selectSetElement+" ");
			}
			System.out.println();
		}
	}
	//-------------------------------------------------------------------------




	//------------------------------assistant function------------------------------
	// �жϷ����Ƿ�ɿգ�������ڲ���ʽA->$,����ڲ���ʽA->�£����з��ž��ɿգ���֤������A�ɿ�
	public boolean isCanBeNull(String symbol) {
		for (Production production : productions) {
			if (production.getLeft().equals(symbol)) {
				//����ʽ�Ҳ�Ϊ$���ض��ɿ�
				if (production.getRights()[0].equals("$")) {
					return true;
				}
				//����ʽ�Ҳ����ս�����������ʽ�ض����ɿ�
				boolean containTerminal = false;
				for(String productionRight : production.getRights()){
					if(terminals.contains(productionRight)){
						containTerminal = true;
					}
				}
				if(containTerminal){
					continue;
				}
				//�����ս��ʱ���ż���Ƿ����з��ս����Ϊ��
				//����һ�����ս�����ɿ�����������ʽ���ɿգ����з��ս�������ɿղ���ʽ�ſɿ�
				boolean isAllNonTerminalNull = true;
				for(String productionRight : production.getRights()){
					if(!isCanBeNull(productionRight)){
						isAllNonTerminalNull = false;
						break;
					}
				}
				if(isAllNonTerminalNull == true){
					return true;
				}
			}
		}
		return false;
	}

	// ���ļ��ж�ȡ����ʽ
	public void readGrammarFromFile() {
		try {
			BufferedReader bufReader = new BufferedReader(new FileReader(new File("E://2020fall/NOHTAEYUN/Bank/src/bank/system/grammar.txt")));
			String production;
			while ((production = bufReader.readLine()) != null) {
				productions.add(new Production(production.split(" -> ")[0], production.split(" -> ")[1].split(" ")));
			}
			bufReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void readGrammarFromString(String[] ss) {
		try {
			for (String  production:ss) {
				productions.add(new Production(production.split(" -> ")[0], production.split(" -> ")[1].split(" ")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// ��÷��ս����:
	// ���ڷ��ս��һ���ᱻת��Ϊ�ս������˱ض�����ĳ������ʽ���󲿳���,���ֻ�轫���ս���󲿵ļ���ȫ���Ž�set���ɵõ����ս����
	public void nonTerminalSet() {
		for (Production line : productions) {
			if (nonterminals.contains(line.getLeft())) {
				continue;
			} else {
				nonterminals.add(line.getLeft());
			}
		}
	}

	// ����ս����:
	// �������еĲ���ʽ,�����ڲ���ʽ�г��ֵģ����Ƿ��ս�����������Ŷ����ս��
	public void TerminalSet() {
		for (Production production : productions) {
			for (String rightSymbol : production.getRights()) {
				if (nonterminals.contains(rightSymbol) || rightSymbol.equals("$") || terminals.contains(rightSymbol)) {
					continue;
				} else {
					terminals.add(rightSymbol);
				}
			}
		}
	}
	//------------------------------------------------------------------------------




	//--------------------------------------����SELECT��--------------------------------------
	// ����FIRST�����㷨��
    // �ս����FIRST�����㣺���������ս���������ս��X������FIRST������{X}
    // ���ս����FIRST�����㣺��������(��������ѭ���Ĳ��)
    // ������÷��ս����FIRST��,���������ظ���������ʽ���ظ�����FIRST��,���з��ս����FIRST���������仯��ֹͣ����
    // �������в���ʽ����ÿһ������ʽ���д���
	//		������ʽ�Ҳ���һ�����ŵ�FIRST���������ʽ�󲿷��ս����FIRST����
	//		����һ�����ſɿգ��򽫵ڶ�������������ͬ�������Դ�����
	//		����һ�����Ų��ɿգ���ֱ�ӽ����ò���ʽ��FIRST���ļ��㣬��ʼ������һ������ʽ��FIRST��
	//		����������ʽ�Ҳ��ɿգ��򽫦����뵽����ʽ�󲿷��ŵ�FIRST����
	//
	// α���룺
	// �ս����FIRST(X)={X};
	// ���ս����
	// for each X: FIRST(X)=null;
	// while(����FISRT�������仯){
	//		for(ÿ������ʽ){
	//			for(����ʽ�Ҳ��ÿ������ Y){
	//				FISRT(X).add(FIRST(Y));
	//				if(Y���ɿ�)	break;
	//			}
	//		}
	// }
	public void getFirstSets() {
		// �ս��X��FIRST������{X}
		for (String terminal : terminals) {
			ArrayList<String> firstSet = new ArrayList<String>();
			firstSet.add(terminal);
			firstSets.put(terminal, firstSet);
		}
		// ��ʼ���з��ս����FIRST��ȫΪ��
		for (String nonterminal : nonterminals) {
			firstSets.put(nonterminal, new ArrayList<String>());
		}
		// ��ʼ���е���������isFirstSetsChanged==false����û��FIRST�������仯ʱ�˳�����
		for (boolean isFirstSetsChanged = true; isFirstSetsChanged;) {
			isFirstSetsChanged = false;
			for (Production production : productions) {
				String productionLeft = production.getLeft();
				for (String productionRight : production.getRights()) {
					// $��û��FIRST��
					if (!productionRight.equals("$")) {
						for (String firstSetElement : firstSets.get(productionRight)) {
							if (!firstSets.get(productionLeft).contains(firstSetElement)) {
								firstSets.get(productionLeft).add(firstSetElement);
								isFirstSetsChanged = true;
							}
						}
					}
					// ���ڷ��Ų��ɿ���ֱ�ӽ����ò���ʽ��FIRST������
					if (!isCanBeNull(productionRight)) {
						break;
					}
				}
			}
		}
	}


	// ����FOLLOW�����㷨��
	// ���ս����FOLLOW�����㣺��������(��������ѭ���Ĳ��)(���з��ս����FOLLOW��)
	// ������÷��ս����FOLLOW��,���������ظ���������ʽ���ظ�����FOLLOW��,���з��ս����FOLLOW���������仯��ֹͣ����
    // �������в���ʽ����ÿһ������ʽ���д���
	//	  	��������ʽ�Ҳ�ÿһ������
	//			����ǰ���ŵĺ�һ�����ķ����ŵ�FIRST�����뵽��ǰ���ŵ�FOLLOW���У����жϵ�ǰ���ŵĺ�һ�����ķ������Ƿ�ɿ�
	//			����ǰ�ķ����Ų��ɿգ��������ǰ���ŵ�FOLLOW���ļ���
	//			���ɿգ����ٺ�һ�����ŵ�FIRST��Ҳ���뵽��ǰ���ŵ�FOLLOW���У����ж��Ƿ�ɿգ����ظ�����������
	//			����ǰ�����ڸò���ʽ�ĵĺ���ִ��ɿգ��򽫲���ʽ�󲿷��ŵ�FOLLOW�����뵽��ǰ���ŵ�FOLLOW����
	//
	//	α���룺
	//	for each X:	FOLLOW(X)=null;
	//	FOLLOW(S).add(#);
	//	while(����FOLLOW�������仯){
	//		for(ÿ������ʽ X->��){
	//			for(����ʽ�Ҳ��ÿ������ Y){
	//				for(Y֮���ÿ������ Z){
	//					FOLLOW(Y).add(FIRST(Z));
	//					if(Z���ɿ�)	break;
	//				}
	//				if(����Y֮����Ӵ��ɿ�)
	//					FOLLOW(Y).add(FOLLOW(X));
	//			}
	//		}
	//	}
	public void getFollowSets() {
		// ���з��ս����follow����ʼ��Ϊ��
		for (String nonterminal : nonterminals) {
			followSets.put(nonterminal, new ArrayList<String>());
		}
		// ��#���뵽follow(S)��
		followSets.get("Program").add("#");
		// ��ʼ��������û��FOLLOW�������仯(isFollowSetsChanged == false)ʱ��������
		for (boolean isFollowSetsChanged = true; isFollowSetsChanged;) {
			isFollowSetsChanged = false;
			for (Production production : productions) {
				String productionLeft = production.getLeft();
				String[] productionRights = production.getRights();
				// ������Ҫ�ж�ǰ���ͺ�̽ڵ㣬��˲�ʹ��foreach�﷨�������������б���
				for (int i = 0; i < productionRights.length; i++) {
					String productionRight = productionRights[i];
					// ���з��ս����FOLLOW��
					if (nonterminals.contains(productionRight)) {
						boolean isSubstringCanBeNull = true;
						for (int j = i + 1; j < productionRights.length; j++) {
							for (String firstSetElement : firstSets.get(productionRights[j])) {
								if (!followSets.get(productionRight).contains(firstSetElement)) {
									followSets.get(productionRight).add(firstSetElement);
									isFollowSetsChanged = true;
								}
							}
							// ������ǰ���ڴ���ķ��ս�����������ִ����ɿ�
							if (!isCanBeNull(productionRights[j])) {
								isSubstringCanBeNull = false;
								break;
							}
						}
						// ����ǰ���ڴ���ķ��ս�����������ִ��ɿգ��򽫲���ʽ�����ս����FOLLOW�����뵽��ǰ���ڴ���ķ��ս����FOLLOW����
						if (isSubstringCanBeNull) {
							for (String followSetElement : followSets.get(productionLeft)) {
								if (!followSets.get(productionRight).contains(followSetElement)) {
									followSets.get(productionRight).add(followSetElement);
									isFollowSetsChanged = true;
								}
							}
						}
					}
				}
			}
		}
		// ���follow���е�#(����Ҫremove����˲���ʹ��foreach)
		for (String productionLeft : nonterminals) {
			for (int i = 0; i < followSets.get(productionLeft).size(); i++) {
				if (followSets.get(productionLeft).get(i).equals("#"))
					followSets.get(productionLeft).remove(i);
			}
		}
	}



	// SELECT���㷨��
	// SELECT(A->��)=FIRST(��) [�����ɿ�]
	// SELECT(A->��)={FIRST(��)-��} U FOLLOW(A) [���ɿ�]
	public void selectSet() {
		// ����ÿ������ʽ������SELECT��
		for (Production production : productions) {
			String productionLeft = production.getLeft();
			String[] productionRights = production.getRights();
			// �ķ���ֹ���ŵ�SELECT��:SELECT(A->$)=FOLLOW(A)
			if (productionRights[0].equals("$")) {
				for (String followSetElement : followSets.get(productionLeft)) {
					if (!production.selectSet.contains(followSetElement)) {
						production.selectSet.add(followSetElement);
					}
				}
			} else {
				boolean isProductionCanBeNull = true;
				// ��FIRST(��)��ӽ�SELECT(A->��)
				for (String productionRight : productionRights) {
					for (String firstSetElement : firstSets.get(productionRight)) {
						if (!production.selectSet.contains(firstSetElement)) {
							production.selectSet.add(firstSetElement);
						}
					}
					// �жϦ��Ƿ�ɿգ����д���һ�����Ų��ɿ�������ɿգ�Ӧ��ֹFIRST(��)�ļ���
					if (!isCanBeNull(productionRight)) {
						isProductionCanBeNull = false;
						break;
					}
				}
				// �����ɿգ���FOLLOW(A)��ӽ�SELECT(A->��)
				if (isProductionCanBeNull) {
					for (String followTerminal : followSets.get(productionLeft)) {
						if (!production.selectSet.contains(followTerminal)) {
							production.selectSet.add(followTerminal);
						}
					}
				}
			}
		}
	}
	//---------------------------------------------------------------------------------------




	//--------------------------------------����Ԥ�������--------------------------------------
	// ����ÿ������ʽ��SELECT������Ԥ������������prediction_table.txt
	// ��ʽ��ջ������#������� -> ����ʽ�Ҳ�(����ʽ�Ҳ����ż��Ե��ո�ָ�)
	public void getPredictionTable() {
		try {
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File("E://2020fall/NOHTAEYUN/Bank/src/bank/system/prediction_table.txt")));
			for (Production production : productions) {
				for (String selectSetElement : production.selectSet) {
					String line = production.getLeft() + "#" + selectSetElement + " ->";
					for (String productionRight : production.getRights()) {
						line = line + " " + productionRight;
					}
					line = line + "\n";
					bufWriter.write(line);
				}
			}
			bufWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//----------------------------------------------------------------------------------------
}