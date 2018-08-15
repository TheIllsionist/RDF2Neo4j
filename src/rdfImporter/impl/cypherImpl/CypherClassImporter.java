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
    /**
     * 将本体类导入Neo4j数据库
     * 只有一个线程将类写入知识库和类缓存,所以虽然在该方法中存在“先检查-后执行”竞态条件,但这并不影响线程安全性
     * @param ontClass
     * @return
     * @throws Exception
     */
    public boolean loadClassIn(OntClass ontClass) throws Exception{
        if(!CacheClass.classContained(CypherUtil.getPreLabel(ontClass.getURI()))){ //当前数据库中不存在该类
            try{
                String cypher = CypherUtil.intoClsCypher(ontClass);  //拼接Cypher语句,可能属于耗时操作
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
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

    /**
     * 将两个本体类之间的关系导入Neo4j数据库
     * 因为两个类之间只可能有一种关系,所以虽然在该方法中存在“先检查-后执行”竞态条件,但这并不影响线程安全性
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
        if(!CacheClass.classContained(fPre) || !CacheClass.classContained(lPre))
            return false;
        //如果关系不存在,则写知识库然后写缓存
        if(!CacheClass.relExisted(fPre,lPre)){
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
            CacheClass.addRelation(fPre,lPre,tag);
        }
        //无论是已经存在还是已经写入,返回true
        return true;
    }

}
