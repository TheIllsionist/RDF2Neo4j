package CypherElement.Clause;

import CypherElement.Basic.*;
import java.util.List;

/**
 * Cypher语句拼接工具
 * 注:只提供语句拼接作用,没有任何语法检查功能,使用该拼接工具的程序员必须要有一定的Cypher语法基础
 */
class Cypher{

    /**  利用匿名内部类复写ThreadLocal的initialValue()方法,每个线程第一次调用get方法时都会得到空串 **/
    private static ThreadLocal<String> cypher = new ThreadLocal<String>(){
        @Override
        public String initialValue(){
            return "";
        }
    };

    /**
     * 拼接查询一个Neo4j节点的Cypher语句
     * @param node
     */
    public static void match(CypherNode node){
        cypher.set(cypher.get() + "match" + node.toCypherStr());
    }

    /**
     * 拼接查询一个Neo4j路径的Cypher语句
     * @param path
     */
    public static void match(CypherPath path){
        cypher.set(cypher.get() + "match" + path.toCypherStr());
    }

    /**
     * 拼接查询多个Neo4j路径的Cypher语句
     * @param paths
     */
    public static void match(List<CypherPath> paths){
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append(cypher.get() + "match");
        for (CypherPath path:paths) {
            cypherbd.append(path.toCypherStr() + ",");
        }
        cypherbd.delete(cypherbd.length() - 1,cypherbd.length());
        cypher.set(cypherbd.toString());
    }

    /**
     * 拼接创建一个Neo4j节点的Cypher语句
     * @param node
     */
    public static void create(CypherNode node){
        cypher.set(cypher.get() + "create" + node.toCypherStr());
    }

    /**
     * 拼接创建一个Neo4j路径的Cypher语句
     * @param path
     */
    public static void create(CypherPath path){
        cypher.set(cypher.get() + "create" + path.toCypherStr());
    }

    /**
     * 拼接创建多个Neo4j路径的Cypher语句
     * @param paths
     */
    public static void create(List<CypherPath> paths){
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append(cypher.get() + "create");
        for (CypherPath path:paths) {
            cypherbd.append(path.toCypherStr() + ",");
        }
        cypherbd.delete(cypherbd.length() - 1,cypherbd.length());
        cypher.set(cypherbd.toString());
    }


    public static void where(PropValPair pair){

    }

    public static void where(CypherProperty property, Operator operator,CypherValue value){

    }

    public static void where(CypherProperty property1,Operator operator,CypherProperty property2){

    }

    public static void and(){

    }

    public static void or(){

    }

    public static void set(){

    }

    public static String getCypher(){
        String cypherStr = cypher.get();
        cypher.remove();   //每调用一次getCypher相当于一次拼接结束,所以要移走旧cypher
        return cypherStr;
    }

}
