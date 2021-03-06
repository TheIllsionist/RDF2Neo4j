package CypherElement.Basic;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Cypher基本元素,对应Neo4j的基本元素Node和Relationship,认为Cypher语句的基本元素为CypherNode和CypherRelationship
 *
 */
abstract class CypherElement extends CypherStr {

    protected String name = null;  //Cypher元素的名字
    protected Set<PropValPair> properties = null;  //该Cypher元素所拥有的属性值对集合

//    protected CypherPath belongs = null;
//    protected void setBelongs(CypherPath path){
//        this.belongs = path;
//    }

    public CypherElement(){
        properties = new HashSet<>();
    }

    public CypherElement(String name,Set<PropValPair> properties){
        this.name = name;
        this.properties = properties;
//        for (PropValPair pair : properties) {
//            pair.setBelongs(this);
//        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        hasChanged();
    }

    public Set<PropValPair> getProperties() {
        return properties;
    }

    public void setProperties(Set<PropValPair> properties) {
        this.properties = properties;
//        for (PropValPair pair : properties) {
//            pair.setBelongs(this);
//        }
        hasChanged();
    }

    protected void hasChanged(){
        this.hasChanged = true;
//        if(belongs != null){
//            belongs.hasChanged();
//        }
    }

    /**
     * 给当前Cypher基本元素添加一个属性值对
     * @param pair
     */
    protected void addPair(PropValPair pair){
        int lastSize = properties.size();
        this.properties.add(pair);
        if(properties.size() > lastSize){  //如果真的添加了(因为Set的不重复性),才标志修改
            hasChanged();
        }
    }

    /**
     * 将某个属性值对从该Cypher基本元素中移除
     * @param pair
     */
    protected void removePair(PropValPair pair){
        this.properties.remove(pair);
        hasChanged();
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
