package rdfImporter.impl.cypherImpl;

import Appender.impl.CpElementAppender;
import Appender.impl.CypherAppender;
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
import rdfImporter.cache.InsCache;
import util.INSTANCE_REL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ThreadSafe
public class CypherInsImporter implements InsImporter{

    private final CypherAppender appender;
    private final static int DEFAULT_RELCOUNT = 6000;  //TODO:默认初始容量的选择还有待调研
    private final static ReentrantReadWriteLock relLock = new ReentrantReadWriteLock();
    @GuardedBy("relLock") private final static HashMap<String,HashMap<String,HashSet<String>>> insRels = new HashMap<>();

    static {
        Record rec = null;
        StatementResult mRst = Neo4jConnection.getSession().run("match(obj:OWL_OBJECTPROPERTY),(ins1:OWL_NAMEDINDIVIDUAL)-[r]->(ins2:OWL_NAMEDINDIVIDUAL)" +
                " where r.preLabel = obj.preLabel return ins1.preLabel,r.preLabel,ins2.preLabel");//此查询可能会返回空串吗?
        while(mRst.hasNext()){
            rec = mRst.next();
            String ins1 = rec.get(0).asString();
            String rel = rec.get(1).asString();
            String ins2 = rec.get(2).asString();
            if(!insRels.get(ins1).containsKey(ins2)){
                insRels.get(ins1).put(ins2,new HashSet<>());
            }
            if(!insRels.get(ins1).get(ins2).contains(rel)){
                insRels.get(ins1).get(ins2).add(rel);
            }
        }
    }

    public CypherInsImporter(CypherAppender appender){
        this.appender = appender;
    }

    /**
     * 判断知识库中是否存在某个指定的关系
     * @param fPre
     * @param lPre
     * @param rel
     * @return
     */
    public static boolean relExisted(String fPre,String lPre,String rel){
        try{
            relLock.readLock().lock();  //获得读锁
            if(!insRels.containsKey(fPre))
                return false;
            if(!insRels.get(fPre).containsKey(lPre))
                return false;
            if(!insRels.get(fPre).get(lPre).contains(rel))
                return false;
            return true;
        }finally {
            relLock.readLock().unlock();  //释放读锁
        }
    }

    /**
     * 将某一实例写入Neo4j数据库,会有多个线程调用此方法
     * 虽然方法中存在“先检查-后执行”竞态条件,但是因为每个写实例的线程所写的实例集之间没有交集,因此仍能保证线程安全
     * @param individual
     * @return
     * @throws Exception
     */
    @Override
    public boolean loadInsIn(Individual individual) throws Exception {
        String preLabel = CpElementAppender.getPreLabel(individual.getURI());
        if(!InsCache.insContained(preLabel)){  //知识库中不存在该实例
            try{
                String cypher = appender.intoIns(individual);  //拼接Cypher语句,可能属于耗时操作
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                InsCache.addIndividual(preLabel);  //写实例缓存
            }catch (NoSuchRecordException nRec){
                System.out.println("Import failure of individual: " + CpElementAppender.getPreLabel(individual.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
        }
        return true;
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, ObjectProperty property) throws Exception {
        String pre1 = CpElementAppender.getPreLabel(ins1.getURI());
        String pre2 = CpElementAppender.getPreLabel(ins2.getURI());
        String rel = CpElementAppender.getPreLabel(property.getURI());
        //写关系的两个实例必须要先存在于知识库中
        if(!InsCache.insContained(pre1) || !InsCache.insContained(pre2)){
            return false;
        }
        final String cypher;   //下面利用了两次锁检查,目的也是尽量减少在临界区内部执行耗时操作
        if(!relExisted(pre1,pre2,rel)){  //两实例间的该关系不存在
            cypher = appender.intoRel(ins1,ins2,property); //耗时操作,在临界区外执行
        }else{
            return true;  //关系已存在则直接返回
        }
        return writeRelIn(pre1,pre2,rel,cypher);
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, INSTANCE_REL rel) throws Exception {
        String pre1 = CpElementAppender.getPreLabel(ins1.getURI());
        String pre2 = CpElementAppender.getPreLabel(ins2.getURI());
        String uriRel = rel == INSTANCE_REL.SAME_AS ? CpElementAppender.getPreLabel(OWL.sameAs.getURI()) : CpElementAppender.getPreLabel(OWL.differentFrom.getURI());
        //写关系的两个实例必须要先存在于知识库中
        if(!InsCache.insContained(pre1) || !InsCache.insContained(pre2)){
            return false;
        }
        final String cypher;
        if(!relExisted(pre1,pre2,uriRel)){
            cypher = appender.intoRel(ins1,ins2,rel);
        }else{
            return true;  //关系已存在则直接返回
        }
        return writeRelIn(pre1,pre2,uriRel,cypher);
    }

    private boolean writeRelIn(String pre1,String pre2,String rel,final String cypher){
        try{
            relLock.writeLock().lock();  //获取写锁
            if(!insRels.containsKey(pre1)){
                insRels.put(pre1,new HashMap<>());
            }
            if(!insRels.get(pre1).containsKey(pre2)){
                insRels.get(pre1).put(pre2,new HashSet<>());
            }
            if(!insRels.get(pre1).get(pre2).contains(rel)){  //两实例间关系不存在
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                insRels.get(pre1).get(pre2).add(rel);  //写缓存
            }
            return true;
        }finally {
            relLock.writeLock().unlock();  //释放写锁
        }
    }
}
