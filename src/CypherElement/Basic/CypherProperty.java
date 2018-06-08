package CypherElement.Basic;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * CypherProperty代表Neo4j中的Node和Relationship上都可以有的Property
 * 每个CypherProperty对象必须都要有属性名且不为空
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

    /**
     * 获取该属性的属性名
     * @return
     */
    @Override
    public String toCypherStr() {
        return toString();
    }

    public String toWhereStr(){
        return toWhereStr(null);
    }

    /**
     * 有时在Cypher查询的Where子句中会指定某个Node或者Relationship的某个属性必须满足的条件
     * 此时就不能仅仅返回该属性的属性名,还要带上在Where条件中指定的该属性所属的Cypher元素
     * //TODO:此处的设计可能不是很合理,可能需要在后面使用过程中进行修改
     * @param element
     * @return
     */
    public String toWhereStr(CypherElement element){
        if(element == null || element.referencedName().matches("\\s*")){
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
