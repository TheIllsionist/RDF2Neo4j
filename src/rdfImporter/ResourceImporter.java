package rdfImporter;

import javafx.beans.property.Property;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.neo4j.graphdb.Node;

public interface ResourceImporter {

    /**
     * 将某个类导入数据库
     * @param ontClass
     * @return
     */
    boolean loadClassIn(OntClass ontClass) throws Exception;

    /**
     * 将某个属性导入数据库
     * @param ontProperty
     * @return
     */
    boolean loadPropertyIn(OntProperty ontProperty) throws Exception;

    /**
     * 将某个实例导入数据库
     * @param individual
     * @return
     */
    boolean loadIndividualIn(Individual individual) throws Exception;

    /**
     * 将两个类之间的某种关系导入数据库
     * 关系的语义可能会根据关系的种类有所不同,可能两个类之间的顺序是有先序关系的
     * @param ontClass1
     * @param ontClass2
     * @param property
     * @return
     * @throws Exception
     */
    boolean loadClassRelIn(OntClass ontClass1, OntClass ontClass2, Property property) throws Exception;

    /**
     * 将两个属性之间的某种关系导入数据库
     * 关系的语义可能会根据关系的种类有所不同,可能两个属性之间的顺序是有先序关系的
     * @param ontProperty1
     * @param ontProperty2
     * @param property
     * @return
     * @throws Exception
     */
    boolean loadPropertyRelIn(OntProperty ontProperty1,OntProperty ontProperty2,Property property) throws Exception;

    /**
     * 将两个实例之间的某种关系导入数据库
     * 关系的语义可能会根据关系的种类有所不同,可能两个实例之间的顺序是有先序关系的
     * @param ins1
     * @param ins2
     * @param property
     * @return
     * @throws Exception
     */
    boolean loadInsRelIn(Individual ins1,Individual ins2,Property property) throws Exception;




}
