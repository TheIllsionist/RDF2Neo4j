package cypherelement.clause;

import concurrentannotation.ThreadSafe;
import cypherelement.basic.*;
import java.util.List;
import java.util.Set;

/**
 * Cypher语句拼接工具
 * 该类只有一个ThreadLocal类型的状态,类中的所有方法也都没有引用其他类的公开域,只使用了虚拟机栈中的局部变量,因此是线程安全的
 * 注:该类只提供语句拼接作用,没有任何语法检查功能,使用该拼接工具的程序员必须要有一定的Cypher语法基础
 */
@ThreadSafe
class Cypher{
    /**
     * 使用ThreadLoacl<T>定义的变量是线程私有的,即多个访问该变量的线程,每个都有一个副本
     * 利用匿名内部类复写ThreadLocal的initialValue()方法,每个线程第一次调用get方法时都会得到空串
     */
    private static ThreadLocal<String> cypher = new ThreadLocal<String>(){
        @Override
        public String initialValue(){
            return "";
        }
    };


    public static void match(String matchClause){
        cypher.set(cypher.get() + "match" + matchClause);
    }

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

    public static void create(String createClause){
        cypher.set(cypher.get() + "create " + createClause);
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

    public static void where(String whereClause){
        cypher.set(cypher.get() + " where " + whereClause);
    }

    /**
     * 为当前Cypher语句添加Where子句和1个条件
     * @param condition
     */
    public static void where(CypherCondition condition){
        cypher.set(cypher.get() + " where " + condition.toCypherStr());
    }

    /**
     * 为当前Cypher语句添加Where子句和多个条件,多个条件之间用 'and '连接
     * @param conditions
     */
    public static void where(Set<CypherCondition> conditions){
        if(conditions == null || conditions.size() == 0)
            return;
        StringBuilder builder = new StringBuilder();
        builder.append(cypher.get());
        builder.append(" where ( ");
        for (CypherCondition condition : conditions) {
            builder.append(condition.toCypherStr() + " and");
        }
        builder.delete(builder.length() - 3,builder.length());
        builder.append(")");
        cypher.set(builder.toString());
    }

    public static void and(String newCondition){
        cypher.set(cypher.get() + " and " + newCondition);
    }

    /**
     * 用and连接符为Where子句添加1条件
     * @param condition
     */
    public static void and(CypherCondition condition){
        cypher.set(cypher.get() + " and " + condition.toCypherStr());
    }

    /**
     * 用and连接符为Where子句添加多个条件,内部的多个条件默认使用' and '连接
     * @param conditions
     */
    public static void and(Set<CypherCondition> conditions){
        if(conditions == null || conditions.size() == 0)
            return;
        StringBuilder builder = new StringBuilder();
        builder.append(cypher.get() + " and ( ");
        for(CypherCondition condition : conditions){
            builder.append(condition.toCypherStr() + " and");
        }
        builder.delete(builder.length() - 3,builder.length());
        builder.append(")");
        cypher.set(builder.toString());
    }

    public static void or(String newCondition){
        cypher.set(cypher.get() + " or " + newCondition);
    }

    public static void or(CypherCondition condition){
        cypher.set(cypher.get() + " or " + condition.toCypherStr());
    }

    public static void or(Set<CypherCondition> conditions){
        if(conditions == null || conditions.size() == 0){
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(cypher.get() + " or ( ");
        for (CypherCondition condition : conditions) {
            builder.append(condition.toCypherStr() + " and");
        }
        builder.delete(builder.length() - 3,builder.length());
        builder.append(")");
        cypher.set(builder.toString());
    }

    public static void set(String newCondition){
        cypher.set(cypher.get() + " set " + newCondition);
    }

    /**
     * 为Set子句设置1个表达式
     * @param condition
     */
    public static void set(CypherCondition condition){
        cypher.set(cypher.get() + " set " + condition.toCypherStr());
    }

    /**
     * 为Set子句设置多个表达式,表达式之间使用' , '隔开
     * @param conditions
     */
    public static void set(Set<CypherCondition> conditions){
        if(conditions == null || conditions.size() == 0){
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(cypher.get() + " set ");
        for (CypherCondition condition : conditions) {
            builder.append(condition.toCypherStr() + ",");
        }
        builder.delete(builder.length() - 1,builder.length());
        cypher.set(builder.toString());
    }

    public static void wantReturn(String returnClause){
        cypher.set(cypher.get() + " return " + returnClause);
    }

    public static void wantReturn(CypherProperty property){
        cypher.set(cypher.get() + " return " + property.toCypherStr());
    }

    public static void wantReturn(List<CypherProperty> properties){
        if(properties == null || properties.size() == 0){
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(cypher.get() + " return ");
        for (CypherProperty property : properties) {
            builder.append(property.toCypherStr() + ",");
        }
        builder.delete(builder.length() - 1,builder.length());
        cypher.set(builder.toString());
    }

    public static void returnIdOf(CypherElement element){
        cypher.set(cypher.get() + " return " + "id(" + element.getName() + ")");
    }

    public static void returnIdOf(List<CypherElement> elements){
        if(elements == null || elements.size() == 0){
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(cypher.get() + " return ");
        for (CypherElement element : elements) {
            builder.append("id(" + element.getName() + "),");
        }
        builder.delete(builder.length() - 1,builder.length());
        cypher.set(builder.toString());
    }

    /**
     * 返回此次语句拼接的结果
     * TODO://到网上搜一下如何避免ThreadLocal使用的内存泄漏问题
     * @return
     */
    public static String getCypher(){
        String cypherStr = cypher.get();
        cypher.remove();   //每调用一次getCypher相当于一次拼接结束,为了防止内存泄漏,调用remove()方法
        return cypherStr;
    }

}
