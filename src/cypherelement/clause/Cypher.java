package cypherelement.clause;

import concurrentannotation.NotThreadSafe;
import cypherelement.basic.*;
import java.util.List;
import java.util.Set;

/**
 * Cypher语句拼接工具
 * 注:该类只提供语句拼接作用,没有任何语法检查功能,使用该拼接工具的程序员必须要有一定的Cypher语法基础
 */
@NotThreadSafe
public class Cypher{

    private final StringBuilder cypher;

    public Cypher(){
        cypher = new StringBuilder();
        cypher.append("");
    }


    public Cypher match(String matchClause){
        cypher.append("match" + matchClause);
        return this;
    }

    /**
     * 拼接查询一个Neo4j节点的Cypher语句
     * @param node
     * @return this
     */
    public Cypher match(CypherNode node){
        cypher.append("match" + node.toCypherStr());
        return this;
    }

    /**
     * 拼接查询一个Neo4j路径的Cypher语句
     * @param path
     * @return this
     */
    public Cypher match(CypherPath path){
        cypher.append("match").append(path.toCypherStr());
        return this;
    }

    /**
     * 拼接查询多个Neo4j结点的Cypher语句
     * @param nodes
     * @return
     */
    public Cypher match(List<CypherNode> nodes){
        cypher.append("match");
        for(CypherNode node : nodes){
            cypher.append(node.toCypherStr() + ",");
        }
        cypher.delete(cypher.length() - 1,cypher.length());
        return this;
    }

//    /**
//     * 拼接查询多个Neo4j路径的Cypher语句
//     * @param paths
//     * @return this
//     */
//    public Cypher match(List<CypherPath> paths){
//        cypher.append("match");
//        for(CypherPath path:paths){
//            cypher.append(path.toCypherStr() + ",");
//        }
//        cypher.delete(cypher.length() - 1,cypher.length());
//        return this;
//    }

    public Cypher create(String createClause){
        cypher.append("create" + createClause);
        return this;
    }

    /**
     * 拼接创建一个Neo4j节点的Cypher语句
     * @param node
     * @return this
     */
    public Cypher create(CypherNode node){
        cypher.append("create" + node.toCypherStr());
        return this;
    }

    /**
     * 拼接创建一个Neo4j路径的Cypher语句
     * @param path
     */
    public Cypher create(CypherPath path){
        cypher.append("create").append(path.toCypherStr());
        return this;
    }

    /**
     * 拼接创建多个Neo4j路径的Cypher语句
     * @param paths
     */
    public Cypher create(List<CypherPath> paths){
        cypher.append("create");
        for(CypherPath path:paths){
            cypher.append(path.toCypherStr() + ",");
        }
        cypher.delete(cypher.length() - 1,cypher.length());
        return this;
    }

    public Cypher merge(String createClause){
        cypher.append("merge" + createClause);
        return this;
    }

    /**
     * 拼接创建一个Neo4j节点的Cypher语句
     * @param node
     * @return this
     */
    public Cypher merge(CypherNode node){
        cypher.append("merge" + node.toCypherStr());
        return this;
    }

    /**
     * 拼接创建一个Neo4j路径的Cypher语句
     * @param path
     */
    public Cypher merge(CypherPath path){
        cypher.append("merge").append(path.toCypherStr());
        return this;
    }

    /**
     * 拼接创建多个Neo4j路径的Cypher语句
     * @param paths
     */
    public Cypher merge(List<CypherPath> paths){
        cypher.append("merge");
        for(CypherPath path:paths){
            cypher.append(path.toCypherStr() + ",");
        }
        cypher.delete(cypher.length() - 1,cypher.length());
        return this;
    }

    public Cypher where(String whereClause){
        cypher.append(" where " + whereClause);
        return this;
    }

    /**
     * 为当前Cypher语句添加Where子句和1个条件
     * @param condition
     */
    public Cypher where(PropCondition condition){
        cypher.append(" where " + condition.toCypherStr());
        return this;
    }

