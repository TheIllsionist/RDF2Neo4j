package CypherElement.Clause;

import CypherElement.Basic.CypherPath;
import java.util.List;

public class Cypher{

    /**  利用匿名内部类复写ThreadLocal的initialValue()方法,每个线程第一次调用get方法时都会得到空串 **/
    private static ThreadLocal<String> cypher = new ThreadLocal<String>(){
        @Override
        public String initialValue(){
            return "";
        }
    };

    public static Cypher match(List<CypherPath> paths){
        return null;
    }

    public static Cypher where(){
        return null;
    }
}
