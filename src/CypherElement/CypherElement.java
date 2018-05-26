package CypherElement;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.Set;

/**
 * Cypher基本元素,对应Neo4j的基本元素Node和Relationship,认为Cypher语句的基本元素为CypherNode和CypherRelationship
 */
abstract class CypherElement extends CypherStr {

    protected String name = null;  //Cypher元素的名字
    protected Set<PropValPair> properties = null;  //该Cypher元素所拥有的属性值对集合

    public CypherElement(){
    }

    public CypherElement(String name,Set<PropValPair> properties){
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.hasChanged = true;
    }

    public Set<PropValPair> getProperties() {
        return properties;
    }

    public void setProperties(Set<PropValPair> properties) {
        this.properties = properties;
        this.hasChanged = true;
    }

    /**
     * 只将属性值对集合转换为合法Cypher语句
     * @return
     */
    protected String propsToStr(){
        if(properties == null || properties.size() == 0){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for(PropValPair pair:properties){
            builder.append(pair.toCypherStr()).append(",");
        }
        builder.delete(builder.length() - 1,builder.length());
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String referencedName(){
        return name;
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        CypherElement newElement = (CypherElement)obj;
        return new EqualsBuilder()
                .append(name,newElement.name)
                .append(properties,newElement.properties)
                .isEquals();
    }

}
