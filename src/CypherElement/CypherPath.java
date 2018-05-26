package CypherElement;

import java.util.LinkedList;

public class CypherPath extends CypherStr {

    private LinkedList<CypherElement> elements = null;  //这条Cypher路径上的元素,用链表链接

    /**
     * 构造函数,要构造一个CypherPath必须要以一个CypherNode开头
     * @param sNode
     */
    public CypherPath(CypherNode sNode){
        elements = new LinkedList<>();
        elements.add(sNode);
        this.hasChanged = true;   //构造实例时第一次拼接
    }

    /**
     * 通过..关系被..相连,当前CypherPath结尾的节点作为该关系的尾节点
     * @param leftRel
     * @return
     * @throws Exception
     */
    public boolean isConnectedThrough(CypherLeftRelationship leftRel) throws Exception{
        if(elements.getLast() instanceof CypherRelationship){
            throw new Exception("不合法的CypherPath:没有关系" + leftRel.getName() + "的尾节点!");
        }
        elements.add(leftRel);
        this.hasChanged = true;
        return true;
    }

    /**
     * 通过..关系与..相连,当前CypherPath结尾的节点作为该关系的头节点
     * @param rightRel
     * @return
     * @throws Exception
     */
    public boolean connectThrough(CypherRightRelationship rightRel) throws Exception{
        if(elements.getLast() instanceof CypherRelationship){  //代表当前Path的链表的最后一个元素不是Node而是Relationship
            throw new Exception("不合法的CypherPath:没有关系" + rightRel.getName() + "的头节点!");
        }
        elements.add(rightRel);
        this.hasChanged = true;
        return true;
    }

    /**
     * 与..节点相连,该节点可能作为头节点,也可能作为尾节点,要视程序逻辑而定
     * @param node
     * @return
     * @throws Exception
     */
    public boolean with(CypherNode node) throws Exception {
        if (elements.getLast() instanceof CypherNode) {
            throw new Exception("不合法的CypherPath:节点" + elements.getLast().getName() + "和节点" + node.getName() + "之间没有关系相连!");
        }
        elements.add(node);
        this.hasChanged = true;
        return true;
    }

    @Override
    public String toCypherStr() {
        if(!hasChanged){
            return cypherFragment;
        }
        StringBuilder builder = new StringBuilder();
        for (CypherElement element:elements) {
            builder.append(element.toCypherStr());
        }
        cypherFragment = builder.toString();
        hasChanged = false;
        return cypherFragment;
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
