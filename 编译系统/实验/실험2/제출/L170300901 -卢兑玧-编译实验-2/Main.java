package bank.system;



import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

class Main {
  

    // TODO Auto-generated method stub
    public static void main(String[] args) {
      int n=9;
       List<StringBuffer> ans=new ArrayList<>();
       List<StringBuffer> num = null;
       StringBuffer s=new StringBuffer();
       s.append("()");
       ans.add(s);
       for (int i=0;i<n-1;i++){
           int len=ans.size();
          num=new ArrayList<>();
           for (int j=0;j<len;j++){
               
               StringBuffer ss=new StringBuffer();
               ss.append("()"+ans.get(j));
               num.add(ss);
//               System.out.println(j);
               ss=new StringBuffer();
               ss.append("("+ans.get(j)+")");
               num.add(ss);
//               System.out.println(num.toString());
           }
           ans=num;
       }
       System.out.println(ans);
   }
}

