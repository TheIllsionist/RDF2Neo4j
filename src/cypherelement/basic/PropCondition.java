package cypherelement.basic;

/**
 * Cypher条件
 * 1.作为一个查询条件,此时它会被拼接在where子句中或者match子句中
 * 2.作为一个添加属性,此时它会被拼接在create子句中
 * 3.作为一次属性设置,此时被拼接在set子句中
 */
public abstract class PropCondition extends CypherCondition{

    protected CypherProperty accProp = null;  //受动属性


    public PropCondition(CypherProperty property, Operator operator){
        super(operator);
        this.accProp = property;
    }

    public CypherProperty getAccProp(){
        return accProp;
    }

    public void setAccProp(CypherProperty prop){
        this.accProp = prop;
        this.cypherFragment = appendCypher();
    }

    public void setOperator(Operator operator){
        super.setOperator(operator);
        this.cypherFragment = appendCypher();
    }

    @Override
    protected String appendCypher(){
        return this.accProp.toCypherStr() + operator.toString();
    }

}
