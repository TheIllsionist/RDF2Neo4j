package rdfImporter.impl;

import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.graphdb.Node;
import rdfImporter.ResourceImporter;
import util.CypherUtil;
import util.cacheUtil.CacheClass;

/**
 * Created by The Illsionist on 2018/7/18.
 * 资源导入类,线程安全地实现各类资源导入
 */
@ThreadSafe
public class CypherResourceImporter implements ResourceImporter{

    public CypherResourceImporter(){

    }

    /**
     * 导入任何RDF数据前都要确保图数据库中已经存在了词汇结点
     */
    @Override
    public void initGraph() {
        Neo4jConnection.getSession().writeTransaction(new TransactionWork<Object>() {
            @Override
            public Object execute(Transaction transaction) {
                transaction.run(CypherUtil.initGraphCypher());
                return null;
            }
        });
    }

    /**
     * 检查该本体类是否已存在于知识库中,如果没有则将该本体类写入知识库
     * TODO:当前只有一个线程往知识库中写入类和写类缓存,所以目前写知识库和写类缓存没有作为一个原子操作
     * @param ontClass &nbsp 当前可能会被写入知识库和类缓存的类
     * @return &nbsp 结果保证该类要存在于知识库中
     */
    @Override
    public Node loadClassAsNode(OntClass ontClass) throws Exception{
        if(!CacheClass.isClassContained(CypherUtil.getPreLabel(ontClass.getURI()))){ //可能之前导入的类会与当前知识源中的类有重合
            String cypher = CypherUtil.intoClsCypher(ontClass);
            Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(cypher);
                    return mRst.single().get(0).asInt();
                }
            });
            CacheClass.addClass(CypherUtil.getPreLabel(ontClass.getURI()));  //写缓存
        }
        return null;  //TODO:目前先实现返回空
    }

    @Override
    public Node loadPropertyAsNode(OntProperty ontProperty) {
        return null;
    }

    @Override
    public Node loadIndividualAsNode(Individual individual) {
        return null;
    }
}
