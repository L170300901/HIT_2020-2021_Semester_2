package bank.system;

import java.util.*;


public class Scanning{
    //Process variable
    private char c;                                 //当前DFA正在处理的字符
    private int row;                                //当前DFA正在处理的字符所处的行号，用于报告错误信息
    private int index;                              //当前DFA正在处理的字符在该行的索引，用于字符指针的前移和后退
    //output variable
    private List<String[]> token;                   //词法分析的分析结果，以{种别码,属性值}的格式存储
    private List<String[]> error;                   //词法分析的错误信息，以{错误行号,错误信息}的格式存储
    private List<String[]> token_Parser_Input;      //用于语法分析的输入，以{种别码,属性值}的格式存储
    //input variable
    private List<String> program;                   //被分析的程序文本
    //constant
    private List<String> KeyWord_List;              //关键字列表，用于识别关键字



    //------------------------------Construct function------------------------------
    public Scanning(List<String> program){
        //input variable
        this.program      = new ArrayList<String>(program);
        //output variable
        this.token        = new ArrayList<String[]>();
        this.error        = new ArrayList<String[]>();
        this.token_Parser_Input = new ArrayList<String[]>();
        //Process variable
        this.row          = 0;
        this.index        = -1;
        this.c='#';//'#'start and '&'end
        //constant
        this.KeyWord_List = Arrays.asList(
                                "int"    , "float"   , "double" , "boolean"  , "true"    ,
                                "false"  , "include" , "char"   , "if"       , "else"    ,
                                "do"     , "while"   , "break"  , "continue" , "for"     ,
                                "void"   , "struct"  , "return" , "char"    );
    }
    //------------------------------------------------------------------------------




    //------------------------------test function------------------------------
    public static void main(String[] args) {
        List<String> program = new ArrayList<String>();
        //add source code here,end with "\n"
        program.add("int main(){\n");
        program.add("	while(a>0){}\n");
        program.add("}\n");
        Scanning scanning = new Scanning(program);
        scanning.DFA();
        System.out.println("词法分析的token序列为：");
        for(String[] token:scanning.getToken()){
            System.out.println(token[0]+token[1]);
        }
        System.out.println("词法分析的错误信息有：");
        for(String[] error:scanning.getError()){
            System.out.println(error[0]+error[1]);
        }
        System.out.println("语法分析的输入为：");
        for(String[] input:scanning.getToken_Parser_Input()){
            for(String test: input){
                System.out.print(test+" ");
            }
            System.out.println();
        }
    }
    //-------------------------------------------------------------------------





    //------------------------------assistant function------------------------------
    //判断词法分析是否完成(用于注释不闭合的情况)
    private boolean isEnd() {
        if (index >= program.get(row).length() && row == program.size() - 1)
            return true;
        return false;
    }

    //将输入字符指针指向下一个字符，若已到达文本结尾，则返回'$'
    // When reach the end of a line:
    // If there are still remaining row,row++
    // Otherwise, return the '$' to show reach the end of text
    private void getNextChar() {
        if(index >= program.get(row).length() - 1){
            if (row >= program.size() - 1){
                index = program.get(row).length();
                c = '$';
            }
            else{
                row++;
                index=0;
                c = program.get(row).charAt(index);
            }
        }
        else{
            index++;
            c = program.get(row).charAt(index);
        }
    }

    //将输入字符指针指向上一个字符，若已到达文本开头，则返回'#'
    // When reach the start of a line:
    // If there are still remaining row,row--
    // Otherwise, return the '#' to show reach the start of text
    private void getPreviousChar() {
        if(index <= 0){
            if(row <= 0){
                c = '#';
            }
            else{
                row--;
                index = program.get(row).length()-1;
                c     = program.get(row).charAt(index);
            }
        }
        else{
            index--;
            c = program.get(row).charAt(index);
        }
    }

    //用于判断该字符是否是字母或下划线
    private boolean isLetter_(char s) {
        if ((s <= 'z' && s >= 'a') || (s <= 'Z' && s >= 'A') || (s == '_'))
            return true;
        return false;
    }

    //用于判断该字符是否是一个0到9的数字
    private boolean isDigit(char s) {
        if (s <= '9' && s >= '0')
            return true;
        return false;
    }

