package rdfImporter.impl.cypherImpl;

import Appender.*;
import connection.Neo4jConnection;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import rdfImporter.PropImporter;
import rdfImporter.cache.PropertyCache;
import util.Words;

public class CypherPropImporter implements PropImporter {

    private Appender appender;

    public CypherPropImporter(Appender appender){
        this.appender = appender;
    }

    /**
     * 将本体属性导入Neo4j数据库
     * 多线程读/写知识库和属性缓存,由于方法中存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的属性集间互不相交才可保证不重复写
     * @param ontProperty
     * @return
     * @throws Exception
     */
    public boolean loadPropertyIn(OntProperty ontProperty) throws Exception{
        String preLabel = appender.getPreLabel(ontProperty.getURI());
        if(!PropertyCache.isPropertyContained(preLabel)){ //当前数据库中不存在该属性
            try {
                String cypher = appender.intoProp(ontProperty); //拼接Cypher语句,可能是耗时操作
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                PropertyCache.addProperty(preLabel); //写缓存
            }catch (NoSuchRecordException nRec){
                System.out.println("Import failure of property: " + appender.getPreLabel(ontProperty.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
        }
        return true;
    }

    /**
     * 将两个本体属性之间的关系导入Neo4j数据库
     * 多线程读/写知识库和属性关系缓存,由于方法中存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的属性关系集间互不相交才可保证不重复写
     * @param prop1
     * @param prop2
     * @param rel
     * @return
     * @throws Exception
     */
    @Override
    public boolean loadPropertyRelIn(OntProperty prop1, OntProperty prop2, Words rel) throws Exception {
        String fPre = appender.getPreLabel(prop1.getURI());
        String lPre = appender.getPreLabel(prop2.getURI());
        //写关系的两个属性必须要先存在于知识库中
        if(!PropertyCache.isPropertyContained(fPre) || !PropertyCache.isPropertyContained(lPre))
            return false;
        //如果关系不存在,则写知识库然后写缓存
        if(!PropertyCache.isRelExisted(fPre,lPre)){
            String cypher = appender.intoRel(prop1,prop2,rel);  //拼接Cypher语句,耗时操作
            Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(cypher);
                    return mRst.single().get(0).asInt();
                }
            });
            int tag = -1;
            switch (rel){
                case RDFS_SUBPROPERTYOF : tag = 1;break;
                case OWL_EQPROPERTY : tag = 2;break;
                case OWL_DJPROPERTY : tag = 3;break;
                case OWL_IVPROPERTY : tag = 4;break;
            }
            PropertyCache.addRelation(fPre,lPre,tag); //写缓存
        }
        //无论是已经存在还是已经写入,返回true
        return true;
    }

}
