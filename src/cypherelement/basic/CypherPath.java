package cypherelement.basic;

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
        cypherFragment = sNode.toCypherStr();
    }


    /**
     * 通过..关系被..相连,要求输入一个左连接关系作为参数,当前CypherPath结尾的节点作为该关系的尾节点
     * @param leftRel
     * @return
     * @throws Exception
     */
    public CypherPath isConnectedThrough(CypherLeftRelationship leftRel) throws Exception{
        if(elements.getLast() instanceof CypherRelationship){
            throw new Exception("不合法的CypherPath:没有关系" + leftRel.getName() + "的尾节点!");
        }
        elements.add(leftRel);
        cypherFragment += leftRel.toCypherStr();
        return this;
    }

    /**
     * 通过..关系与..相连,当前CypherPath结尾的节点作为该关系的头节点
     * @param rightRel
     * @return
     * @throws Exception
     */
    public CypherPath connectThrough(CypherRightRelationship rightRel) throws Exception{
        if(elements.getLast() instanceof CypherRelationship){  //代表当前Path的链表的最后一个元素不是Node而是Relationship
            throw new Exception("不合法的CypherPath:没有关系" + rightRel.getName() + "的头节点!");
        }
        elements.add(rightRel);
        cypherFragment += rightRel.toCypherStr();
        return this;
    }

    /**
     * 与..节点相连,该节点可能作为头节点,也可能作为尾节点,要视程序逻辑而定
     * @param node
     * @return
     * @throws Exception
     */
    public CypherPath with(CypherNode node) throws Exception {
        if (elements.getLast() instanceof CypherNode) {
            throw new Exception("不合法的CypherPath:节点" + elements.getLast().getName() + "和节点" + node.getName() + "之间没有关系相连!");
        }
        elements.add(node);
        cypherFragment += node.toCypherStr();
        return this;
    }

    @Override
    protected String appendCypher() {
        return null;
    }

    @Override
    public String toCypherStr() {
        return cypherFragment;
    }

    /**
     * 在Cypher中一条Path是没有可引用名的
     * @return
     */
    @Override
    public String referenceName() {
        return "";
    }

}
