package rdfImporter.abs.cypherAbs;

import concurrentannotation.GuardedBy;
import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import org.apache.jena.ontology.Individual;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import rdfImporter.InsImporter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by The Illsionist on 2018/8/13.
 * 为提升性能,实现将实例和实例间关系同时导入,需要将实例和实例关系分别缓存,两块缓存的读写分别由两个不同的锁保护,两块缓存的一致性通过程序实现维护
 */
@ThreadSafe
public abstract class AbsCypherInsImporter implements InsImporter{

    private final static int DEFAULT_INSCOUNT = 3000;  //TODO:默认初始容量的选择还有待调研
    private final static int DEFAULT_RELCOUNT = 6000;  //TODO:默认初始容量的选择还有待调研
    protected final static ReentrantReadWriteLock insLock = new ReentrantReadWriteLock();
    protected final static ReentrantReadWriteLock relLock = new ReentrantReadWriteLock();
    /** 实例缓存,由锁insLock保护,多个线程读,多个线程写 **/
    @GuardedBy("insLock") protected final static Map<String,Individual> individuals = new HashMap<>();
    /** 实例关系缓存,由锁relLock保护,多个线程读,多个线程写  **/
    @GuardedBy("relLock") protected final static Map<String,Map<String,HashSet<String>>> insRels = new HashMap<>();

    static {  //类加载时即查询知识库中的已有实例和已有关系
        Record rec = null;
        StatementResult mRst = Neo4jConnection.getSession().run("match(ins:OWL_NAMEDINDIVIDUAL) return ins.preLabel");//此查询可能会返回空串吗?
        while(mRst.hasNext()){
            rec = mRst.next();
            String ins = rec.get(0).asString();
            individuals.put(ins,null);
            insRels.put(ins,new HashMap<>());  //这样做是默认绝大多数的实例都有对象属性关系
        }
        mRst = Neo4jConnection.getSession().run("match(obj:OWL_OBJECTPROPERTY),(ins1:OWL_NAMEDINDIVIDUAL)-[r]->(ins2:OWL_NAMEDINDIVIDUAL)" +
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

    /**
     * 判断某个实例是否已经存在于知识库中
     * @param preLabel
     * @return
     */
    public static boolean insContained(String preLabel){
        try{
            insLock.readLock().lock();
            return individuals.containsKey(preLabel);
        }finally {
            insLock.readLock().unlock();
        }
    }

    /**
     * 判断某两个实例是否已经存在于知识库中
     * @param pre1
     * @param pre2
     * @return
     */
    public static boolean insBothContained(String pre1,String pre2){
        try{
            insLock.readLock().lock();
            return individuals.containsKey(pre1) && individuals.containsKey(pre2);
        }finally {
            insLock.readLock().unlock();
        }
    }


}
