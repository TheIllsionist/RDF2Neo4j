package rdfImporter.impl.cypherImpl;

import connection.Neo4jConnection;
import org.apache.jena.ontology.OntClass;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import rdfImporter.ClassImporter;
import rdfImporter.cache.ClassCache;
import util.CLASS_REL;
import util.CypherUtil;

public class CypherClassImporter implements ClassImporter{
    /**
     * 将本体类导入Neo4j数据库
     * 多个线程将类写入知识库和类缓存,必须要保证每个线程所写入的类集间互不相交,即可在该方法中存在“先检查-后执行”竞态条件下,仍能保证线程安全性
     * @param ontClass
     * @return
     * @throws Exception
     */
    public boolean loadClassIn(OntClass ontClass) throws Exception{
        String preLabel = CypherUtil.getPreLabel(ontClass.getURI());
        if(!ClassCache.classContained(preLabel)){ //当前数据库中不存在该类
            try{
                String cypher = CypherUtil.intoClsCypher(ontClass);  //拼接Cypher语句,可能属于耗时操作
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                ClassCache.addClass(preLabel);  //写缓存
            }catch (NoSuchRecordException nRec){  //可能因为图未经初始化所以返回空集
                System.out.println("Import failure of class: " + CypherUtil.getPreLabel(ontClass.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
        }
        return true;  //TODO:目前先实现返回true
    }

    /**
     * 将两个本体类之间的关系导入Neo4j数据库
     * 多个线程将关系写入知识库和缓存,必须要保证每个线程所写入的类关系集间互不相交,即可在该方法中存在“先检查-后执行”竞态条件下,仍能保证线程安全性
     * @param class1
     * @param class2
     * @param rel
     * @return
     * @throws Exception
     */
    public boolean loadClassRelIn(OntClass class1, OntClass class2, CLASS_REL rel) throws Exception {
        String fPre = CypherUtil.getPreLabel(class1.getURI());
        String lPre = CypherUtil.getPreLabel(class2.getURI());
        //写关系的两个类必须要先存在与知识库中
        if(!ClassCache.classContained(fPre) || !ClassCache.classContained(lPre))
            return false;
        //如果关系不存在,则写知识库然后写缓存
        if(!ClassCache.relExisted(fPre,lPre)){
            String cypher = CypherUtil.intoRelCypher(class1,class2,rel);  //拼接Cypher语句,可能是耗时操作
            Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
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
            ClassCache.addRelation(fPre,lPre,tag);
        }
        //无论是已经存在还是已经写入,返回true
        return true;
    }

}
