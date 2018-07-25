package cypherelement.basic;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * 属性值对,为了使用方便,将Cypher属性,Cypher值和Cypher操作符组合为属性值对类
 * PropValPair既可以转换为Match子句或Create子句的内置Cypher片段,也可以作为Where子句或Set子句的片段
 */
public class PropValPair extends CypherCondition {

    private CypherValue value = null;

    public PropValPair(CypherProperty property,CypherValue value){
        this(property,Operator.COLON,value);  //默认操作符是冒号,Neo4j内置
    }

    public PropValPair(CypherProperty property, Operator operator, CypherValue value){
        super(property,operator);
        this.value = value;
        this.cypherFragment = appendCypher();
    }

    public CypherValue getValue() {
        return value;
    }

    public void setValue(CypherValue value) {
        this.value = value;
        this.cypherFragment = appendCypher();
    }


    @Override
    protected String appendCypher(){
        return super.appendCypher() + value.toCypherStr();
    }

    /**
     * 该对象转换为Match子句或Create子句的内置Cypher片段
     * 此方法未使用cypherFragment作为语句缓存
     * @return
     */
    public String toInnerString(){
        return accProp.toCypherStr() + ":" + value.toCypherStr();
    }

    /**
     * 一对属性值对在Cypher语句中被提及时往往是通过提及属性名来代表
     * @return
     */
    @Override
    public String referenceName() {
        return "";
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || this.getClass() != obj.getClass()){
            return false;
        }
        PropValPair pair = (PropValPair)obj;
        return new EqualsBuilder()
                .append(accProp,pair.accProp)
                .append(operator,pair.operator)
                .append(value,pair.value)
                .isEquals();
    }

}
