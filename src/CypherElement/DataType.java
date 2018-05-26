package CypherElement;

/**
 * Neo4j中Property取值的数据类型
 * 对 Date 和 DateTime 型的支持依靠固定格式的字符串比较
 */
public enum DataType {
    INT,  //整型
    DOUBLE,  //双精度型浮点数
    STR,  //字符串
}