    //用于判断该字符是否是一个0到7的数字
    private boolean isOctalDigit(char s) {
        if (s <= '7' && s >= '0')
            return true;
        return false;
    }

    //用于判断该字符是否是一个0到f的数字
    private boolean isHexDigit(char s) {
        if ((s <= '9' && s >= '0') || ((s <= 'f' && s >= 'a')) || (s <= 'F' && s >= 'A'))
            return true;
        return false;
    }

    //用于判断该字符是否是一个空格，tab，回车或换行
    private boolean isSpace(char s) {
        if(c == ' ' || c == '\r' || c == '\t' || c == '\n')
            return true;
        return false;
    }

    //接收函数：当一个单词被自动机识别并接受，则调用接收函数，形成token
    private void receive_handler(String type,String value) {
        token.add(new String[]{type,value});
        if(type.equals("标识符")){
			token_Parser_Input.add(new String[]{"ID",value,String.valueOf(row)});
		}
		else if(type.equals("无符号整数") || type.equals("八进制数") || type.equals("十六进制数")){
			token_Parser_Input.add(new String[]{"INT",value,String.valueOf(row)});
        }
		else if (type.equals("科学计数法")||type.equals("浮点数")) {
			token_Parser_Input.add(new String[]{"FLOAT",value,String.valueOf(row)});
		}
		else if(type.equals("字符常量")){
			token_Parser_Input.add(new String[]{"CHAR",value,String.valueOf(row)});
		}
		else if(type.equals("字符串常量")){
			token_Parser_Input.add(new String[]{"STRING",value,String.valueOf(row)});
        }
        else if(type.equals("注释")){
            //注释不进入语法分析阶段
        }
		else {//包含：关键字(一词一码)，运算符，界符
			token_Parser_Input.add(new String[]{value,String.valueOf(row)});
		}
    }

    //异常处理程序，用于处理不可识别的字符和自动机无法接收的单词
    private void exception_handler(int exc_code,String s) {
        //获取一个完整的错误单词
        //Identifier format error
        if(exc_code == 1){
            while(isLetter_(c) || isDigit(c)){
                s += c;
                getNextChar();
            }
            error.add(new String[]{row+"", "Incorrect identifier format:"+s});
        }
        //String format error:include "" is not closed,and insert '\n' into String
        else if(exc_code == 2){
            index = Integer.valueOf(s);
            getNextChar();
            error.add(new String[]{row+"", "\" is not closed:"});
        }
        //Char format error:' follow by \r \n \t
        else if(exc_code == 3){
            error.add(new String[]{row+"", "Incorrect char format,mismatch of another \'"});
        }
        //Char length error:char have more than one character or mismatch of another '
        else if(exc_code == 4){
            int start_row = row;
            int start_index = index;
            while(c != '\'' && c !='\n'){
                s += c;
                getNextChar();
            }
            if(c =='\n'){
                row = start_row;
                index = start_index;
                getPreviousChar();
                error.add(new String[]{row+"", "Incorrect char format,mismatch of another \'"});
            }
            else{
                getNextChar();
                error.add(new String[]{row+"", "Incorrect char format:"+s});
            }
        }
        //int and float Number format error:
        else if(exc_code == 5){
            while(isDigit(c) || isLetter_(c)){
                s += c;
                getNextChar();
            }
            error.add(new String[]{row+"", "Incorrect number format:"+s});
        }
        //Comment format error:
        else if(exc_code == 6){
            error.add(new String[]{row+"", "Incorrect comment format:"+s});
        }
        //Comment not closed error:
        else if(exc_code == 7){
            row =Integer.valueOf(s.split(",")[0]);
            index =Integer.valueOf(s.split(",")[1]);
            getNextChar();//movement of '/'
            getNextChar();//movement of '*'
            error.add(new String[]{row+"", "Comment is not closed,mismatch another */"});
        }
        //Symbol format error:(only \t \r \n can throw this exception)
        else if(exc_code == 8){
            while(!isSpace(c)){
                s += c;
                getNextChar();
            }
            error.add(new String[]{row+"", "Incorrect symbol format:"+s});
        }
        //Unsupport character error:
        else if(exc_code == 9){
            error.add(new String[]{row+"", "Unrecognized character \""+s+"\""});
        }
        //Octal Number format error:
        else if(exc_code == 10){
            while(isLetter_(c) || isDigit(c)){
                s += c;
                getNextChar();
            }
            error.add(new String[]{row+"", "Incorrect Octal Number format:"+s});
        }
        //Hex Number format error:
        else if(exc_code == 11){
            while(isLetter_(c) || isDigit(c)){
                s += c;
                getNextChar();
            }
            error.add(new String[]{row+"", "Incorrect Octal Number format:"+s});
        }
    }

