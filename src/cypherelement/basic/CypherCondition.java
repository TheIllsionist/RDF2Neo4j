package cypherelement.basic;

/**
 * Cypher条件
 * 1.作为一个查询条件,此时它会被拼接在where子句中或者match子句中
 * 2.作为一个添加属性,此时它会被拼接在create子句中
 * 3.作为一次属性设置,此时被拼接在set子句中
 */
public abstract class CypherCondition extends CypherStr{

    protected CypherProperty accProp = null;  //受动属性
    protected Operator operator = null;    //属性受动方式,即操作符


    public CypherCondition(CypherProperty property,Operator operator){
        this.accProp = property;
        this.operator = operator;
    }

    public CypherProperty getAccProp(){
        return accProp;
    }

    public void setAccProp(CypherProperty prop){
        this.accProp = prop;
        this.cypherFragment = appendCypher();
    }

    public Operator getOperator(){
        return operator;
    }

    public void setOperator(Operator operator){
        this.operator = operator;
        this.cypherFragment = appendCypher();
    }

    @Override
    protected String appendCypher(){
            return this.accProp.toCypherStr() + operator.toString();
    }

}
