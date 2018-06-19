package CypherElement.Basic;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * CypherProperty代表Neo4j中的Node和Relationship上都可以有的Property
 * 每个CypherProperty对象必须都要有属性名且不为空
 */
public class CypherProperty implements ToCypher{

    private String proName = null;  //该Property的名字
    private CypherElement belongs = null;  //该Property所属于的Cypher元素(Node或Relationship)

    public CypherProperty(String name){
        this(name,null);
    }

    public CypherProperty(String name,CypherElement element){
        this.proName = name;
        this.belongs = element;
    }

    public String getProName() {
        return proName;
    }

    public CypherElement getBelongs(){
        return this.belongs;
    }

    public void setBelongs(CypherElement element){
        this.belongs = element;
    }

    /**
     * 返回该属性的属性名
     * @return
     */
    public String toString(){
        return proName;
    }

    /**
     * 将该属性转为Cypher语句片断
     * 如果该属性有所属Cypher元素,则返回"元素名.属性名",否则直接返回属性名
     * @return
     */
    @Override
    public String toCypherStr() {
        return belongs == null ? proName : belongs.getName() + "." + proName;
    }

    /**
     * 返回该属性在Cypher语句中的引用名,即该属性的属性名
     * @return
     */
    @Override
    public String referenceName() {
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
        if(belongs == null && property.belongs == null){  //两个所属属性都无引用,则只比较属性名是否相同
            return proName.equals(property.proName);
        }
        if(belongs != null && property.belongs != null){  //两个所属都有引用,则都比较
            return new EqualsBuilder().append(proName,property.proName).append(belongs,property.belongs).isEquals();
        }
        return false;  //一个有所属,一个没有所属,肯定不相等
    }

}
