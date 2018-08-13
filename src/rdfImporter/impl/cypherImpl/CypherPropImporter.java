package rdfImporter.impl.cypherImpl;

import connection.Neo4jConnection;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import rdfImporter.abs.cypherAbs.AbsCypherPropImporter;
import util.CypherUtil;
import util.PROPERTY_REL;

public class CypherPropImporter extends AbsCypherPropImporter {

    public boolean loadPropertyIn(OntProperty ontProperty) throws Exception{
        if(!CacheProperty.isPropertyContained(CypherUtil.getPreLabel(ontProperty.getURI()))){
            try {
                String cypher = CypherUtil.intoPropCypher(ontProperty);
                Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                    @Override
                    public Integer execute(Transaction transaction) {
                        StatementResult mRst = transaction.run(cypher);
                        return mRst.single().get(0).asInt();
                    }
                });
            }catch (NoSuchRecordException nRec){
                System.out.println("Import failure of property: " + CypherUtil.getPreLabel(ontProperty.getURI()) +
                        ". Maybe because of lack of initialization.");
                throw nRec;
            }
            CacheProperty.addProperty(CypherUtil.getPreLabel(ontProperty.getURI()));
        }
        return true;
    }

    @Override
    public boolean loadPropertyRelIn(OntProperty prop1, OntProperty prop2, PROPERTY_REL rel) throws Exception {
        String fPre = CypherUtil.getPreLabel(prop1.getURI());
        String lPre = CypherUtil.getPreLabel(prop2.getURI());
        //写关系的两个属性必须要先存在与知识库中
        if(!CacheProperty.isPropertyContained(fPre) || !CacheProperty.isPropertyContained(lPre))
            return false;
        //如果关系不存在,则写知识库然后写缓存
        if(!CacheProperty.isRelExisted(fPre,lPre)){
            String cypher = CypherUtil.intoRelCypher(prop1,prop2,rel);
            Neo4jConnection.getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(cypher);
                    return mRst.single().get(0).asInt();
                }
            });
            int tag = -1;
            switch (rel){
                case SUBPROPERTY_OF : tag = 1;break;
                case EQUIVALENT_PROPERTY : tag = 2;break;
                case DISJOINT_PROPERTY : tag = 3;break;
                case INVERSE_OF : tag = 4;break;
            }
            CacheProperty.addRelation(fPre,lPre,tag);
        }
        //无论是已经存在还是已经写入,返回true
        return true;
    }

}
