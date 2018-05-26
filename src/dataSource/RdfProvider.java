package dataSource;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RdfProvider {

    /**
     * 得到RDFModel中的所有类
     * @return &nbsp 包含RDFModel中所有类的集合
     */
    Set<OntClass> allOntClasses();

    /**
     * 得到RDFModel中的所有属性(可以根据场景需要不同实现)
     * @return
     */
    Set<OntProperty> allOntProperties();

    /**
     * 得到RDFModel中的所有非空节点实例
     * @return &nbsp 包含RDFModel中所有实例的集合
     */
    Set<Individual> allIndividuals();

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
     * 列出某个类的所有父类(根据情况的不同可以全部列出或只列出直接父类)
     * @param ontClass
     * @return
     */
    Set<OntClass> allSupClassesOf(OntClass ontClass);

    /**
     * 列出某个属性的所有父属性(根据情况的不同可以全部列出或只列出直接父类)
     * @param ontProperty
     * @return
     */
    Set<Property> allSupPropertiesOf(OntProperty ontProperty);

    /**
     * 列出某个实例的所有直接所属类
     * @param individual
     * @return
     */
    Set<OntClass> allOntClassesOf(Individual individual);

    /**
     * 得到某个特定OWL实例所拥有的所有数据类型属性及其对应的属性值,如果某个属性的属性值有多个,则放入一个列表
     * @param individual &nbsp 某指定实例
     * @return
     */
    Map<DatatypeProperty,List<Literal>> allDatatypePropertyValuesOf(Individual individual);

    /**
     * 得到某个特定OWL实例所拥有的所有对象属性及其对应的属性值
     * 由于关于实例与实例之间关系的知识表示规范特点,当前接口的返回值较为复杂
     * @param individual &nbsp 某指定实例
     * @return
     */
    Map<ObjectProperty,Map<Individual,Map<DatatypeProperty,List<Literal>>>> allObjectPropertyValuesOf(Individual individual);

}
