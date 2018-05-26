package CypherElement.Basic;

import org.apache.commons.lang.builder.EqualsBuilder;

public class PropValPair extends CypherStr {

    private CypherProperty property = null;
    private Operator operator = null;
    private CypherValue value = null;


    public PropValPair(CypherProperty property,CypherValue value){
        this(property,Operator.EQ_TO,value);  //默认操作符是相等
    }

    public PropValPair(CypherProperty property, Operator operator, CypherValue value){
        this.property = property;
        this.operator = operator;
        this.value = value;
        this.hasChanged = true;  //实例新建时需要第一次拼接Cypher片段
    }

    public String toString(){
        if(!hasChanged){
            return cypherFragment;
        }
        cypherFragment = property.toString() + ":" + value.toString();
        hasChanged = false;
        return cypherFragment;
    }

    @Override
    public String toCypherStr() {
        if(!hasChanged){
            return cypherFragment;
        }
        cypherFragment = property.toCypherStr() + ":" + value.toCypherStr();
        hasChanged = false;
        return cypherFragment;
    }

    /**
     * 一对属性值对在Cypher语句中被提及时往往是通过只提及属性名来代表
     * @return
     */
    @Override
    public String referencedName() {
        return property.getProName();
    }


    public String toWhereStr() throws Exception{
        return toWhereStr(null);
    }

    /**
     * 该方法将当前属性值对转换为一个合法的Cypher语句片段返回,该片段属于某个Cypher的Where子句中的一个限制条件
     * @param var 表明作为限制条件的该属性值对所限制的目标对象是谁
     * @return
     * @throws Exception
     */
    public String toWhereStr(CypherElement element) throws Exception {
        //加入一部分不合法的Cypher语法检查(几种操作符和属性值的不匹配情况)
        if(operator == Operator.IN && value.getValFormat() == 0){
            throw new Exception("非列表型属性值不能使用IN运算符!");
        }
        if(operator == Operator.MATCH || operator == Operator.STARTS_WITH
                || operator == Operator.ENDS_WITH || operator == Operator.CONTAINS){
            if(value.getDataType() != DataType.STR){
                throw new Exception("MATCH,STARTS_WITH,ENDS_WITH,CONTAINS运算符只能用在String上!");
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(property.toWhereStr(element)).append(operator.toString()).append(value.toCypherStr());
        return builder.toString();
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
                .append(property,pair.property)
                .append(operator,pair.operator)
                .append(value,pair.value)
                .isEquals();
    }

}
