package datasource;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Literal;
import util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public interface RdfProvider {

    /**
     * 得到RDFModel中的所有类
     * @return &nbsp 包含RDFModel中所有类的集合
     */
    Set<OntClass> allOntClasses();

    /**
     * 得到RDFModel中的所有属性
     * @return
     */
    Set<OntProperty> allOntProperties();

    /**
     * 得到RDFModel中的所有数据类型属性
     * @return
     */
    Set<DatatypeProperty> allDatatypeProperties();

    /**
     * 得到RDFModel中的所有对象属性
     * @return
     */
    Set<ObjectProperty> allObjectProperties();


    /**
     * 得到RDFModel中所有的实例,包括空节点类的实例
     * @return &nbsp 包含RDFModel中所有实例的集合
     */
    Set<Individual> allIndividuals();

    /**
     * 得到RDFModel中所有属于指定类的实例
     * @param ontClass
     * @return
     */
    Set<Individual> allIndividualsOfClass(OntClass ontClass);

    /**
     * 得到某本体资源的所有的rdfs:label集合
     * @param ontResource
     * @return
     */
    Set<String> allLabelsOf(OntResource ontResource);

    /**
     * 得到某本体资源的所有rdfs:comment集合
     * @param ontResource
     * @return
     */
    Set<String> allCommentsOf(OntResource ontResource);

    /**
     * 得到某个特定OWL实例所拥有的所有数据类型属性及其对应的属性值,如果某个属性的属性值有多个,则放入一个列表
     * @param individual &nbsp 某指定实例
     * @return
     */
    Map<DatatypeProperty,List<Literal>> allDpValuesOf(Individual individual);

    /**
     * 得到某个特定OWL实例所拥有的所有对象属性及其对应的属性值
     * 由于关于实例与实例之间关系的知识表示规范特点,当前接口的返回值较为复杂
     * @param individual &nbsp 某指定实例
     * @return
     */
    Map<ObjectProperty,Individual> allOpValuesOf(Individual individual);

    /**
     * 得到该RDFModel中所有的有父子关系的类对,子类在前,父类在后
     * @return
     */
    Queue<Pair<OntClass,OntClass>> allSubClassOfRel();

    /**
     * 得到该RDFModel中所有的有父子关系的属性对,子属性在前,父属性在后
     * @return
     */
    Queue<Pair<OntProperty,OntProperty>> allSubPropertyOfRel();



}