package cypherelement.basic;

/**
 * Created by The Illsionist on 2018/7/25.
 */
public class LabelCondition extends CypherCondition{

    private String label;
    private CypherElement element;

    public LabelCondition(CypherElement element,String label){
        super(Operator.COLON);  //设置Label条件时,操作符就是冒号
        this.element = element;
        this.label = label;
        this.cypherFragment = appendCypher();
    }

    public CypherElement getElement(){
        return this.element;
    }

    public void setElement(CypherElement element){
        this.element = element;
        this.cypherFragment = appendCypher();
    }

    public String getLabel(){
        return this.label;
    }

    public void setLabel(String label){
        this.label = label;
        this.cypherFragment = appendCypher();
    }

    @Override
    protected String appendCypher() {
        return element.getName() + operator.toString() + label;
    }

    @Override
    public String referenceName() {
        return "";
    }

}
