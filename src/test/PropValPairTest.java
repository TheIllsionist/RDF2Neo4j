package test;
import cypherelement.basic.DataType;
import cypherelement.basic.Operator;
import cypherelement.basic.PropValPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropValPairTest {
    public static void main(String args[]) throws Exception{
        PropValPair pair1 = new PropValPair("name",Operator.MATCH,"高文豪");
        System.out.println(pair1.toInnerStr());
        System.out.println(pair1.toWhereStr());
        PropValPair pair2 = new PropValPair("age", Operator.SM_OR_EQ,24);
        System.out.println(pair2.toInnerStr());
        System.out.println(pair2.toWhereStr());
        PropValPair pair3 = new PropValPair("height",Operator.BIG_OR_EQ,178.5);
        System.out.println(pair3.toInnerStr());
        System.out.println(pair3.toWhereStr());
        List<String> list1 = new ArrayList<>();
        list1.add("自由");
        list1.add("梦想");
        list1.add("爱情");
        PropValPair pair4 = new PropValPair("追求",list1);
        System.out.println(pair4.toInnerStr());
        System.out.println(pair4.toWhereStr());
        List<Integer> list2 = new ArrayList<>();
        list2.add(1);
        list2.add(2);
        list2.add(3);
        PropValPair pair5 = new PropValPair("test",Operator.IN,list2, DataType.INT);
        System.out.println(pair5.toInnerStr());
        System.out.println(pair5.toWhereStr());
        Map<String,Double> map1 = new HashMap<>();
        map1.put("姓名",1.3);
        map1.put("本科",2.4);
        map1.put("硕士",3.5);
        PropValPair pair6 = new PropValPair("基本信息",Operator.IN,map1,DataType.DOUBLE);
        System.out.println(pair6.toInnerStr());
        System.out.println(pair6.toWhereStr());
    }
}
