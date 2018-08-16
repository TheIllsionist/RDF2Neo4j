package rdfImporter.impl.cypherImpl;

import concurrentannotation.GuardedBy;
import connection.Neo4jConnection;
import cypherelement.clause.Cypher;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import rdfImporter.InsImporter;
import rdfImporter.abs.cypherAbs.AbsCypherInsImporter;
import rdfImporter.cache.InsCache;
import util.CypherUtil;
import util.INSTANCE_REL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CypherInsImporter implements InsImporter{

    private final static int DEFAULT_RELCOUNT = 6000;  //TODO:默认初始容量的选择还有待调研
    private final static ReentrantReadWriteLock relLock = new ReentrantReadWriteLock();
    @GuardedBy("relLock") private final static HashMap<String,HashMap<String,HashSet<String>>> insRels = new HashMap<>();

    public static boolean relExisted()

    /**
     * 将某一实例写入Neo4j数据库,会有多个线程调用此方法
     * 虽然方法中存在“先检查-后执行”竞态条件,但是因为每个写实例的线程所写的实例集之间没有交集,因此仍能保证线程安全
     * @param individual
     * @return
     * @throws Exception
     */
    @Override
    public boolean loadInsIn(Individual individual) throws Exception {
        String preLabel = CypherUtil.getPreLabel(individual.getURI());
        if(!InsCache.insContained(preLabel)){
            try{
                String cypher = CypherUtil.intoInsCypher(individual);  //拼接Cypher语句,可能属于耗时操作
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                InsCache.addIndividual(preLabel);  //写实例缓存
            }catch (NoSuchRecordException nRec){
                System.out.println("Import failure of individual: " + CypherUtil.getPreLabel(individual.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
        }
        return true;
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, ObjectProperty property) throws Exception {
        String pre1 = CypherUtil.getPreLabel(ins1.getURI());
        String pre2 = CypherUtil.getPreLabel(ins2.getURI());
        //写关系的两个实例必须要先存在于知识库中
        if(!InsCache.insContained(pre1) || !InsCache.insContained(pre2)){
            return false;
        }

    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, INSTANCE_REL rel) throws Exception {
        String pre1 = CypherUtil.getPreLabel(ins1.getURI());
        String pre2 = CypherUtil.getPreLabel(ins2.getURI());
        //写关系的两个实例必须要先存在于知识库中
        if(!InsCache.insContained(pre1) || !InsCache.insContained(pre2)){
            return false;
        }

    }
}
