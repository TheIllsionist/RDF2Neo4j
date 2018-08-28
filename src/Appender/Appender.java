package Appender;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import util.CLASS_REL;
import util.INSTANCE_REL;
import util.PROPERTY_REL;

/**
 * Created by The Illsionist on 2018/8/28.
 */
public interface Appender {
    /**
     * 拼接导入类的语句
     * @param ontClass
     * @return
     * @throws Exception
     */
    String intoCls(OntClass ontClass) throws Exception;

    /**
     * 拼接导入属性的语句
     * @param ontProperty
     * @return
     * @throws Exception
     */
    String intoProp(OntProperty ontProperty) throws Exception;

    /**
     * 拼接导入实例的语句
     * @param individual
     * @return
     * @throws Exception
     */
    String intoIns(Individual individual) throws Exception;

    /**
     * 拼接导入两个类间关系的语句
     * @param class1
     * @param class2
     * @param rel
     * @return
     * @throws Exception
     */
    String intoRel(OntClass class1, OntClass class2, CLASS_REL rel) throws Exception;

    /**
     * 拼接导入两个属性间关系的语句
     * @param prop1
     * @param prop2
     * @param rel
     * @return
     * @throws Exception
     */
    String intoRel(OntProperty prop1, OntProperty prop2, PROPERTY_REL rel) throws Exception;

    /**
     * 拼接导入两个实例间语义关系的语句
     * @param ins1
     * @param ins2
     * @param rel
     * @return
     * @throws Exception
     */
    String intoRel(Individual ins1, Individual ins2, INSTANCE_REL rel) throws Exception;

    /**
     * 拼接导入两个实例间属性关系的语句
     * @param ins1
     * @param ins2
     * @param prop
     * @return
     * @throws Exception
     */
    String intoRel(Individual ins1,Individual ins2,ObjectProperty prop) throws Exception;

}
