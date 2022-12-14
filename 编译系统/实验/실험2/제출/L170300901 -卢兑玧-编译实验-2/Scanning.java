package bank.system;
    
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Scanning{
    //Process variable
    private char c;                                 
    private int row;                               
    private int index;                             
    //output variable
    private List<String[]> token;                   
    private List<String[]> error;                   
    private List<String[]> token_Parser_Input;      
    //input variable
    private List<String> program;                  
    //constant
    private List<String> KeyWord_List;             



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
        this.KeyWord_List = List.of(
                                "int"    , "float"   , "double" , "boolean"  , "true"    ,
                                "false"  , "include" , "char"   , "if"       , "else"    ,
                                "do"     , "while"   , "break"  , "continue" , "for"     ,
                                "main"   , "void"    , "printf" , "class"    , "scanf"   ,
                                "return" , "char"    , "public" , "static"   , "private" );
    }
    
    
    public static void main(String[] args) {
        List<String> program = new ArrayList<String>();
        //add source code here,end with "\n"
        program.add("int main(){\n");
        program.add("	while(a>0){}\n");
        program.add("}\n");
        Scanning scanning = new Scanning(program);
        scanning.DFA();
        System.out.println("??????????token????????");
        for(String[] token:scanning.getToken()){
            System.out.println(token[0]+token[1]);
        }
        System.out.println("??????????????????????");
        for(String[] error:scanning.getError()){
            System.out.println(error[0]+error[1]);
        }
        System.out.println("??????????????????");
        for(String[] input:scanning.getToken_Parser_Input()){
            for(String test: input){
                System.out.print(test+" ");
            }
            System.out.println();
        }
    }
    
    private boolean isEnd() {
        if (index >= program.get(row).length() && row == program.size() - 1)
            return true;
        return false;
    }
    
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

    //????????????????????????????????
    private boolean isLetter_(char s) {
        if ((s <= 'z' && s >= 'a') || (s <= 'Z' && s >= 'A') || (s == '_'))
            return true;
        return false;
    }

    //????????????????????????0??9??????
    private boolean isDigit(char s) {
        if (s <= '9' && s >= '0')
            return true;
        return false;
    }

    //????????????????????????0??7??????
    private boolean isOctalDigit(char s) {
        if (s <= '7' && s >= '0')
            return true;
        return false;
    }

    //????????????????????????0??f??????
    private boolean isHexDigit(char s) {
        if ((s <= '9' && s >= '0') || ((s <= 'f' && s >= 'a')) || (s <= 'F' && s >= 'A'))
            return true;
        return false;
    }

    //??????????????????????????????tab????????????
    private boolean isSpace(char s) {
        if(c == ' ' || c == '\r' || c == '\t' || c == '\n')
            return true;
        return false;
    }

    //????????????????????????????????????????????????????????????token
    private void receive_handler(String type,String value) {
        token.add(new String[]{type,value});
        if(type.equals("??????")){
			token_Parser_Input.add(new String[]{"ID",value,String.valueOf(row)});
		}
		else if(type.equals("??????????") || type.equals("????????") || type.equals("??????????")){
			token_Parser_Input.add(new String[]{"INT",value,String.valueOf(row)});
        }
		else if (type.equals("??????????")||type.equals("??????")) {
			token_Parser_Input.add(new String[]{"FLOAT",value,String.valueOf(row)});
		}
		else if(type.equals("????????")){
			token_Parser_Input.add(new String[]{"CHAR",value,String.valueOf(row)});
		}
		else if(type.equals("??????????")){
			token_Parser_Input.add(new String[]{"STRING",value,String.valueOf(row)});
        }
        else if(type.equals("????")){
            //??????????????????????
        }
		else {//????????????(????????)??????????????
			token_Parser_Input.add(new String[]{value,String.valueOf(row)});
		}
    }

    //??????????????????????????????????????????????????????????
    private void exception_handler(int exc_code,String s) {
        //??????????????????????
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
                    receive_handler("??????",indentifier);
                }
                else{
                    receive_handler("??????",indentifier);
                }
                return;
            }
        }
    }
    
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
                    receive_handler("??????????",number);
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
                    receive_handler("??????",number);
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
                    receive_handler("??????????",number);
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
                    receive_handler("??????????",number);
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
                    receive_handler("????????",number);
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
                    receive_handler("??????????",number);
                    return;
                }
            }
        }
    }
    
    private void DFA_symbol(){
        //1.DFA of '+',receive + ++ +=
        if(c == '+'){
            DFA_plus();
            //DFA_read();
        }

        //2.DFA of '-',receive -(??????) -- -=
        else if(c == '-'){
            DFA_minus();
        }

        //3.DFA of '*' '%' '!',receive * *= % %= !(??????) !=
        else if(c == '*' || c == '%' || c == '!'){
            DFA_mult_percent_not();
        }

        //4.DFA of '=',receive = ==
        else if(c == '='){
            DFA_equal();
        }

        //5.DFA of '/',receive / /= /**/(????)
        else if(c == '/'){
            DFA_slash();
        }

        //6.DFA of '>',receive > >= >> >>>(??????????)
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
            receive_handler("??????",c+"");
            getNextChar();
        }

        //11.DFA of single delimiter character,including , . ; ( ) [ ] { } #
        else if(c == ',' || c == '.' || c == ';' || c == '(' || c == ')' ||
                c == '[' || c == ']' || c == '{' || c == '}' || c == '#' ){
            //RECEIVE
            receive_handler("????",c+"");
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
            else if(state == 4){
                //RECEIVE
                receive_handler("????",symbol);
                return;
            }
        }
    }

    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
            else if(state == 4){
                //RECEIVE
                receive_handler("????",symbol);
                return;
            }
        }
    }
    
    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("????",symbol);
                return;
            }
        }
    }
    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
        }
    }
    
    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("????",symbol);
                return;
            }
        }
    }
    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("??????",symbol);
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 5){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
        }
    }
    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
            else if(state == 4){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
        }
    }

    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
        }
    }
    
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
                    receive_handler("??????",symbol);
                    return;
                }
            }
            else if(state == 3){
                //RECEIVE
                receive_handler("??????",symbol);
                return;
            }
        }
    }
    
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
                receive_handler("??????????",string);
                return;
            }
        }
    }
    
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
                receive_handler("????????",character);
                return;
            }
        }
    }
    
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
                receive_handler("????",comment);
                return;
            }
        }
    }
    
    private void DFA_read(){
        List<String> receive_states = new ArrayList<String>();
        List<Map<String,String>> dfa_map =new ArrayList<Map<String,String>>();
        //????FA????????????FA
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("C:/Users/Cosmos/Desktop/parser/2.txt")));
            //??0????????????????
            receive_states = Arrays.asList(reader.readLine().split(" "));
            //????????????????????????????????????????????????????
            //??????????????????"->"??????????????"->"????????????
            while (reader.ready()) {
                Map<String,String> state_row =new HashMap<String,String>();
                for(String state_transition : reader.readLine().split(" ")){
                    System.out.println(state_transition);
                    if(state_transition.length() > 2){
                        state_row.put(state_transition.split("->")[0], state_transition.split("->")[1]);
                    }
                }
                dfa_map.add(state_row);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("DFA??"+dfa_map.size()+"??????");

        //DFA????????????????state1
        String receive_string = "";
        String state = "0";
        while(true){
            //????????????????????????????????????????????
            for(String receive_characters : dfa_map.get(Integer.valueOf(state)).keySet()){
                //????????????????????????????
                if(receive_characters.contains(String.valueOf(c))){
                    state = dfa_map.get(Integer.valueOf(state)).get(receive_characters);
                    receive_string += c;
                    getNextChar();
                    break;
                }
                //????????????????????????????????????????????????????????????
                else{
                    if(receive_states.contains(state)){
                        //RECEIVE
                        receive_handler("????????",receive_string);
                        return;
                    }
                    else{
                        //EXCEPTION
                        exception_handler(2,receive_string);
                        return;
                    }
                }
            }
        }
    }
}
    