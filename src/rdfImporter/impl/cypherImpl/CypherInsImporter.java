package rdfImporter.impl.cypherImpl;

import connection.Neo4jConnection;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import rdfImporter.abs.cypherAbs.AbsCypherInsImporter;
import util.CypherUtil;
import util.INSTANCE_REL;

public class CypherInsImporter extends AbsCypherInsImporter{


    @Override
    public boolean loadInsIn(Individual individual) throws Exception {
        String preLabel = CypherUtil.getPreLabel(individual.getURI());
        if(insContained(preLabel))  //知识库中已经有此实例
            return true;
        String cypher = CypherUtil.intoInsCypher(individual);  //可能是耗时操作,于是在锁外进行
        try{
            insLock.writeLock().lock();
            if(!insContained(preLabel)){  //需要重新判断一下,避免“先检查-后执行”竞态条件
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写数据库
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                individuals.put(preLabel,null);  //写缓存
            }
            return true;
        }finally {
            insLock.writeLock().unlock();
        }
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, ObjectProperty property) throws Exception {
        return false;
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, INSTANCE_REL rel) throws Exception {
        return false;
    }
}
