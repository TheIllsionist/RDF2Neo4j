package cypherelement.basic;

/**
 * 属性名对,可以作为Where子句的条件,也可以作为Set子句的参数
 */
public class PropPropPair extends CypherCondition{

    private CypherProperty appProp = null;  //施动属性

    public PropPropPair(CypherProperty prop1,CypherProperty prop2){
        this(prop1,Operator.EQ_TO,prop2);  //属性名对的默认操作符是相等
    }

    public PropPropPair(CypherProperty prop1,Operator operator,CypherProperty prop2){
        super(prop1,operator);
        this.appProp = prop2;
        this.cypherFragment = appendCypher();
    }

    public CypherProperty getAppProp(){
        return this.appProp;
    }

    public void setAppProp(CypherProperty property){
        this.appProp = property;
        this.cypherFragment = appendCypher();
    }

    @Override
    protected String appendCypher(){
        return super.appendCypher() + " " + appProp.toCypherStr();
    }

    @Override
    public String referenceName() {
        return "";
    }

}
