package bank.system;

import java.util.List;

public class Function {
    private String name;
    private String returnType;
    public List<String> paramTypes;

    public Function(String name,String returnType,List<String> paramTypes){
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    public String getName(){
        return name;
    }

    public String getReturnType(){
        return returnType;
    }
}
