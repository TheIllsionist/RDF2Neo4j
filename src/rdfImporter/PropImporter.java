package rdfImporter;

import org.apache.jena.ontology.OntProperty;
import util.PROPERTY_REL;

/**
 * Created by The Illsionist on 2018/8/13.
 */
public interface PropImporter {
    /**
     * 将属性导入数据库
     * @param property
     * @return
     * @throws Exception
     */
    public boolean loadPropertyIn(OntProperty property) throws Exception;

    /**
     * 将两个属性之间的关系导入数据库
     * @param prop1
     * @param prop2
     * @param rel
     * @return
     * @throws Exception
     */
    public boolean loadPropertyRelIn(OntProperty prop1, OntProperty prop2, PROPERTY_REL rel) throws Exception;

}
