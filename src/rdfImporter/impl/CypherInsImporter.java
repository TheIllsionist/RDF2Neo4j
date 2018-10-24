package rdfImporter.impl;

import Appender.*;
import concurrentannotation.GuardedBy;
import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.vocabulary.OWL;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import rdfImporter.InsImporter;
import rdfImporter.cache.CypherInsCache;
import util.Words;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ThreadSafe
public class CypherInsImporter implements InsImporter{

    private Appender appender;
    private final static int DEFAULT_RELCOUNT = 6000;  //TODO:默认初始容量的选择还有待调研
    private final static ReentrantReadWriteLock relLock = new ReentrantReadWriteLock();  //实例关系缓存的读写锁
    @GuardedBy("relLock") private final static HashMap<String,HashMap<String,HashSet<String>>> insRels = new HashMap<>();

    static {
        Record rec = null;
        StatementResult mRst = Neo4jConnection.getSession().run("match(obj:OWL_OBJECTPROPERTY),(ins1:OWL_NAMEDINDIVIDUAL)-[r]->(ins2:OWL_NAMEDINDIVIDUAL)" +
                " where r.preLabel = obj.preLabel return ins1.preLabel,r.preLabel,ins2.preLabel");  //此查询可能会返回空串吗?
        while(mRst.hasNext()){
            rec = mRst.next();
            String ins1 = rec.get(0).asString();
            String rel = rec.get(1).asString();
            String ins2 = rec.get(2).asString();
            if(ins1 == null || ins2 == null || ins1.equals("null") || ins2.equals("null"))  //没有preLabel的空节点会造成此现象出现
                continue;
            if(!insRels.containsKey(ins1)){
                insRels.put(ins1,new HashMap<>());
            }
            if(!insRels.get(ins1).containsKey(ins2)){
                insRels.get(ins1).put(ins2,new HashSet<>());
            }
            if(!insRels.get(ins1).get(ins2).contains(rel)){
                insRels.get(ins1).get(ins2).add(rel);
            }
        }
    }

    public CypherInsImporter(Appender appender){
        this.appender = appender;
    }

    public void setAppender(Appender appender){
        this.appender = appender;
    }

    /**
     * 将某一实例写入Neo4j数据库,会有多个线程调用此方法
     * 由于方法中存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的实例集间互不相交才可保证不重复写
     * @param individual
     * @return
     * @throws Exception
     */
    @Override
    public boolean loadInsIn(Individual individual) throws Exception {
        String preLabel = appender.getPreLabel(individual.getURI());
        if(!CypherInsCache.insContained(preLabel)){  //知识库中不存在该实例,此处存在“先检查-后执行”竞态条件
            try{
                String cypher = appender.intoIns(individual);  //拼接Cypher语句,可能属于耗时操作
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                CypherInsCache.addIndividual(preLabel);  //写实例缓存
            }catch (NoSuchRecordException nRec){
                System.out.println("Import failure of individual: " + appender.getPreLabel(individual.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }catch (Exception e){
                System.out.println("导入实例: " + preLabel + "失败");
                throw e;
            }
        }
        return true;
    }

    /**
     * 判断知识库中是否存在某个指定的关系
     * 必须先确定两个实例都已存在于知识库中并且关系缓存中有了该关系的两方,才能调用该方法
     * TODO:该方法目前只在loadInsRelIn方法中调用
     * @param fPre
     * @param lPre
     * @param rel
     * @return
     */
    public static boolean relExisted(String fPre,String lPre,String rel){
        relLock.readLock().lock();  //获得读锁
        try{
            return insRels.get(fPre).get(lPre).contains(rel);
        }finally {
            relLock.readLock().unlock();  //释放读锁
        }
    }

    /**
     * 将两个实例之间的对象属性关系写入知识库和缓存
     * 多线程写实例间关系,由于方法中存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的实例关系集间互不相交才可保证不重复写
     * @param ins1
     * @param ins2
     * @param property
     * @return
     * @throws Exception
     */
    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, ObjectProperty property) throws Exception {
        String pre1 = appender.getPreLabel(ins1.getURI());
        String pre2 = appender.getPreLabel(ins2.getURI());
        String rel = appender.getPreLabel(property.getURI());
        //写关系的两个实例必须要先存在于知识库中
        if(!CypherInsCache.insContained(pre1) || !CypherInsCache.insContained(pre2)){
            return false;
        }
        relLock.writeLock().lock();  //获得写锁
        try{
            if(!insRels.containsKey(pre1)){
                insRels.put(pre1,new HashMap<>());
            }
            if(!insRels.get(pre1).containsKey(pre2)){
                insRels.get(pre1).put(pre2,new HashSet<>());
            }
        }finally {
            relLock.writeLock().unlock();  //释放写锁
        }
        if(relExisted(pre1,pre2,rel))  //方法中判断关系rel是否已存在
            return true;
        String cypher = appender.intoRel(ins1,ins2,property);  //耗时操作,不在临界区内执行
        return writeRelIn(pre1,pre2,rel,cypher);
    }

    /**
     * 将两个实例之间的语义关系写入知识库和缓存
     * 多线程写实例间关系,由于方法中存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的实例关系集间互不相交才可保证不重复写
     * @param ins1
     * @param ins2
     * @param rel
     * @return
     * @throws Exception
     */
    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, Words rel) throws Exception {
        String pre1 = appender.getPreLabel(ins1.getURI());
        String pre2 = appender.getPreLabel(ins2.getURI());
        String uriRel = rel == Words.OWL_SAME_AS ? appender.getPreLabel(OWL.sameAs.getURI()) : appender.getPreLabel(OWL.differentFrom.getURI());
        //写关系的两个实例必须要先存在于知识库中
        if(!CypherInsCache.insContained(pre1) || !CypherInsCache.insContained(pre2)){
            return false;
        }
        relLock.writeLock().lock();  //获得写锁
        try{
            if(!insRels.containsKey(pre1)){
                insRels.put(pre1,new HashMap<>());
            }
            if(!insRels.get(pre1).containsKey(pre2)){
                insRels.get(pre1).put(pre2,new HashSet<>());
            }
        }finally {
            relLock.writeLock().unlock();  //释放写锁
        }
        if(relExisted(pre1,pre2,uriRel))  //方法中判断关系uriRel是否已存在
            return true;
        String cypher = appender.intoRel(ins1,ins2,rel);  //耗时操作,不在临界区内执行
        return writeRelIn(pre1,pre2,uriRel,cypher);
    }

    /**
     * 多线程执行,将实例间关系写入知识库和缓存
     * @param pre1
     * @param pre2
     * @param rel
     * @param cypher
     * @return
     */
    private boolean writeRelIn(String pre1,String pre2,String rel,final String cypher){
        Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {  //写数据库,耗时操作,不在临界区执行
            @Override
            public Integer execute(Transaction transaction) {
                StatementResult mRst = transaction.run(cypher);
                return mRst.single().get(0).asInt();
            }
        });
        relLock.writeLock().lock();  //获取写锁
        try {
            insRels.get(pre1).get(pre2).add(rel);  //写缓存
        }finally {
            relLock.writeLock().unlock();  //释放写锁
        }
        return true;
    }

}
