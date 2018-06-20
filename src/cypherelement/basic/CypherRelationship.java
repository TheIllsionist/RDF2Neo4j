package cypherelement.basic;

import org.apache.commons.lang.builder.EqualsBuilder;
import java.util.Set;

public class CypherRelationship extends CypherElement{

    private String type = null;
    private int minDepth = 1;  //默认情况下最小查询深度为1
    private int maxDepth = 1;  //默认情况下最大查询深度为1

    /**
     * 新建一个关系,没有其他可用信息
     */
    public CypherRelationship(){
        super();
        this.cypherFragment = appendCypher();
    }
    /**
     * 新建一个关系,只声明它的Type,查询深度取默认值,其他为空
     * @param type
     */
    public CypherRelationship(String type){
        this(null,type,1,1,null);
    }
    /**
     * 新建一个关系,声明它的Type,最小深度,最大深度,其他信息为空
     * @param type
     * @param minDepth
     * @param maxDepth
     */
    public CypherRelationship(String type,int minDepth,int maxDepth){
        this(null,type,minDepth,maxDepth,null);
    }
    /**
     * 新建一个关系,声明Type,最小深度
     * 意味着不限最大深度,会默认将最大深度设置为Integer.MAX_VALUE,其他信息为空
     * @param type
     * @param minDepth
     */
    public CypherRelationship(String type,int minDepth){
        this(null,type,minDepth,Integer.MAX_VALUE,null);
    }

    /**
     * 新建一个关系,声明关系的Type和所包含的属性值对集合,深度取默认值,其他信息为空
     * @param type
     * @param properties
     */
    public CypherRelationship(String type,Set<PropValPair> properties){
        this(null,type,1,1,properties);
    }

    /**
     * 创建一个关系对象,所有信息都由程序员指定
     * @param name &nbsp 该Relationship在Cypher中其他位置被引用的引用名
     * @param type &nbsp 该Relationship的Type
     * @param minDepth &nbsp 该Relationship关系的最小深度
     * @param maxDepth &nbsp 该Relationship关系的最大深度
     * @param properties &nbsp 该Relationship关系所含有的属性值对集合
     */
    public CypherRelationship(String name,String type,int minDepth,int maxDepth,Set<PropValPair> properties){
        super(name,properties);
        this.type = type;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.propsFragment = propsToStr();
        this.cypherFragment = appendCypher();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.cypherFragment = appendCypher();
    }

    public int getMinDepth() {
        return minDepth;
    }

    public void setMinDepth(int minDepth) {
        this.minDepth = minDepth;
        this.cypherFragment = appendCypher();
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        this.cypherFragment = appendCypher();
    }

    @Override
    protected String appendCypher() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        //指定关系的引用名
        if(name != null && !name.matches("\\s*")){
            builder.append(name);
        }
        if(type != null && !type.matches("\\s*")){
            builder.append(":").append(type);
        }
        builder.append("*");
        if(minDepth != maxDepth){
            builder.append(minDepth).append("..");  //设置最小层数
            if(maxDepth != Integer.MAX_VALUE){ //当最大层数为Integer.MAX_VALUE时,表明没有层数限制
                builder.append(maxDepth);
            }
        }else {
            builder.append(minDepth);
        }
        builder.append(" " + propsFragment);
        builder.append("]");
        return builder.toString();
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        CypherRelationship newRel = (CypherRelationship)obj;
        return new EqualsBuilder()
                .append(name,newRel.name)
                .append(type,newRel.type)
                .append(minDepth,newRel.minDepth)
                .append(maxDepth,newRel.maxDepth)
                .append(properties,newRel.properties)
                .isEquals();
    }
}
