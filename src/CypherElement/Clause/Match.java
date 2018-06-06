package CypherElement.Clause;

public class Match { //TODO://

    /**  利用匿名内部类复写ThreadLocal的initialValue()方法,使得每个线程第一次调用get方法时都会得到 "match "子句 **/
    private static ThreadLocal<String> cypher = new ThreadLocal<String>(){
        @Override
        public String initialValue(){
            return "match ";
        }
    };

}
