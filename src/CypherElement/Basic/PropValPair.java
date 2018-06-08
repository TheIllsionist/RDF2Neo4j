package CypherElement.Basic;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * 属性值对,为了使用方便将Cypher属性,Cypher值和Cypher操作符组合为属性值对类
 */
public class PropValPair extends CypherStr {

    private CypherProperty property = null;
    private Operator operator = null;
    private CypherValue value = null;


//    private CypherElement belongs = null;   //该属性值对所属的CypherElement
//    /**
//     * 设置该属性值对的所属Cypher元素
//     * TODO://注!此方法只能此包内调用,其实只能由此对象的所属对象调用
//     */
//    void setBelongs(CypherElement element){
//        this.belongs = element;
//    }


    public PropValPair(CypherProperty property,CypherValue value){
        this(property,Operator.EQ_TO,value);  //默认操作符是相等
    }

    public PropValPair(CypherProperty property, Operator operator, CypherValue value){
        this.property = property;
        this.operator = operator;
        this.value = value;
//        this.value.setBelongs(this);  //设置值的所属为本对象
        hasChanged();
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
        hasChanged();
    }

    public CypherValue getValue() {
        return value;
    }

    public void setValue(CypherValue value) {
        this.value = value;
//        this.value.setBelongs(this);
        hasChanged();
    }


    private void hasChanged(){
        this.hasChanged = true;
//        if(this.belongs != null){
//            this.belongs.hasChanged();
//        }
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
     * 该方法将当前属性值对转换为一个合法的Cypher语句片段返回,该片段属于某个Cypher查询的Where子句中的一个限制条件
     * @param element 限制条件所限制的目标对象是谁
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
