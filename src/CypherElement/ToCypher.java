package CypherElement;

/**
 * 实现此接口的类都必须可以被转化为合法的Cypher查询语句片段
 */
public interface ToCypher {

    /**
     * 将该ToCypher对象表示为合法的Cypher语句片段,返回其字符串形式
     * @return
     */
    String toCypherStr();

    /**
     * 返回该ToCypher对象在Cypher语句中被引用时的引用名
     * @return
     */
    String referencedName();

}
