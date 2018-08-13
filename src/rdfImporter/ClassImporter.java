package rdfImporter;

import org.apache.jena.ontology.OntClass;
import util.CLASS_REL;

/**
 * Created by The Illsionist on 2018/8/13.
 */
public interface ClassImporter {

    /**
     * 将类导入数据库
     * @param ontClass
     * @return
     * @throws Exception
     */
    public boolean loadClassIn(OntClass ontClass) throws Exception;


    /**
     * 将两个类之间的关系导入数据库
     * @param class1
     * @param class2
     * @param rel
     * @return
     * @throws Exception
     */
    public boolean loadClassRelIn(OntClass class1, OntClass class2, CLASS_REL rel) throws Exception;

}
