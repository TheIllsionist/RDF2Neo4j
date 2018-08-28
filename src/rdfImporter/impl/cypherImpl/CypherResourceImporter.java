package rdfImporter.impl.cypherImpl;

import Appender.impl.CpElementAppender;
import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import javafx.beans.property.Property;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import rdfImporter.ClassImporter;
import rdfImporter.InsImporter;
import rdfImporter.PropImporter;
import rdfImporter.ResourceImporter;

/**
 * Created by The Illsionist on 2018/7/18.
 * 资源导入类,线程安全地实现各类资源导入
 */
@ThreadSafe
public class CypherResourceImporter implements ResourceImporter{

    private final ClassImporter classImporter;
    private final PropImporter propImporter;
    private final InsImporter insImporter;

    public CypherResourceImporter(ClassImporter classImporter,PropImporter propImporter,InsImporter insImporter){
        this.classImporter = classImporter;
        this.propImporter = propImporter;
        this.insImporter = insImporter;
    }

    /**
     * 导入任何RDF数据前都要确保图数据库中已经存在了词汇结点
     * 初始化工作必须允许多次执行而不相互冲突
     */
    public void initGraph() {
        Neo4jConnection.getSession().writeTransaction(new TransactionWork<Object>() {
            @Override
            public Object execute(Transaction transaction) {
                transaction.run(CpElementAppender.initGraphCypher());
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
    public boolean loadClassIn(OntClass ontClass) throws Exception{
        return classImporter.loadClassIn(ontClass);
    }

    /**
     * 检查本体属性是否已存在于知识库中,如果没有则将该本体属性写入知识库
     * TODO:当前只有一个线程往知识库中写入属性缓存,所以目前写知识库和写属性缓存没有作为一个原子操作
     * @param ontProperty &nbsp 当前可能被写入知识库和属性缓存的属性
     * @return &nbsp 结果保证该属性要存在于知识库中
     * @throws Exception
     */
    @Override
    public boolean loadPropertyIn(OntProperty ontProperty) throws Exception{
        return propImporter.loadPropertyIn(ontProperty);
    }

    @Override
    public boolean loadIndividualIn(Individual individual) {
        return false;
    }

    @Override
    public boolean loadClassRelIn(OntClass ontClass1, OntClass ontClass2, Property property) throws Exception {
        return false;
    }

    @Override
    public boolean loadPropertyRelIn(OntProperty ontProperty1, OntProperty ontProperty2, Property property) throws Exception {
        return false;
    }

    @Override
    public boolean loadInsRelIn(Individual ins1, Individual ins2, Property property) throws Exception {
        return false;
    }

}
