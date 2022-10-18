package bank.system;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Grammar_handler {

	ArrayList<String> terminals 	  = new ArrayList<String>();								//文法的终结符集合
	ArrayList<String> nonterminals 	  = new ArrayList<String>();								//文法的非终结符集合
	ArrayList<Production> productions = new ArrayList<Production>();							//文法的产生式集合
	HashMap<String, ArrayList<String>> firstSets = new HashMap<String, ArrayList<String>>();	//FIRST集的集合 格式:<文法符号，FIRST集>
	HashMap<String, ArrayList<String>> followSets = new HashMap<String, ArrayList<String>>();	//FOLLOW集的集合 格式:<非终结符，FOLLOW集>


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
		System.out.println("first集为：");
		for(String nonterminal : a.nonterminals){
			System.out.println(nonterminal+" "+a.firstSets.get(nonterminal));
		}
		System.out.println("follow集为：");
		for(String nonterminal : a.nonterminals){
			System.out.println(nonterminal+" "+a.followSets.get(nonterminal));
		}
		for (Production production : a.productions) {
			System.out.print("产生式"+production.getLeft()+" ->");
			for(String productionRight: production.getRights()){
				System.out.print(" "+productionRight);
			}
			System.out.println("的SELECT集为：");
			for(String selectSetElement: production.selectSet){
				System.out.print(selectSetElement+" ");
			}
			System.out.println();
		}
	}
	//-------------------------------------------------------------------------




	//------------------------------assistant function------------------------------
	// 判断符号是否可空：如果存在产生式A->$,或存在产生式A->β，β中符号均可空，则证明符号A可空
	public boolean isCanBeNull(String symbol) {
		for (Production production : productions) {
			if (production.getLeft().equals(symbol)) {
				//产生式右部为$，必定可空
				if (production.getRights()[0].equals("$")) {
					return true;
				}
				//产生式右部有终结符，这个产生式必定不可空
				boolean containTerminal = false;
				for(String productionRight : production.getRights()){
					if(terminals.contains(productionRight)){
						containTerminal = true;
					}
				}
				if(containTerminal){
					continue;
				}
				//不含终结符时，才检查是否所有非终结符均为空
				//存在一个非终结符不可空则整个产生式不可空，所有非终结符均不可空产生式才可空
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

	// 从文件中读取产生式
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
	// 获得非终结符集:
	// 由于非终结符一定会被转换为终结符，因此必定会在某个产生式的左部出现,因此只需将非终结符左部的集合全部放进set即可得到非终结符集
	public void nonTerminalSet() {
		for (Production line : productions) {
			if (nonterminals.contains(line.getLeft())) {
				continue;
			} else {
				nonterminals.add(line.getLeft());
			}
		}
	}

	// 获得终结符集:
	// 遍历所有的产生式,所有在产生式中出现的，不是非终结符的其他符号都是终结符
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




	//--------------------------------------计算SELECT集--------------------------------------
	// 计算FIRST集的算法：
    // 终结符的FIRST集计算：遍历所有终结符，对于终结符X，它的FIRST集就是{X}
    // 非终结符的FIRST集计算：迭代计算(缩进代表循环的层次)
    // 迭代求得非终结符的FIRST集,即：不断重复遍历产生式并重复计算FIRST集,所有非终结符的FIRST集不发生变化则停止迭代
    // 遍历所有产生式，对每一个产生式进行处理
	//		将产生式右部第一个符号的FIRST集加入产生式左部非终结符的FIRST集中
	//		若第一个符号可空，则将第二个符号做上述同样处理，以此类推
	//		若第一个符号不可空，则直接结束该产生式的FIRST集的计算，开始计算下一个产生式的FIRST集
	//		若整个产生式右部可空，则将Σ加入到产生式左部符号的FIRST集中
	//
	// 伪代码：
	// 终结符：FIRST(X)={X};
	// 非终结符：
	// for each X: FIRST(X)=null;
	// while(存在FISRT集发生变化){
	//		for(每个产生式){
	//			for(产生式右侧的每个符号 Y){
	//				FISRT(X).add(FIRST(Y));
	//				if(Y不可空)	break;
	//			}
	//		}
	// }
	public void getFirstSets() {
		// 终结符X的FIRST集就是{X}
		for (String terminal : terminals) {
			ArrayList<String> firstSet = new ArrayList<String>();
			firstSet.add(terminal);
			firstSets.put(terminal, firstSet);
		}
		// 初始所有非终结符的FIRST集全为空
		for (String nonterminal : nonterminals) {
			firstSets.put(nonterminal, new ArrayList<String>());
		}
		// 开始进行迭代，仅当isFirstSetsChanged==false，即没有FIRST集发生变化时退出迭代
		for (boolean isFirstSetsChanged = true; isFirstSetsChanged;) {
			isFirstSetsChanged = false;
			for (Production production : productions) {
				String productionLeft = production.getLeft();
				for (String productionRight : production.getRights()) {
					// $符没有FIRST集
					if (!productionRight.equals("$")) {
						for (String firstSetElement : firstSets.get(productionRight)) {
							if (!firstSets.get(productionLeft).contains(firstSetElement)) {
								firstSets.get(productionLeft).add(firstSetElement);
								isFirstSetsChanged = true;
							}
						}
					}
					// 存在符号不可空则直接结束该产生式的FIRST集计算
					if (!isCanBeNull(productionRight)) {
						break;
					}
				}
			}
		}
	}


	// 计算FOLLOW集的算法：
	// 非终结符的FOLLOW集计算：迭代计算(缩进代表循环的层次)(仅有非终结符有FOLLOW集)
	// 迭代求得非终结符的FOLLOW集,即：不断重复遍历产生式并重复计算FOLLOW集,所有非终结符的FOLLOW集不发生变化则停止迭代
    // 遍历所有产生式，对每一个产生式进行处理
	//	  	遍历产生式右部每一个符号
	//			将当前符号的后一个的文法符号的FIRST集加入到当前符号的FOLLOW集中，并判断当前符号的后一个的文法符号是否可空
	//			若当前文法符号不可空，则结束当前符号的FOLLOW集的计算
	//			若可空，则将再后一个符号的FIRST集也加入到当前符号的FOLLOW集中，并判断是否可空（即重复上述操作）
	//			若当前符号在该产生式的的后继字串可空，则将产生式左部符号的FOLLOW集加入到当前符号的FOLLOW集中
	//
	//	伪代码：
	//	for each X:	FOLLOW(X)=null;
	//	FOLLOW(S).add(#);
	//	while(存在FOLLOW集发生变化){
	//		for(每个产生式 X->α){
	//			for(产生式右侧的每个符号 Y){
	//				for(Y之后的每个符号 Z){
	//					FOLLOW(Y).add(FIRST(Z));
	//					if(Z不可空)	break;
	//				}
	//				if(符号Y之后的子串可空)
	//					FOLLOW(Y).add(FOLLOW(X));
	//			}
	//		}
	//	}
	public void getFollowSets() {
		// 所有非终结符的follow集初始化为空
		for (String nonterminal : nonterminals) {
			followSets.put(nonterminal, new ArrayList<String>());
		}
		// 将#加入到follow(S)中
		followSets.get("Program").add("#");
		// 开始迭代，当没有FOLLOW集发生变化(isFollowSetsChanged == false)时结束迭代
		for (boolean isFollowSetsChanged = true; isFollowSetsChanged;) {
			isFollowSetsChanged = false;
			for (Production production : productions) {
				String productionLeft = production.getLeft();
				String[] productionRights = production.getRights();
				// 由于需要判断前驱和后继节点，因此不使用foreach语法，改用索引进行遍历
				for (int i = 0; i < productionRights.length; i++) {
					String productionRight = productionRights[i];
					// 仅有非终结符有FOLLOW集
					if (nonterminals.contains(productionRight)) {
						boolean isSubstringCanBeNull = true;
						for (int j = i + 1; j < productionRights.length; j++) {
							for (String firstSetElement : firstSets.get(productionRights[j])) {
								if (!followSets.get(productionRight).contains(firstSetElement)) {
									followSets.get(productionRight).add(firstSetElement);
									isFollowSetsChanged = true;
								}
							}
							// 声明当前正在处理的非终结符后面跟随的字串不可空
							if (!isCanBeNull(productionRights[j])) {
								isSubstringCanBeNull = false;
								break;
							}
						}
						// 若当前正在处理的非终结符后面跟随的字串可空，则将产生式左侧非终结符的FOLLOW集加入到当前正在处理的非终结符的FOLLOW集中
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
		// 清除follow集中的#(由于要remove，因此不能使用foreach)
		for (String productionLeft : nonterminals) {
			for (int i = 0; i < followSets.get(productionLeft).size(); i++) {
				if (followSets.get(productionLeft).get(i).equals("#"))
					followSets.get(productionLeft).remove(i);
			}
		}
	}



	// SELECT集算法：
	// SELECT(A->α)=FIRST(α) [α不可空]
	// SELECT(A->α)={FIRST(α)-空} U FOLLOW(A) [α可空]
	public void selectSet() {
		// 遍历每个产生式计算其SELECT集
		for (Production production : productions) {
			String productionLeft = production.getLeft();
			String[] productionRights = production.getRights();
			// 文法终止符号的SELECT集:SELECT(A->$)=FOLLOW(A)
			if (productionRights[0].equals("$")) {
				for (String followSetElement : followSets.get(productionLeft)) {
					if (!production.selectSet.contains(followSetElement)) {
						production.selectSet.add(followSetElement);
					}
				}
			} else {
				boolean isProductionCanBeNull = true;
				// 将FIRST(α)添加进SELECT(A->α)
				for (String productionRight : productionRights) {
					for (String firstSetElement : firstSets.get(productionRight)) {
						if (!production.selectSet.contains(firstSetElement)) {
							production.selectSet.add(firstSetElement);
						}
					}
					// 判断α是否可空，α中存在一个符号不可空则α不可空，应终止FIRST(α)的计算
					if (!isCanBeNull(productionRight)) {
						isProductionCanBeNull = false;
						break;
					}
				}
				// 若α可空，则将FOLLOW(A)添加进SELECT(A->α)
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




	//--------------------------------------构建预测分析表--------------------------------------
	// 根据每个产生式的SELECT集创建预测分析表，输出至prediction_table.txt
	// 格式：栈顶符号#输入符号 -> 产生式右部(产生式右部符号间以单空格分隔)
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