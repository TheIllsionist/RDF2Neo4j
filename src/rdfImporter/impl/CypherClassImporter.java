package rdfImporter.impl;

import Appender.*;
import connection.Neo4jConnection;
import org.apache.jena.ontology.OntClass;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import rdfImporter.ClassImporter;
import rdfImporter.cache.CypherClassCache;
import util.Words;

public class CypherClassImporter implements ClassImporter{

    private Appender appender;

    public CypherClassImporter(Appender appender){
        this.appender = appender;
    }

    public void setAppender(Appender appender){
        this.appender = appender;
    }

    /**
     * 将本体类导入Neo4j数据库
     * 多线程将类写数据库和类缓存,由于方法中存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的类集间互不相交才可保证不重复写
     * @param ontClass
     * @return
     * @throws Exception
     */
    public boolean loadClassIn(OntClass ontClass) throws Exception{
        String preLabel = appender.getPreLabel(ontClass.getURI());
        if(!CypherClassCache.classContained(preLabel)){ //当前数据库中不存在该类,注意此处存在竞态条件
            try{
                String cypher = appender.intoCls(ontClass);  //拼接Cypher语句,可能属于耗时操作
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
                CypherClassCache.addClass(preLabel);  //写缓存
            }catch (NoSuchRecordException nRec){  //可能由于图数据库未经词汇初始化而报错
                System.out.println("Import failure of class: " + appender.getPreLabel(ontClass.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
        }
        return true;  //数据库中早已存在该类或写入成功
    }

    /**
     * 将两个本体类之间的关系导入Neo4j数据库
     * 多线程将关系写知识库和写缓存,由于方法中存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的类关系集间互不相交才可保证不重复写
     * @param class1
     * @param class2
     * @param rel
     * @return
     * @throws Exception
     */
    public boolean loadClassRelIn(OntClass class1, OntClass class2, Words rel) throws Exception {
        String fPre = appender.getPreLabel(class1.getURI());
        String lPre = appender.getPreLabel(class2.getURI());
        //写关系的两个类必须要先存在与知识库中
        if(!CypherClassCache.classContained(fPre) || !CypherClassCache.classContained(lPre))
            return false;
        //如果关系不存在,则写知识库然后写缓存
        if(!CypherClassCache.relExisted(fPre,lPre)){  //注意此处存在竞态条件
            String cypher = appender.intoRel(class1,class2,rel);  //拼接Cypher语句,可能是耗时操作
            Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() { //写知识库,耗时
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(cypher);
                    return mRst.single().get(0).asInt();
                }
            });
            int tag = -1;
            switch (rel){
                case RDFS_SUBCLASSOF : tag = 1;break;
                case OWL_EQCLASS : tag = 2;break;
                case OWL_DJCLASS : tag = 3;break;
            }
            CypherClassCache.addRelation(fPre,lPre,tag);  //写缓存
        }
        //无论是已经存在还是已经写入,返回true
        return true;
    }

}
