package cypherelement.basic;

/**
 * Neo4j中Property取值的数据类型
 * 通过固定格式的字符串支持 Date 和 Datetime数据类型
 */
public enum DataType {
    INT,  //整型
    DOUBLE,  //双精度型浮点数
    STR,  //字符串
    //2018-06-08   //日期型
    //2018-06-08 20:13:07  //时间型
}