    public List<String[]> getToken() {
        return new ArrayList<String[]>(token);
    }

    public List<String[]> getError() {
        return new ArrayList<String[]>(error);
    }

    public List<String[]> getToken_Parser_Input() {
        return new ArrayList<String[]>(token_Parser_Input);
    }
    //------------------------------------------------------------------------------


    //--------------------------------------DFA--------------------------------------
    //DFA主控程序：分析第一个字母，确定使用哪一个自动机
    //letter_ 则是标识符/关键字
    //数字，则是常数
    //符号，则是运算符/界符/注释/字符串常量/字符常量
    //                    letter_
    //               --------------->DFA_identifier(including keywords)
    //               |
    //               |
    //               |
    //               |     digit
    //DFA------------+-------------->DFA_number
    //               |
    //               |
    //               |
    //               |     other
    //               --------------->DFA_symbol(including String, comment and char)
    public void DFA() {
        getNextChar();
        for(char firstChar = c;firstChar != '$';firstChar = c){
            if(isLetter_(firstChar)){
                DFA_identifier();
            }
            else if(isDigit(firstChar)){
                DFA_number();
            }
            else{
                DFA_symbol();
            }
        }
    }

    //  识别标识符和关键字的DFA：关键字在标识符识别完成后判断是否在标识符集合中
    //  Regular expression of identifier : id = letter_ (letter_ | number)*
    //  DFA of identifier:
    //
    //                                     letter_/number              other
    //  start(state0)------------->state1------------------>state2-------------->exit
    //                                                      |   ⬆
    //                                                      |___|
    //                                                  letter_/number
    //
    //  receive state: state2
    private void DFA_identifier() {
        int state = 0;
        String indentifier = "";
        while (true) {
            // start(state0)
            if (state == 0)
                state = 1;
            // state1
            else if (state == 1) {
                if (isLetter_(c)) {
                    state = 2;
                    indentifier += c;
                    getNextChar();
                } else {
                    // throw Exception;
                    exception_handler(1,indentifier);
                    return;
                }
            }
            // state2
            else if (state == 2) {
                if (isLetter_(c) || isDigit(c)) {
                    state = 2;
                    indentifier += c;
                    getNextChar();
                }
                else{
                    state = 3;
                }
            }
            //exit(state3)
            else if(state == 3){
                //RECEIVE
                if(KeyWord_List.contains(indentifier)){
                    receive_handler("关键字",indentifier);
                }
                else{
                    receive_handler("标识符",indentifier);
                }
                return;
            }
        }
    }