    /**
     * 为当前Cypher语句添加Where子句和多个条件,多个条件之间用 'and '连接
     * @param conditions
     */
    public Cypher where(Set<PropCondition> conditions){
        if(conditions == null || conditions.size() == 0)
            return this;
        cypher.append(" where ( ");
        for(PropCondition condition : conditions){
            cypher.append(condition.toCypherStr() + " and");
        }
        cypher.delete(cypher.length() - 3,cypher.length());
        cypher.append(")");
        return this;
    }

    public Cypher and(String newCondition){
        cypher.append(" and " + newCondition);
        return this;
    }

    /**
     * 用and连接符为Where子句添加1条件
     * @param condition
     */
    public Cypher and(PropCondition condition){
        cypher.append(" and " + condition.toCypherStr());
        return this;
    }

    /**
     * 用and连接符为Where子句添加多个条件,内部的多个条件默认使用' and '连接
     * @param conditions
     */
    public Cypher and(Set<PropCondition> conditions){
        if(conditions == null || conditions.size() == 0)
            return this;
        cypher.append(" and ( ");
        for(PropCondition condition : conditions){
            cypher.append(condition.toCypherStr() + " and");
        }
        cypher.delete(cypher.length() - 3,cypher.length());
        cypher.append(")");
        return this;
    }

    public Cypher or(String newCondition){
        cypher.append(" or " + newCondition);
        return this;
    }

    public Cypher or(PropCondition condition){
        cypher.append(" or " + condition.toCypherStr());
        return this;
    }

    public Cypher or(Set<PropCondition> conditions){
        if(conditions == null || conditions.size() == 0){
            return this;
        }
        cypher.append(" or ( ");
        for(PropCondition condition : conditions){
            cypher.append(condition.toCypherStr() + " and");
        }
        cypher.delete(cypher.length() - 3,cypher.length());
        cypher.append(")");
        return this;
    }

    public Cypher set(String newCondition){
        cypher.append(" set " + newCondition);
        return this;
    }

    /**
     * 为Set子句设置1个表达式
     * @param condition
     */
    public Cypher set(CypherCondition condition){
        cypher.append(" set " + condition.toCypherStr());
        return this;
    }

    /**
     * 为Set子句设置多个表达式,表达式之间使用' , '隔开
     * @param conditions
     */
    public Cypher set(Set<CypherCondition> conditions){
        if(conditions == null || conditions.size() == 0){
            return this;
        }
        cypher.append(" set ");
        for(CypherCondition condition : conditions){
            cypher.append(condition.toCypherStr() + ",");
        }
        cypher.delete(cypher.length() - 1,cypher.length());
        return this;
    }

    public Cypher wantReturn(String returnClause){
        cypher.append(" return " + returnClause);
        return this;
    }

    public Cypher wantReturn(CypherProperty property){
        cypher.append(" return " + property.toCypherStr());
        return this;
    }

    public Cypher wantReturn(List<CypherProperty> properties){
        if(properties == null || properties.size() == 0){
            return this;
        }
        cypher.append(" return ");
        for(CypherProperty property : properties){
            cypher.append(property.toCypherStr() + ",");
        }
        cypher.delete(cypher.length() - 1,cypher.length());
        return this;
    }

    public Cypher returnIdOf(CypherElement element){
        cypher.append(" return id(" + element.getName() + ")");
        return this;
    }

    public Cypher returnIdOf(List<CypherElement> elements){
        if(elements == null || elements.size() == 0){
            return this;
        }
        cypher.append(" return ");
        for(CypherElement element : elements){
            cypher.append("id(" + element.toCypherStr() + "),");
        }
        cypher.delete(cypher.length() - 1,cypher.length());
        return this;
    }

    /**
     * 返回此次语句拼接的结果
     * @return
     */
    public String getCypher(){
        String cypherStr = cypher.toString();
        cypher.delete(0,cypher.length());  //返回拼接结果之后即清空builder缓存,使Cypher对象可以作为对象成员使用
        return cypherStr;
    }

}
