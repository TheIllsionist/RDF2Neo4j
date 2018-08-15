package rdfImporter.abs.cypherAbs;

import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import rdfImporter.PropImporter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The Illsionist on 2018/8/13.
 */
public abstract class AbsCypherPropImporter implements PropImporter {

    /**
     * 属性及其关系缓存
     * 在知识库中属性的数目是有限的,因此属性缓存缓存全部的属性及其之间的关系
     * 属性之间主要有四种关系：subPropertyOf,equivalentProperty,disjointProperty,inverseOf
     * 当前缓存基于：{ 实例封闭 + 将线程安全性委托给现有线程安全类 + 特殊的线程读写方式 } 实现线程安全性
     * 只有一个线程写属性缓存,多个线程读属性缓存
     * 多个线程读/写关系,但每个线程所写关系的种类不同,一个线程只写某种特定的关系,每种特定的关系也只有一个线程写
     * 在缓存中用一个整型变量表征两个属性之间的关系：1-subPropertyOf,2-equivalentProperty,3-disjointProperty,4-inverseOf
     * 两个属性间的4种关系是互斥存在的,即假如属性A是属性B的子属性,则属性A与属性B之间不会再有其他关系
     */
    @ThreadSafe //只有在特定的使用该缓存的方式下才满足线程安全性
    protected static class CacheProperty{

        private final static int DEFAULT_CAPACITY = 5460;  //TODO:默认初始容量的选择还有待调研

        /** 利用静态初始化器保证对象引用的可见性,当前实现为只缓存每个属性的preLabel,当前初始容量默认为5460 **/
        private final static ConcurrentHashMap<String,ConcurrentHashMap<String,Integer>> propWithRels = new ConcurrentHashMap<>(84);

        //TODO:此时加载知识库中的内容到内存中是一个合适的时机吗?从性能上考虑
        static {  //类加载时即查询知识库中已有的属性
            StatementResult res = Neo4jConnection.getSession().run("match(p:OWL_DATATYPEPROPERTY)" +
                    " optional match(p)-[r:RDFS_SUBPROPERTYOF|:EQUIVALENT_PROPERTY|:DISJOINT_PROPERTY|:INVERSE_PROPERTY]->(anop:OWL_DATATYPEPROPERTY) " +
                    "return p.preLabel as p,case r.preLabel when \"rdfs:subPropertyOf\" then 1 when \"owl:equivalentProperty\" then 2 " +
                    "when \"owl:propertyDisjointWith\" then 3 when \"owl:inverseOf\" then 4 end as tag,anop.preLabel as anop union " +
                    "match(p:OWL_OBJECTPROPERTY) optional match(p)-[r:RDFS_SUBPROPERTYOF|:EQUIVALENT_PROPERTY|:DISJOINT_PROPERTY|:INVERSE_PROPERTY]->(anop:OWL_OBJECTPROPERTY) " +
                    "return p.preLabel as p,case r.preLabel when \"rdfs:subPropertyOf\" then 1 when \"owl:equivalentProperty\" then 2 " +
                    "when \"owl:propertyDisjointWith\" then 3 when \"owl:inverseOf\" then 4 end as tag,anop.preLabel as anop ");
            Record rec = null;
            while (res.hasNext()){
                rec = res.next();
                String proPre = rec.get(0).asString();
                if(proPre == null||proPre.equals("null"))  //防止第1个参数为空的意外情况出现
                    continue;
                if(!propWithRels.containsKey(proPre)){  //保证了第1个键的值不会被延迟初始化,避免"先检查后执行"竞态条件发生
                    propWithRels.put(proPre,new ConcurrentHashMap<>());
                }
                String anoProPre = rec.get(2).asString();
                if(anoProPre == null||anoProPre.equals("null"))
                    continue;
                propWithRels.get(proPre).put(anoProPre,rec.get(1).asInt());
            }
        }

        /**
         * 判断某个属性是否早已被写入知识库
         * @param preLabel &nbsp 唯一标识该属性的preLabel(这只是目前的评价指标)
         * @return true表示该属性已存在于知识库中,false表示该属性未存在于知识库中
         * 注:因写知识库和写缓存不在一个原子操作内,所以可能出现误判,但程序实现逻辑容忍未存在误判
         */
        public static boolean isPropertyContained(String preLabel){
            return propWithRels.containsKey(preLabel);
        }

        /**
         * 往缓存中写入新属性,缓存写入紧接着知识库写入并且一定要在知识库写入之后(禁止指令重排序)
         * 因为只有一个线程写属性缓存,所以知识库的写入和缓存的写入可以不在一个原子操作内,这也是造成误判的原因
         * @param preLabel &nbsp 唯一标识该类的preLabel
         */
        public static void addProperty(String preLabel){
            propWithRels.put(preLabel,new ConcurrentHashMap<>());
        }

        /**
         * 判断某两个属性之间的关系(这里的关系是具有先序性的)是否早已存在于知识库中
         * @param fPre 先序属性
         * @param lPre 后序属性
         * @return true 如果两个属性之间的关系已被写入知识库
         * 注:这个方法调用之前会必须先调用两次isPropertyContained方法判断两个属性是否都被写入知识库中了
         */
        public static boolean isRelExisted(String fPre,String lPre){
            return propWithRels.get(fPre).get(lPre) != null;
        }

        /**
         * 将一个新加入知识库的关系写入缓存
         * @param fPre 先序属性
         * @param lPre 后序属性
         * @param tag 关系种类标签
         */
        public static void addRelation(String fPre,String lPre,int tag){
            propWithRels.get(fPre).put(lPre,tag);
        }
    }
}
