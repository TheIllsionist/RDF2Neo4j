package CypherElement;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * CypherProperty代表Neo4j中的Node和Relationship上都可以有的Property
 * 每个CypherProperty对象必须都要有属性名且不为空,不为空串
 */
public class CypherProperty implements ToCypher{

    private String proName = null;  //该Property的名字

    public CypherProperty(String name){
        this.proName = name;
    }

    public String getProName() {
        return proName;
    }

    public void setProName(String proName) {
        this.proName = proName;
    }

    public String toString(){
        return proName;
    }

    @Override
    public String toCypherStr() {
        return toString();
    }

    public String toWhereStr(){
        return toWhereStr(null);
    }

    public String toWhereStr(CypherElement element){
        if(element == null){
            return this.toCypherStr();
        }
        return element.referencedName() + "." + toCypherStr();
    }

    @Override
    public String referencedName() {
        return proName;
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || this.getClass() != obj.getClass()){
            return false;
        }
        CypherProperty property = (CypherProperty)obj;
        return new EqualsBuilder().append(proName,property.proName).isEquals();
    }

}
