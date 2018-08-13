package rdfImporter.abs.cypherAbs;

import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import rdfImporter.ClassImporter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The Illsionist on 2018/8/13.
 */
public abstract class AbsCypherClassImporter implements ClassImporter{

    /**
     * 类及其关系缓存
     * 在知识库中类的数目是有限的,允许类及其关系缓存缓存全部的类及其之间的关系
     * 类之间主要有三种语义关系：subClassOf,equivalentClass,disjointClass
     * 当前缓存基于：{ 实例封闭 + 将线程安全性委托给现有线程安全类 + 特殊的线程读写方式 } 实现线程安全性
     * 只有一个线程写类缓存,多个线程读类缓存
     * 多个线程读/写关系,但每个线程所写关系的种类不同,一个线程只写某种特定的关系,每种特定的关系也只有一个线程写
     * 在缓存中用一个整型变量表征两个类之间的关系：1-subClassOf,2-equivalentClass,3-disjointClass
     * 两个类间的3种关系是互斥存在的,即假如类A是类B的子类,则类A与类B之间不会再有其他关系
     */
    @ThreadSafe  //只有在特定的使用该缓存的方式下才满足线程安全性
    protected static class CacheClass{

        private final static int DEFAULT_CAPACITY = 682;  //TODO:默认初始容量的选择还有待调研

        /** 保证对象引用的可见性与不可变性,当前实现为只缓存每个类的preLabel,当前初始容量默认为682 **/
        private final static ConcurrentHashMap<String,ConcurrentHashMap<String,Integer>> classWithRels = new ConcurrentHashMap<>(170);

        //TODO:此时加载知识库中的内容到内存中是一个合适的时机吗?从性能上考虑
        static {  //类加载时即查询知识库中的已有类和已有关系
            StatementResult res = Neo4jConnection.getSession().run("match(cls:OWL_CLASS) " +
                    "optional match(cls)-[r:RDFS_SUBCLASSOF|EQUIVALENT_CLASS|DISJOINT_CLASS]->(anCls:OWL_CLASS) " +
                    "return cls.preLabel as cls, case r.preLabel when \"rdfs:subClassOf\" then 1 when \"owl:equivalentClass\" then 2 " +
                    " when \"owl:disjointWith\" then 3 end as index,anCls.preLabel as anoCls");
            Record rec = null;
            while (res.hasNext()){
                rec = res.next();
                String clsPre = rec.get(0).asString();
                if(clsPre == null){  //防止第1个参数为空的意外情况出现
                    continue;
                }
                if(!classWithRels.contains(clsPre)){  //保证了第1个键的值不会被延迟初始化,避免"先检查后执行"竞态条件发生
                    classWithRels.put(clsPre,new ConcurrentHashMap<>());
                }
                String anoClsPre = rec.get(2).asString();
                if(anoClsPre == null)
                    continue;
                classWithRels.get(clsPre).put(anoClsPre,rec.get(1).asInt());
            }
        }

        /**
         * 判断某个类是否早已被写入知识库
         * @param preLabel &nbsp 唯一标识该类的preLabel(这只是目前的评价指标)
         * @return true表示该类已存在于知识库中,false表示该类未存在于知识库中
         * 注:因写知识库和写缓存不在一个原子操作内,所以可能出现误判,但程序实现逻辑容忍误判
         */
        public static boolean isClassContained(String preLabel){
            return classWithRels.containsKey(preLabel);
        }

        /**
         * 往缓存中写入新类,缓存写入紧接着知识库写入并且一定要在知识库写入之后(禁止指令重排序)
         * TODO:在只有一个线程写类缓存的情况下,知识库的写入和缓存的写入可以不在一个原子操作内,这也是造成误判的原因(注意,程序实现逻辑容忍误判)
         * @param preLabel &nbsp 唯一标识该类的preLabel
         */
        public static void addClass(String preLabel){
            classWithRels.put(preLabel,new ConcurrentHashMap<>());
        }

        /**
         * 判断某两个类之间的关系(这里的关系是具有先序性的)是否早已存在于知识库中
         * @param fPre 先序类
         * @param lPre 后序类
         * @return true 如果两个类之间的关系已被写入知识库
         * 注:此方法调用前必须先调用两次isClassContained方法并确定两个类都已存在在知识库中
         */
        public static boolean isRelExisted(String fPre,String lPre){
            return classWithRels.get(fPre).get(lPre) != null;
        }

        /**
         * 将一个新加入知识库的关系写入缓存
         * @param fPre 先序类
         * @param lPre 后序类
         * @param tag 关系种类标签
         * 注：在调用isRelExisted方法并确定关系不在知识库中后,将关系写入知识库然后才能调用该方法写缓存
         */
        public static void addRelation(String fPre,String lPre,int tag){
            classWithRels.get(fPre).put(lPre,tag);
        }

    }

}
