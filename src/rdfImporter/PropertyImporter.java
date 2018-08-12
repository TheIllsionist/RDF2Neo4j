package rdfImporter;

import connection.Neo4jConnection;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import util.CypherUtil;

import java.util.concurrent.ConcurrentHashMap;

public class PropertyImporter {

    /**
     * 属性及其关系缓存
     * 在知识库中属性的数目是有限的,因此属性缓存缓存全部的属性及其之间的关系
     * 属性之间主要有四种关系：subPropertyOf,equivalentProperty,disjointProperty,inverseOf
     * 只有一个线程写属性,多个线程读属性
     * 多个线程读关系,多个线程写关系,但每个线程所写关系种类不同,一个线程只写某个特定类别的关系
     * 在缓存中用一个整型变量表征两个属性之间的关系：1-subPropertyOf,2-equivalentProperty,3-disjointProperty,4-inverseOf
     */
    static class CacheProperty{
        /** 利用静态初始化器保证对象引用的可见性,当前实现为只缓存preLabel字符串 **/
        private static ConcurrentHashMap<String,ConcurrentHashMap<String,Integer>> propWithRels = new ConcurrentHashMap<>();

        //TODO:此时加载知识库中的内容到内存中是一个合适的时机吗?从性能上考虑
        static {  //类加载时即查询知识库中已有的属性
            StatementResult res = Neo4jConnection.getSession().run("match(pro:OWL_DATATYPEPROPERTY)" +
                    " optional match(p)-[r:RDFS_SUBPROPERTYOF|:EQUIVALENT_PROPERTY|:DISJOINT_PROPERTY|:INVERSE_OF]->(anop:OWL_DATATYPEPROPERTY) " +
                    "return p.preLabel as p,case r.preLabel when \"rdfs:subPropertyOf\" then 1 when \"owl:equivalentProperty\" then 2 " +
                    "when \"owl:disjointProperty\" then 3 when \"owl:inverseOf\" then 4 end as index,anop.preLabel as anop union " +
                    "match(pro:OWL_OBJECTPROPERTY) optional match(p)-[r:RDFS_SUBPROPERTYOF|:EQUIVALENT_PROPERTY|:DISJOINT_PROPERTY|:INVERSE_OF]->(anop:OWL_OBJECTPROPERTY) " +
                    "return p.preLabel as p,case r.preLabel when \"rdfs:subPropertyOf\" then 1 when \"owl:equivalentProperty\" then 2 " +
                    "when \"owl:disjointProperty\" then 3 when \"owl:inverseOf\" then 4 end as index,anop.preLabel as anop ");
            Record rec = null;
            while (res.hasNext()){
                rec = res.next();
                String proPre = rec.get(0).asString();
                if(proPre == null)  //防止第1个参数为空的意外情况出现
                    continue;
                if(!propWithRels.contains(proPre)){  //保证了第1个键的值不会被延迟初始化,避免"先检查后执行"竞态条件发生
                    propWithRels.put(proPre,new ConcurrentHashMap<>());
                }
                String anoProPre = rec.get(2).asString();
                if(anoProPre == null)
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

    public static boolean loadPropertyIn(OntProperty ontProperty) throws Exception{
        if(!CacheProperty.isPropertyContained(CypherUtil.getPreLabel(ontProperty.getURI()))){
            try {
                String cypher = CypherUtil.intoPropCypher(ontProperty);
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
            }catch (NoSuchRecordException nRec){
                System.out.println("Import failure of property: " + CypherUtil.getPreLabel(ontProperty.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
            CacheProperty.addProperty(CypherUtil.getPreLabel(ontProperty.getURI()));
        }
        return true;
    }
}