    //  识别常数的DFA：可识别内容包括：无符号整数和浮点数，包括科学计数法，字符串常数
    //  科学计数法格式：1.352E32  97.78E-45
    //  Regular expression of number : number = digit+  ((. digit+)|ε)  ((E(+|-|ε)digit+)|ε)
    //                                          digits      Fraction         EXponent
    //  DFA of number:
    //                                                        E                        +/-       digit
    //                                     ----------------------------------------   ---->state6-----
    //                                     |                                      |   |              |
    //                              1~9    |      .           digit           E   ↓   |    digit     ↓
    //  start(state0)----->state1------->state2------->state3------->state4------>state5---------->state7
    //                       |           | ⬆  ⬆          ⬆           |   ⬆          ⬆              |   ⬆
    //                       |           |_|  |          |           |___|          |              |___|
    //                       |          digit |0         |.          digit         E|              digit
    //                       |                |__________|__________________________|
    //                       |                           |
    //                       |             0             |                         1~7
    //                       ------------------------->state8--------------------------------------------->state9
    //                                                   |                                                |   ⬆
    //                                                   |      X/x                0~f                    |___|
    //                                                   |------------>state10------------>state11         0~7
    //                                                                                      |   ⬆
    //                                                                                      |___|
    //                                                                                       0~f
    //
    //  receive state: state2(int),state4(float),state7(number with exponent),state8(singel zero)
    //                 state9(octal digits),state11(hex digits)
    private void DFA_number(){
        int state = 0;
        String number = "";
        while(true){
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(isDigit(c)){
                    if(c == '0'){
                        state = 8;
                        number += c;
                        getNextChar();
                    }
                    else{
                        state = 2;
                        number += c;
                        getNextChar();
                    }
                }
                else{
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
            }
            else if(state == 2){
                if(isDigit(c)){
                    state = 2;
                    number += c;
                    getNextChar();
                }
                else if(c == '.'){
                    state = 3;
                    number += c;
                    getNextChar();
                }
                else if(c == 'E'){
                    state = 5;
                    number += c;
                    getNextChar();
                }
                else if(isLetter_(c)){
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
                else{
                    //RECEIVE
                    receive_handler("无符号整数",number);
                    return;
                }
            }
            else if(state == 3){
                if(isDigit(c)){
                    state = 4;
                    number += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
            }
            else if(state == 4){
                if(isDigit(c)){
                    state = 4;
                    number += c;
                    getNextChar();
                }
                else if(c == 'E'){
                    state = 5;
                    number += c;
                    getNextChar();
                }
                else if(isLetter_(c)){
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
                else{
                    //RECEIVE
                    receive_handler("浮点数",number);
                    return;
                }
            }
            else if(state == 5){
                if(isDigit(c)){
                    state = 7;
                    number += c;
                    getNextChar();
                }
                else if(c == '+' || c == '-'){
                    state = 6;
                    number += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
            }
            else if(state == 6){
                if(isDigit(c)){
                    state = 7;
                    number += c;
                    getNextChar();
                }
                else if(isLetter_(c)){
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
                else{
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
            }
            else if(state == 7){
                if(isDigit(c)){
                    state = 7;
                    number += c;
                    getNextChar();
                }
                else if(isLetter_(c)){
                    //EXCEPTION
                    exception_handler(5,number);
                    return;
                }
                else{
                    //RECEIVE
                    receive_handler("科学计数法",number);
                    return;
                }
            }
            else if(state == 8){
                if(c == 'X' || c == 'x'){
                    state = 10;
                    number += c;
                    getNextChar();
                }
                else if(isOctalDigit(c) && c != '0'){
                    state = 9;
                    number += c;
                    getNextChar();
                }
                else if(c == '0'){
                    state = 2;
                    number += c;
                    getNextChar();
                }
                else if(c == '.'){
                    state = 3;
                    number += c;
                    getNextChar();
                }
                else if(c == 'E'){
                    state = 5;
                    number += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("无符号整数",number);
                    return;
                }
            }
            else if(state == 9){
                if(isOctalDigit(c)){
                    state = 9;
                    number += c;
                    getNextChar();
                }
                else if(isLetter_(c) || isDigit(c)){
                    //EXCEPTION
                    exception_handler(10,number);
                    return;
                }
                else{
                    //RECEIVE
                    receive_handler("八进制数",number);
                    return;
                }
            }
            else if(state == 10){
                if(isHexDigit(c)){
                    state = 11;
                    number += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(11,number);
                    return;
                }
            }
            else if(state == 11){
                if(isHexDigit(c)){
                    state = 11;
                    number += c;
                    getNextChar();
                }
                else if(isLetter_(c)){
                    //EXCEPTION
                    exception_handler(11,number);
                    return;
                }
                else{
                    //RECEIVE
                    receive_handler("十六进制数",number);
                    return;
                }
            }
        }
    }

    //  识别符号的DFA：可识别内容包括：运算符，界符，注释，字符串常量，字符常量
    //一、支持被识别的符号
    //（一）运算符
    //1.算术运算符
    //单目：-（负号运算符）、 ++（自增）、 - -（自减）、
    //双目：+ - * / %（取余）
    //三目：? :
    //2.关系运算符
    //等于符号:==，不等于符号:!= ，大于符号:>， 小于符号:<，大于等于符号:>= ，小于等于符号:<=
    //3.逻辑运算符
    //位运算符　与（&）、非（~）、或（|）、异或（^）
    //位移 << 带符号左移 >>带符号右移 >>> 无符号右移
    //逻辑运算符 与（&&）、非（!）、或（||）
    //（二）界符
    //1.用于赋值语句的界符(不考虑&= ^= |= >>= <<=等赋值运算符)
    //= += -= *= /= %=
    //2.用于句子结尾的界符，如“；”
    //单界符 ',' '.' ';'
    //3.用于数组表示的界符，如“[”和“]”；
	//双界符  '['   ']' '(' ')' '{' '}'
    //空白符  ' ' '\n' '\r' '\t'
    //头文件导入符号 #
    //二、所有会被处理的字符的集合
    // + - * / % ? : = ! > < & | ~ ^ , . ; " ' ( ) [ ] { } 空格 #
    //三、处理方法
    //为每个存在冲突的字符单独编写处理程序(DFA),根据符号的开始字符选择不同的DFA

    private void DFA_symbol(){
        //1.DFA of '+',receive + ++ +=
        if(c == '+'){
            DFA_plus();
            //DFA_read();
        }

        //2.DFA of '-',receive -(含负号) -- -=
        else if(c == '-'){
            DFA_minus();
        }

        //3.DFA of '*' '%' '!',receive * *= % %= !(逻辑非) !=
        else if(c == '*' || c == '%' || c == '!'){
            DFA_mult_percent_not();
        }

        //4.DFA of '=',receive = ==
        else if(c == '='){
            DFA_equal();
        }

        //5.DFA of '/',receive / /= /**/(注释)
        else if(c == '/'){
            DFA_slash();
        }

        //6.DFA of '>',receive > >= >> >>>(无符号右移)
        else if(c == '>'){
            DFA_greater();
        }

        //7.DFA of '<',receive < <= <<
        else if(c == '<'){
            DFA_less();
        }

        //8.DFA of '&',receive & &&
        else if(c == '&'){
            DFA_and();
        }

        //9.DFA of '|',receive | ||
        else if(c == '|'){
            DFA_or();
        }

        //10.DFA of single operator character,including ? : ~ ^
        else if(c =='?' || c == ':' ||c == '~' || c == '^' ){
            //RECEIVE
            receive_handler("运算符",c+"");
            getNextChar();
        }

        //11.DFA of single delimiter character,including , . ; ( ) [ ] { } #
        else if(c == ',' || c == '.' || c == ';' || c == '(' || c == ')' ||
                c == '[' || c == ']' || c == '{' || c == '}' || c == '#' ){
            //RECEIVE
            receive_handler("界符",c+"");
            getNextChar();
        }

        //12.Indentify the String,start with "
        else if(c == '"'){
            DFA_string();
        }

        //13.Indentify the Char,start with '
        else if(c =='\''){
            DFA_char();
        }

        //14.Ignore the space
        else if(c == ' ' || c == '\r' || c == '\t' || c == '\n'){
            getNextChar();
        }

        //15.Unsupport character error
        else{
            exception_handler(9,c+"");
            getNextChar();
            return;
        }
    }

    //  DFA of '+':(identify: + ++ +=)
    //
    //                            +          +
    //  start(state0)---->state1----->state2---->state3       state4
    //                                   |                      ⬆
    //                                   |______________________|
    //                                              =
    //
    //  receive state: state2,state3,state4
    private void DFA_plus(){
        int state=0;
        String symbol="";
        while(true){
            if(state == 0)
                state = 1;
            else if(state == 1){
                if(c == '+'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '+'){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else if(c == '='){
                    state = 4;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
            else if(state == 4){
                //RECEIVE
                receive_handler("界符",symbol);
                return;
            }
        }
    }

    //  DFA of '-':(identify: - -- -=)
    //
    //                            -          -
    //  start(state0)---->state1----->state2---->state3       state4
    //                                   |                      ⬆
    //                                   |______________________|
    //                                              =
    //
    //  receive state: state2,state3,state4
    private void DFA_minus(){
        int state=0;
        String symbol="";
        while(true){
            if(state == 0)
                state = 1;
            else if(state == 1){
                if(c == '-'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '-'){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else if(c == '='){
                    state = 4;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
            else if(state == 4){
                //RECEIVE
                receive_handler("界符",symbol);
                return;
            }
        }
    }

    //  DFA of '*/%/!':(identify:* % ! *= %= !=)
    //
    //                           */%/!            =
    //  start(state0)---->state1--------->state2----->state3
    //
    //
    //  receive state: state2,state3
    private void DFA_mult_percent_not(){
        int state=0;
        String symbol="";
        while (true) {
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(c == '*' || c == '%' || c == '!'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '='){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("界符",symbol);
                return;
            }
        }
    }

    //  DFA of '=':(identify: = ==)
    //
    //                              =             =
    //  start(state0)---->state1--------->state2----->state3
    //
    //
    //  receive state: state2,state3
    private void DFA_equal(){
        int state=0;
        String symbol="";
        while (true) {
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(c == '='){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '='){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
        }
    }

    //  DFA of '/':
    //
    //                            /           =
    //  start(state0)---->state1----->state2----->state3   DFA_COMMENT
    //                                   |                      ⬆
    //                                   |______________________|
    //                                            * or /
    //
    //  receive state: state2,state3
    private void DFA_slash(){
        int state=0;
        String symbol="";
        while (true) {
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(c == '/'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '='){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else if(c == '*' || c == '/'){
                    getPreviousChar();
                    DFA_comment();
                    return;
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("界符",symbol);
                return;
            }
        }
    }

    //  DFA of '>':(//>: > >= >> >>>(无符号右移))
    //
    //                            >           =                         >
    //  start(state0)---->state1----->state2----->state3      state4------->state5
    //                                   |                      ⬆
    //                                   |______________________|
    //                                              >
    //
    //  receive state: state2,state3,state4,state5
    private void DFA_greater(){
        int state=0;
        String symbol="";
        while (true) {
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(c == '>'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '='){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else if(c == '>'){
                    state = 4;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
            else if(state == 4){
                if(c == '>'){
                    state = 5;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 5){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
        }
    }

    //  DFA of '<':
    //
    //                            <           =
    //  start(state0)---->state1----->state2----->state3      state4
    //                                   |                      ⬆
    //                                   |______________________|
    //                                              <
    //
    //  receive state: state2,state3,state4
    private void DFA_less(){
        int state=0;
        String symbol="";
        while (true) {
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(c == '<'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '='){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else if(c == '<'){
                    state = 4;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
            else if(state == 4){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
        }
    }

    // DFA of '&':
    //                            &           &
    //  start(state0)---->state1----->state2----->state3
    //
    //
    //  receive state: state2,state3
    private void DFA_and(){
        int state=0;
        String symbol="";
        while (true) {
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(c == '&'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '&'){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
        }
    }

    //  DFA of '|':
    //                            |           |
    //  start(state0)---->state1----->state2----->state3
    //
    //
    //  receive state: state2,state3
    private void DFA_or(){
        int state=0;
        String symbol="";
        while (true) {
            if(state == 0){
                state = 1;
            }
            else if(state == 1){
                if(c == '|'){
                    state = 2;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //EXCEPTION
                    exception_handler(8,symbol);
                    return;
                }
            }
            else if(state == 2){
                if(c == '|'){
                    state = 3;
                    symbol += c;
                    getNextChar();
                }
                else{
                    //RECEIVE
                    receive_handler("运算符",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("运算符",symbol);
                return;
            }
        }
    }

    //  识别字符串常量的DFA(注意：字符串常量不能跨越行，即中间不能有'\n',但可以有"\n")
    //  Regular expression of string : string = " other* "
    //
    //  DFA of string:
    //
    //                                           "                      "
    //  start(state0)------------->state1------------------>state2-------------->exit(state3)
    //                                                      |   ⬆
    //                                                      |___|
    //                                                 other(except '\n')
    //
    //  receive state: state3
    private void DFA_string(){
        int state = 0;
        int start_index = index;
        String string = "";
        while (true) {
            // start(state0)
            if (state == 0)
                state = 1;
            // state1
            else if (state == 1) {
                if (c == '"') {
                    state = 2;
                    getNextChar();
                }
                else {
                    // throw Exception;
                    exception_handler(2,start_index+"");
                    return;
                }
            }
            // state2
            else if (state == 2) {
                if (c == '"') {
                    state = 3;
                    getNextChar();
                }
                else if(c == '\n'){
                    // throw Exception;
                    exception_handler(2,start_index+"");
                    return;
                }
                else{
                    state = 2;
                    string += c;
                    getNextChar();
                }
            }
            //exit(state3)
            else if(state == 3){
                //RECEIVE
                receive_handler("字符串常量",string);
                return;
            }
        }
    }

    //  识别字符常量的DFA(仅支持一部分转义字符 '\n' '\t' '\r')
    //  Regular expression of char : char = ' other '
    //
    //  DFA of char:
    //
    //                             '           other          '
    //  start(state0)----->state1----->state2-------->state3------>exit(state5)
    //                                   |               ↑
    //                                   | \             |
    //                                   ↓      r/t/n    |
    //                                 state4-------------
    //
    //  receive state: state3
    private void DFA_char(){
        int state = 0;
        String character = "";
        while (true) {
            // start(state0)
            if (state == 0)
                state = 1;
            // state1
            else if (state == 1) {
                if (c == '\'') {
                    state = 2;
                    getNextChar();
                } else {
                    // throw Exception;
                    exception_handler(3,character);
                    return;
                }
            }
            // state2
            else if (state == 2) {
                if(c == '\\'){
                    state = 4;
                    character += c;
                    getNextChar();
                }
                else if(c == '\r' || c == '\t' || c == '\n'){
                    // throw Exception;
                    exception_handler(3,character);
                    return;
                }
                else{
                    state = 3;
                    character += c;
                    getNextChar();
                }
            }
            else if (state == 3) {
                if (c == '\'') {
                    state = 5;
                    getNextChar();
                } else {
                    // throw Exception;
                    exception_handler(4,character);
                    return;
                }
            }
            else if (state == 4) {
                if (c == 'r' || c == 't' || c =='n') {
                    state = 3;
                    character += c;
                    getNextChar();
                } else {
                    // throw Exception;
                    exception_handler(4,character);
                    return;
                }
            }
            //exit(state5)
            else if(state == 5){
                receive_handler("字符常量",character);
                return;
            }
        }
    }

    //  识别注释的DFA
    //  Regular expression of comment : comment = /* other* */ or //other*
    //
    //  DFA of comment:
    //
    //                            /           *           *           /
    //  start(state0)---->state1----->state2---->state3----->state4----->exit(state6)
    //                                  |        | ⬆ ⬆       | | ⬆         ⬆
    //                                  |        |_| |_______| |_|         |
    //                                  |       other  other    *          |
    //                                  |   /                  \n          |
    //                                  -------->state5---------------------
    //                                            |  ⬆
    //                                            |__|
    //                                            other
    //  receive state: state5
    private void DFA_comment(){
        int state = 0;
        int start_row = row;
        int start_index = index;
        String comment = "";
        while (true) {
            // start(state0)
            if (state == 0)
                state = 1;
            // state1
            else if (state == 1) {
                if (c == '/') {
                    state = 2;
                    getNextChar();
                } else {
                    // throw Exception;
                    exception_handler(6,comment);
                    return;
                }
            }
            // state2
            else if (state == 2) {
                if (c == '*') {
                    state = 3;
                    getNextChar();
                }
                else if(c == '/'){
                    state = 5;
                    getNextChar();
                }
                else{
                    // throw Exception;
                    exception_handler(6,comment);
                    return;
                }
            }
            else if (state == 3) {
                if (c == '*') {
                    state = 4;
                    getNextChar();
                }
                else if(isEnd()){
                    exception_handler(7,start_row+","+start_index);
                    return;
                }
                else {
                    state = 3;
                    comment += c;
                    getNextChar();
                }
            }
            else if(state == 4){
                if (c == '/') {
                    state = 6;
                    getNextChar();
                }
                else if (c == '*') {
                    state = 4;
                    comment += c;
                    getNextChar();
                }
                else {
                    state = 3;
                    comment += c;
                    getNextChar();
                }
            }
            else if(state == 5){
                if(c != '\n'){
                    state = 5;
                    comment += c;
                    getNextChar();
                }
                else{
                    state = 6;
                    getNextChar();
                }
            }
            else if(state == 6){
                receive_handler("注释",comment);
                return;
            }
        }
    }
    //-------------------------------------------------------------------------------
}