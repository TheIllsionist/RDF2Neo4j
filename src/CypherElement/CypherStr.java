package CypherElement;

abstract class CypherStr implements ToCypher {

    protected String cypherFragment = null;  //将该对象转换为Cypher语句片段的结果
    protected boolean hasChanged = false;    //代表cypherFragment是否被修改过并且需要重新生成(为了运行效率和局部可重用性)

}
