package cypherelement.basic;

abstract class CypherStr implements ToCypher {

    protected String cypherFragment = null;//上次该对象转为Cypher语句片段的结果,从上次转换到此次使用间没有发生修改时可以复用

    protected abstract String appendCypher();  //将对象元素拼接为Cypher语句字符串并返回

    public String toCypherStr(){
        return cypherFragment;
    }

}
