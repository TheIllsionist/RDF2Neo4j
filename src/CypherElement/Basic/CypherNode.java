package CypherElement.Basic;

import org.apache.commons.lang.builder.EqualsBuilder;
import java.util.Set;

/**
 * 可以被转化为表示Neo4j中Node的Cypher语句片段的对象
 */
public class CypherNode extends CypherElement{

    private String label = null;   //此Neo4j节点的Label

    /**
     * 新建一个CypherNode,什么都没有
     */
    public CypherNode() {
        super();
        this.cypherFragment = appendCypher();
    }

    /**
     * 新建一个CypherNode对象,只有节点名称
     * @param nodeName
     */
    public CypherNode(String nodeName){
        this(nodeName,null,null);
    }

    /**
     * 新建一个CypherNode对象,只有Label和属性值对集合
     * @param label
     * @param properties
     */
    public CypherNode(String label,Set<PropValPair> properties){
        this(null,label,properties);
    }

    /**
     * 新建一个CypherNode对象,具有名称,Label和属性值对集合
     * @param name
     * @param label
     * @param properties
     */
    public CypherNode(String name,String label,Set<PropValPair> properties){
        super(name,properties);
        this.label = label;
        this.propsFragment = propsToStr();
        this.cypherFragment = appendCypher();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        this.cypherFragment = appendCypher(); //只修改Label,不需要重新拼接属性值对
    }

    @Override
    protected String appendCypher() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        if(name != null && !name.matches("\\s*")){
            builder.append(name);
        }
        if(label != null && label.matches("\\s*")){
            builder.append(":").append(label);
        }
        builder.append("" + propsFragment);
        builder.append(")");
        return builder.toString();
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        CypherNode newNode = (CypherNode)obj;
        return new EqualsBuilder()
                .append(name,newNode.name)
                .append(label,newNode.label)
                .append(properties,newNode.properties)
                .isEquals();
    }

}
