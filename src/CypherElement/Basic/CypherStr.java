package CypherElement.Basic;

abstract class CypherStr implements ToCypher {

    protected String cypherFragment = null;//上次该对象转为Cypher语句片段的结果,从上次转换到此次使用间没有发生修改时可以复用

    protected boolean hasChanged = false;  //表征cypherFragment是否可以复用的标志

}
