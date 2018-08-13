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
        return false;
    }

}
