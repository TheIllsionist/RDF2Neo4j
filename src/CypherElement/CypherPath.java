package CypherElement;

import java.util.LinkedList;
import java.util.List;

public class CypherPath extends CypherStr {  //TODO:

    private List<CypherElement> elements = null;  //这条Cypher路径上的元素,用链表链接

    public CypherPath(CypherNode sNode){
        elements = new LinkedList<>();
        elements.add(sNode);
    }

    public boolean connectThrough(CypherRelationship rel){
        return true;
    }


    @Override
    public String toCypherStr() {
        return null;
    }

    /**
     * 在Cypher中一条Path是没有可引用名的
     * @return
     */
    @Override
    public String referencedName() {
        return "";
    }

}
