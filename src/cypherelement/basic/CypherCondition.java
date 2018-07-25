package cypherelement.basic;

/**
 * Created by The Illsionist on 2018/7/25.
 */
public abstract class CypherCondition extends CypherStr{

    protected Operator operator = null;   //操作符

    protected CypherCondition(Operator operator){
        this.operator = operator;
    }

    public Operator getOperator(){
        return operator;
    }

    protected void setOperator(Operator operator){
        this.operator = operator;
    }

}
