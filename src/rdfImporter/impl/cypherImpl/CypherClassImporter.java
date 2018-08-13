package rdfImporter.impl.cypherImpl;

import connection.Neo4jConnection;
import org.apache.jena.ontology.OntClass;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import rdfImporter.abs.cypherAbs.AbsCypherClassImporter;
import util.CLASS_REL;
import util.CypherUtil;

public class CypherClassImporter extends AbsCypherClassImporter {

    public boolean loadClassIn(OntClass ontClass) throws Exception{
        if(!CacheClass.isClassContained(CypherUtil.getPreLabel(ontClass.getURI()))){ //当前数据库中不存在该类
            try{
                String cypher = CypherUtil.intoClsCypher(ontClass);
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
            }catch (NoSuchRecordException nRec){  //可能因为图未经初始化所以返回空集
                System.out.println("Import failure of class: " + CypherUtil.getPreLabel(ontClass.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
            CacheClass.addClass(CypherUtil.getPreLabel(ontClass.getURI()));  //写缓存
        }
        return true;  //TODO:目前先实现返回true
    }

    public boolean loadClassRelIn(OntClass class1, OntClass class2, CLASS_REL rel) throws Exception {
        String fPre = CypherUtil.getPreLabel(class1.getURI());
        String lPre = CypherUtil.getPreLabel(class2.getURI());
        //写关系的两个类必须要先存在与知识库中
        if(!CacheClass.isClassContained(fPre) || !CacheClass.isClassContained(lPre))
            return false;
        //如果关系不存在,则写知识库然后写缓存
        if(!CacheClass.isRelExisted(fPre,lPre)){
            String cypher = CypherUtil.intoRelCypher(class1,class2,rel);
            Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(cypher);
                    return mRst.single().get(0).asInt();
                }
            });
            int tag = -1;
            switch (rel){
                case SUBCLASS_OF : tag = 1;break;
                case DISJOINT_CLASS : tag = 2;break;
                case EQUIVALENT_CLASS : tag = 3;break;
            }
            CacheClass.addRelation(fPre,lPre,tag);
        }
        //无论是已经存在还是已经写入,返回true
        return true;
    }

}
