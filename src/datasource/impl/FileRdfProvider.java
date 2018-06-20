package datasource.impl;

import datasource.RdfProvider;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class FileRdfProvider implements RdfProvider {

    private String prefix = "meta";  //TODO:后面修改为从配置文件中读取
    private String nameSpace = "http://kse.seu.edu.cn/meta#";  //TODO:后面修改为从配置文件中读取
    private OntModel ontModel = null;
    private String sourcePath = null;  //文件夹路径

    /**
     * 构造函数,根据指定的文件夹路径读取该文件夹下的所有owl文件的内容,如果该文件夹下没有owl文件,则返回一个空模型
     * @param sourcePath &nbsp 指定的文件夹路径
     */
    public FileRdfProvider(String sourcePath){
        this.sourcePath = sourcePath;
        File folder = new File(sourcePath);
        File[] files = folder.listFiles();
        ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); //创建Model,OWL Full,In Memory,No Reasoner
        if(files.length == 0){
            return;
        }
        for (File file : files) {  //将文件夹下的所有本体文件的内容读入
           InputStream in = FileManager.get().open(file.getAbsolutePath());
           this.ontModel.read(in,null);
        }

    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getSourcePath(){
        return this.sourcePath;
    }

    public void setSourcePath(String sourcePath){
        this.sourcePath = sourcePath;
    }

    public OntModel getOntModel(){
        return this.ontModel;
    }

    public void setOntModel(OntModel ontModel){
        this.ontModel = ontModel;
    }

    /**
     * 返回该RDF提供者拥有的所有本体类
     * @return
     */
    @Override
    public Set<OntClass> allOntClasses() {
        HashSet<OntClass> classes = new HashSet<>();
        ExtendedIterator<OntClass> classIter = ontModel.listClasses();
        while (classIter.hasNext()){
            classes.add(classIter.next());
        }
        return classes;
    }

    /**
     * 返回该RDF提供者拥有的所有本体属性
     * @return
     */
    @Override
    public Set<OntProperty> allOntProperties(){
        HashSet<OntProperty> ontProperties = new HashSet<>();
        ExtendedIterator<OntProperty> ontPropertyIter = ontModel.listAllOntProperties();//列所有属性,包括DP,OP,AP等
        while(ontPropertyIter.hasNext()){
            OntProperty tmpPro = ontPropertyIter.next();
            ontProperties.add(tmpPro);
        }
        return ontProperties;
    }

    /**
     * 返回该RDF提供者拥有的所有数据类型属性
     * @return
     */
    @Override
    public Set<DatatypeProperty> allDatatypeProperties() {
        HashSet<DatatypeProperty> dps = new HashSet<>();
        ExtendedIterator<DatatypeProperty> dpIter = ontModel.listDatatypeProperties();
        while(dpIter.hasNext()){
            dps.add(dpIter.next());
        }
        return dps;
    }

    /**
     * 返回该RDF提供者拥有的所有对象属性
     * @return
     */
    @Override
    public Set<ObjectProperty> allObjectProperties() {
        HashSet<ObjectProperty> objectProperties = new HashSet<>();
        ExtendedIterator<ObjectProperty> opIter = ontModel.listObjectProperties();
        while(opIter.hasNext()){
            objectProperties.add(opIter.next());
        }
        return objectProperties;
    }

    /**
     * 返回该RDF提供者拥有的所有实例
     * @return
     */
    @Override
    public Set<Individual> allIndividuals() {
        HashSet<Individual> individuals = new HashSet<>();
        ExtendedIterator<Individual> insIter = ontModel.listIndividuals();
        while(insIter.hasNext()){
            individuals.add(insIter.next());
        }
        return individuals;
    }

    /**
     * 返回此RDF提供者中直接属于某指定类别的实例的集合
     * @param ontClass &nbsp 指定类别
     * @return
     */
    @Override
    public Set<Individual> allIndividualsOfClass(OntClass ontClass) {
        Set<Individual> individuals = new HashSet<>();
        ExtendedIterator<? extends OntResource> insIter = ontClass.listInstances(true);
        while (insIter.hasNext()){
            individuals.add((Individual)insIter.next());
        }
        return individuals;
    }

    /**
     * 返回某个指定本体资源所拥有的所有Label(rdfs:label属性值)
     * @param ontResource
     * @return
     */
    @Override
    public Set<String> allLabelsOf(OntResource ontResource) {
        HashSet<String> labels = new HashSet<>();
        NodeIterator labelIter = ontResource.listPropertyValues(RDFS.label);
        while(labelIter.hasNext()){
            labels.add(labelIter.next().asLiteral().getString());
        }
        return labels;
    }

    /**
     * 返回某个指定本体资源所拥有的所有Comments(rdfs:comment属性值)
     * @param ontResource
     * @return
     */
    @Override
    public Set<String> allCommentsOf(OntResource ontResource) {
        HashSet<String> comments = new HashSet<>();
        NodeIterator commentIter = ontResource.listPropertyValues(RDFS.comment);
        while(commentIter.hasNext()){
            comments.add(commentIter.next().asLiteral().getString());
        }
        return comments;
    }

    /**
     * 返回某个指定实例所拥有的所有数据类型属性及其对应的取值
     * @param individual &nbsp 某指定实例
     * @return
     */
    @Override
    public Map<DatatypeProperty, List<Literal>> allDpValuesOf(Individual individual) {
        Map<DatatypeProperty,List<Literal>> dpValsOfIns = new HashMap<>();
        StmtIterator iterator = individual.listProperties();  //列出该实例声明所有属性的三元组集
        while(iterator.hasNext()) {  //迭代该三元组集
            Statement propStmt = iterator.nextStatement();
            Property property = propStmt.getPredicate();  //取当前此条三元组的谓词
            if (property.hasProperty(RDF.type, OWL.DatatypeProperty)) {  //当前谓词是数据类型属性
                DatatypeProperty dp = ontModel.getDatatypeProperty(property.getURI());
                if (!dpValsOfIns.containsKey(dp)) {
                    dpValsOfIns.put(dp, new LinkedList<>());
                }
                StmtIterator valIter = individual.listProperties(dp);
                while (valIter.hasNext()) {
                    dpValsOfIns.get(dp).add(valIter.nextStatement().getLiteral());
                }
            }
        }
        return dpValsOfIns;
    }

    /**
     * 返回某个指定实例所拥有的所有对象属性,该对象属性的实例取值,以及这两个实例之间关系的属性(参考中介节点规范)
     * @param individual &nbsp 某指定实例
     * @return
     */
    @Override
    public Map<ObjectProperty, Individual> allOpValuesOf(Individual individual) {
        Map<ObjectProperty,Individual> opValsOfIns = new HashMap<>();
        StmtIterator iterator = individual.listProperties();
        while(iterator.hasNext()){
            Statement propStmt = iterator.nextStatement();
            Property property = propStmt.getPredicate();
            if(property.hasProperty(RDF.type,OWL.ObjectProperty)){
                ObjectProperty op = ontModel.getObjectProperty(property.getURI());
                if(!opValsOfIns.containsKey(op)){  //不会出现同一个OP下对应多个实例的情况,所以只需加入一次即可
                    opValsOfIns.put(op,ontModel.getIndividual(propStmt.getObject().asResource().getURI()));
                }
            }
        }
        return opValsOfIns;
    }

    /**
     * 列出某个类全部的直接父类
     * @param ontClass
     * @return
     */
    public Set<OntClass> allSupClassesOf(OntClass ontClass){
        HashSet<OntClass> supOntClasses = new HashSet<>();
        ExtendedIterator<OntClass> supOntClassIter = ontClass.listSuperClasses(true);  //列出当前类的直接父类
        while(supOntClassIter.hasNext()){
            supOntClasses.add(supOntClassIter.next());
        }
        return supOntClasses;
    }

    /**
     * 列出某个属性全部的直接父属性
     * TODO: 这里注意了,某个属性的父属性可能已经不是 owl:DatatypeProperty或者OWL:ObjectProperty,所以这里使用了Set<Property>作为返回结果
     * 父属性可能包含 owl:topDataProperty 和 owl:topObjectProperty
     * @param ontProperty
     * @return
     */
    public Set<Property> allSupPropertiesOf(OntProperty ontProperty){
        HashSet<Property> supProperties = new HashSet<>();
        StmtIterator supPropStmtIter = ontProperty.listProperties(RDFS.subPropertyOf); //列当前属性subPropertyOf的值,即它的所有父属性
        while(supPropStmtIter.hasNext()){
            Resource tmpResource = supPropStmtIter.nextStatement().getObject().asResource();
            supProperties.add(ontModel.getProperty(tmpResource.getURI()));
        }
        return supProperties;
    }

    /**
     * 列出某个实例直接所属的类的集合
     * @param individual
     * @return
     */
    public Set<OntClass> allOntClassesOf(Individual individual){
        HashSet<OntClass> ontClasses = new HashSet<>();
        StmtIterator rdfTypeIter = individual.listProperties(RDF.type);  //列出该实例所有谓词是rdf:type的语句
        while(rdfTypeIter.hasNext()){
            Resource tmpRes = rdfTypeIter.nextStatement().getObject().asResource();  //取第三元的资源
            if(tmpRes.hasProperty(RDF.type,OWL.Class)){  //如果第三元是类,说明当前第三元是individual的所属类
                ontClasses.add(ontModel.getOntClass(tmpRes.getURI()));
            }
        }
        return ontClasses;
    }

}
