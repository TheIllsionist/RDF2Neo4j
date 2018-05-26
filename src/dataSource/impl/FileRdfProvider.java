package dataSource.impl;

import dataSource.RdfProvider;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import sun.plugin.dom.exception.InvalidStateException;
import java.io.InputStream;
import java.util.*;

public class FileRdfProvider implements RdfProvider {

    private final static String meta = "http://kse.seu.edu.cn/meta#";
    private static ObjectProperty instanceIs = null;

    private OntModel ontModel = null;
    private String sourcePath = null;

    private Individual utilIns = null;  //为了加快处理速度的辅助变量
    private HashMap<DatatypeProperty,List<Literal>> insDpVals = null;
    private HashMap<ObjectProperty,Map<Individual,Map<DatatypeProperty,List<Literal>>>> insOpVals = null;


    public FileRdfProvider(String sourcePath){
        this.sourcePath = sourcePath;
        ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);//owl_full,in-memory,no reasoner
        InputStream in = FileManager.get().open(sourcePath);
        this.ontModel.read(in,null);
        instanceIs = ontModel.getObjectProperty(meta + "实例"); //得到 meta:实例 对象属性
    }

    public OntModel getOntModel(){
        return this.ontModel;
    }

    public ObjectProperty getInstanceIs() {
        return instanceIs;
    }

    /**
     * 返回该RDF提供者拥有的所有本体类
     * @return
     */
    @Override
    public Set<OntClass> allOntClasses() {
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        HashSet<OntClass> classes = new HashSet<>();
        ExtendedIterator<OntClass> classIter = ontModel.listClasses();
        while (classIter.hasNext()){
            classes.add(classIter.next());
        }
        return classes;
    }

    /**
     * 返回该RDF提供者所拥有的所有本体属性(目前返回的属性只包括DP和OP)
     * @return
     */
    @Override
    public Set<OntProperty> allOntProperties(){
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        HashSet<OntProperty> ontProperties = new HashSet<>();
        ExtendedIterator<OntProperty> ontPropertyIter = ontModel.listAllOntProperties();//列所有属性,包括DP,OP,AP等
        while(ontPropertyIter.hasNext()){
            OntProperty tmpPro = ontPropertyIter.next();
            //进行一步筛选,只选择DP和OP
            if(tmpPro.hasProperty(RDF.type,OWL.DatatypeProperty) || tmpPro.hasProperty(RDF.type,OWL.ObjectProperty)){
                ontProperties.add(tmpPro);
            }
        }
        return ontProperties;
    }

    /**
     * 返回该RDF提供者所拥有的所有数据类型属性
     * @return
     */
    public Set<DatatypeProperty> allDatatypeProperties() {
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        HashSet<DatatypeProperty> datatypeProperties = new HashSet<>();
        ExtendedIterator<DatatypeProperty> dpIter = ontModel.listDatatypeProperties();
        while(dpIter.hasNext()){
            datatypeProperties.add(dpIter.next());
        }
        return datatypeProperties;
    }

    /**
     * 返回该RDF提供者所拥有的所有对象属性
     * @return
     */
    public Set<ObjectProperty> allObjectProperties() {
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        HashSet<ObjectProperty> objectProperties = new HashSet<>();
        ExtendedIterator<ObjectProperty> opIter = ontModel.listObjectProperties();
        while(opIter.hasNext()){
            objectProperties.add(opIter.next());
        }
        return objectProperties;
    }

    /**
     * 返回该RDF提供者所拥有的所有非blankNode实例
     * @return
     */
    @Override
    public Set<Individual> allIndividuals() {
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        HashSet<Individual> individuals = new HashSet<>();
        ExtendedIterator<Individual> insIter = ontModel.listIndividuals();
        while(insIter.hasNext()){
            individuals.add(insIter.next());
        }
        //去掉了blankNode的实例
        individuals.removeIf(individual -> individual.hasProperty(RDF.type,ontModel.getOntClass(meta + "blankNode")));
        return individuals;
    }

    /**
     * 返回某个指定本体资源所拥有的所有Label(rdfs:label属性值)
     * @param ontResource
     * @return
     */
    @Override
    public Set<String> allLabelsOf(OntResource ontResource) {
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
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
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
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
    public Map<DatatypeProperty, List<Literal>> allDatatypePropertyValuesOf(Individual individual) {
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        if(individual == utilIns && insDpVals != null){  //表明之前处理过此实例
            return insDpVals;
        }else{
            collectOntPropValuesOf(individual);
            return insDpVals;
        }
    }

    /**
     * 返回某个指定实例所拥有的所有对象属性,该对象属性的实例取值,以及这两个实例之间关系的属性(参考中介节点规范)
     * @param individual &nbsp 某指定实例
     * @return
     */
    @Override
    public Map<ObjectProperty, Map<Individual,Map<DatatypeProperty,List<Literal>>>> allObjectPropertyValuesOf(Individual individual) {
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        if(individual == utilIns && insOpVals != null){ //表明之前处理过此实例
            return insOpVals;
        }else{
            collectOntPropValuesOf(individual);
            return insOpVals;
        }
    }

    /**
     * 列出某个类全部的直接父类
     * @param ontClass
     * @return
     */
    @Override
    public Set<OntClass> allSupClassesOf(OntClass ontClass){
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
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
    @Override
    public Set<Property> allSupPropertiesOf(OntProperty ontProperty){
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        HashSet<Property> supProperties = new HashSet<>();
        StmtIterator supPropStmtIter = ontProperty.listProperties(RDFS.subPropertyOf); //列当前属性subPropertyOf的值,即它的所有父属性
        while(supPropStmtIter.hasNext()){
            Resource tmpResource = supPropStmtIter.nextStatement().getResource();
            supProperties.add(ontModel.getProperty(tmpResource.getURI()));
        }
        return supProperties;
    }

    /**
     * 列出某个实例的所有的直接所属类
     * @param individual
     * @return
     */
    @Override
    public Set<OntClass> allOntClassesOf(Individual individual){
        if(ontModel == null || ontModel.size() == 0){
            throw new InvalidStateException("无本体或本体内容为空!");
        }
        HashSet<OntClass> ontClasses = new HashSet<>();
        StmtIterator rdfTypeIter = individual.listProperties(RDF.type);  //列出该实例所有谓词是rdf:type的语句
        while(rdfTypeIter.hasNext()){
            Resource tmpRes = rdfTypeIter.nextStatement().getResource();  //取第三元的资源
            if(tmpRes.hasProperty(RDF.type,OWL.Class)){  //如果第三元是类,说明当前第三元是individual的所属类
                ontClasses.add(ontModel.getOntClass(tmpRes.getURI()));
            }
        }
        return ontClasses;
    }

    private void collectOntPropValuesOf(Individual individual){
        this.utilIns = individual;
        if(insDpVals == null){
            insDpVals = new HashMap<>();
        }else {
            insDpVals.clear();
        }
        if(insOpVals == null){
            insOpVals = new HashMap<>();
        }else{
            insOpVals.clear();
        }
        StmtIterator proIter = utilIns.listProperties();  //列出实例所有属性(包括数据类型属性和对象属性)
        while(proIter.hasNext()){
            Statement proStmt = proIter.nextStatement();
            Property property = proStmt.getPredicate();
            if(property.hasProperty(RDF.type, OWL.DatatypeProperty)){  // 数据类型属性
                DatatypeProperty dp = ontModel.getDatatypeProperty(property.getURI());
                if(!insDpVals.containsKey(dp)){
                    insDpVals.put(dp,new LinkedList<>());
                }
                StmtIterator valIter = utilIns.listProperties(dp);
                while(valIter.hasNext()){
                    insDpVals.get(dp).add(valIter.nextStatement().getLiteral());
                }
            }else if(property.hasProperty(RDF.type,OWL.ObjectProperty)){ //对象属性
                ObjectProperty op = ontModel.getObjectProperty(property.getURI());
                if(!insOpVals.containsKey(op)){  //如果不包括此op属性,加入接着就处理了
                    insOpVals.put(op,new HashMap<>());
                }else{  //如果已经包括了该Op属性,则说明已经处理过了,跳过(**注意此处小细节很重要**)
                    continue;
                }
                StmtIterator insIter = utilIns.listProperties(op);
                while(insIter.hasNext()){
                    Individual blankIns = ontModel.getIndividual(insIter.nextStatement().getObject().toString());//此时得到的是blankNode的实例
                    if(blankIns.getProperty(instanceIs) == null){  //数据没有按照规范存储
                        System.out.println("实例" + utilIns.getURI() + "的对象属性" + op.getURI() + "的中介节点" + blankIns.getURI() + "没有instanceIs属性!");
                        continue;
                    }
                    Individual realIns = ontModel.getIndividual(blankIns.getProperty(instanceIs).getResource().getURI()); //此时得到真正实例
                    if(realIns == null){  //由于数据中出现的一些错误,比如没有用owl:NamedIndividual词汇去定义某个实例,导致Jena查询不到它
                        System.out.println("实例" + utilIns.getURI() + "的对象属性" + op.getURI() + "的中介节点" + blankIns.getURI() + "的instanceIs属性值为空!");
                        continue;
                    }
                    if(!insOpVals.get(op).containsKey(realIns)){
                        insOpVals.get(op).put(realIns,new HashMap<>());
                    }
                    StmtIterator relDpsIter = blankIns.listProperties();
                    while(relDpsIter.hasNext()){
                        Statement relDpValStmt = relDpsIter.nextStatement();
                        if(!relDpValStmt.getPredicate().hasProperty(RDF.type,OWL.DatatypeProperty)){  //当前只处理空节点的数据类型属性
                            continue;
                        }
                        DatatypeProperty relDp = ontModel.getDatatypeProperty(relDpValStmt.getPredicate().getURI());
                        if(!insOpVals.get(op).get(realIns).containsKey(relDp)){
                            insOpVals.get(op).get(realIns).put(relDp,new LinkedList<>());
                        }
                        insOpVals.get(op).get(realIns).get(relDp).add(relDpValStmt.getLiteral());
                    }
                }
            }
        }
    }
}
