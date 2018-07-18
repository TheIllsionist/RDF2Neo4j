package cypherelement.basic;

import org.apache.commons.lang.builder.EqualsBuilder;
import java.util.HashSet;
import java.util.Set;

/**
 * Cypher基本元素
 * 对应Neo4j的基本元素Node和Relationship,认为Cypher语句的基本元素为CypherNode和CypherRelationship
 */
public abstract class CypherElement extends CypherStr {

    protected String name = null;  //Cypher元素的名字
    protected Set<PropValPair> properties = null;  //该Cypher元素所拥有的属性值对集合
    protected String propsFragment = null;   //缓存属性值对集的拼接结果

    public CypherElement(){
        properties = new HashSet<>();
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
        this.cypherFragment = appendCypher();  //只有元素名称改变,不需要重新拼接属性值对
    }

    public Set<PropValPair> getProperties() {
        return properties;
    }

    public void setProperties(Set<PropValPair> properties) {
        this.properties = properties;
        this.propsFragment = propsToStr();
        this.cypherFragment = appendCypher();
    }

    /**
     * 给当前Cypher基本元素添加一个属性值对
     * @param pair
     */
    public void addCondition(PropValPair pair){
        int lastSize = properties.size();
        this.properties.add(pair);
        if(properties.size() > lastSize){  //属性值对真的有变化时再重新拼接
            this.propsFragment = propsToStr();
            this.cypherFragment = appendCypher();
        }
    }

    /**
     * 将某个属性值对从该Cypher基本元素中移除
     * @param pair
     */
    public void removeCondition(PropValPair pair){
        int lastSize = properties.size();
        this.properties.remove(pair);
        if(properties.size() < lastSize){  //属性值对真的有变化时再重新拼接
            this.propsFragment = propsToStr();
            this.cypherFragment = appendCypher();
        }
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
    public String referenceName(){
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
