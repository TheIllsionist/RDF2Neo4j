package rdfloader.impl;

import rdfloader.Words;
import datasource.impl.FileRdfProvider;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.*;
import org.neo4j.driver.v1.*;
import sun.plugin.dom.exception.InvalidStateException;
import java.security.InvalidParameterException;
import java.util.*;
import static org.neo4j.driver.v1.Values.parameters;

public class CypherResourceLoader{

    private Driver driver = null;
    private Session utilSession = null;
    private HashMap<String,String> nsMap = null;

    public CypherResourceLoader(String uri, String user, String psd){
        if(driver == null){
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user,psd));  //获取Neo4j驱动
        }
        //保存命名空间的前缀和全称
        nsMap = new HashMap<>();
        nsMap.put("http://kse.seu.edu.cn/rdb#","rdb");
        nsMap.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf");
        nsMap.put("http://www.w3.org/2000/01/rdf-schema#","rdfs");
        nsMap.put("http://www.w3.org/2002/07/owl#","owl");
        nsMap.put("http://www.w3.org/2001/XMLSchema#","xsd");
        nsMap.put("http://kse.seu.edu.cn/meta#","meta");
        nsMap.put("http://kse.seu.edu.cn/wgbq#","wgbq");
        nsMap.put("http://kse.seu.edu.cn/xgbg#","xgbg");
        nsMap.put("rdb","http://kse.seu.edu.cn/rdb#");
        nsMap.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        nsMap.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
        nsMap.put("owl","http://www.w3.org/2002/07/owl#");
        nsMap.put("xsd","http://www.w3.org/2001/XMLSchema#");
        nsMap.put("meta","http://kse.seu.edu.cn/meta#");
        nsMap.put("wgbq","http://kse.seu.edu.cn/wgbq#");
        nsMap.put("xgbg","http://kse.seu.edu.cn/xgbg#");
    }

    /**
     * 根据Driver获得执行Cypher语句的Session对象
     * @return
     */
    public Session getSession(){
        if(utilSession == null){
            utilSession = driver.session();
        }
        return utilSession;
    }

    /**
     * 将一个OWL词汇作为Neo4j的Node写入数据库,当前只有六个词汇
     * 即:OWL_CLASS, OWL_DATAPROPERTY, OWL_OBJECTPROPERTY, OWL_NAMEDINDIVIDUAL, OWL_TOPDATAPROPERTY, OWL_TOPOBJECTPROPERTY
     * 当前每个代表词汇的Node有两个Property,即词汇的uri和词汇的preLabel
     * @param word
     * @return
     */
    private int loadWordAsNode(Words word) {
        String uri = null;
        switch(word){
            case OWL_CLASS: uri = OWL.Class.getURI();break;
            case OWL_DATATYPEPROPERTY: uri = OWL.DatatypeProperty.getURI();break;
            case OWL_OBJECTPROPERTY: uri = OWL.ObjectProperty.getURI();break;
            case OWL_NAMEDINDIVIDUAL: uri = OWL2.NamedIndividual.getURI();break;
            case OWL_TOPDATAPROPERTY: uri = OWL2.topDataProperty.getURI();break;
            case OWL_TOPOBJECTPROPERTY: uri = OWL2.topObjectProperty.getURI();break;
        }
        String finalUri = uri;
        String finalLabel = getPreLabel(finalUri);
        int result = getSession().writeTransaction(new TransactionWork<Integer>() {
            @Override
            public Integer execute(Transaction transaction) {
                StatementResult mRst = transaction.run("create (n:OWL_WORD {uri:$uri,preLabel:$preLabel}) return id(n)",
                        parameters("uri", finalUri,"preLabel",finalLabel));
                return mRst.single().get(0).asInt();
            }
        });
        return result;
    }

    /**
     * 根据某资源的preLabel检查图数据库中是否已有该资源(目前该方法仅仅用在对类资源和属性资源的检查上)
     * @param resource
     * @param word
     * @return
     * @throws Exception
     */
    private boolean isResourceExisted(OntResource resource,Words word) throws Exception{
        StringBuilder builder = new StringBuilder();
        builder.append("match(n");
        switch (word){
            case OWL_CLASS:builder.append(":OWL_CLASS ");break;
            case OWL_DATATYPEPROPERTY:builder.append(":OWL_DATATYPEPROPERTY ");break;
            case OWL_OBJECTPROPERTY:builder.append(":OWL_OBJECTPROPERTY ");break;
            case OWL_NAMEDINDIVIDUAL:builder.append(":OWL_NAMEDINDIVIDUAL ");break;
        }
        builder.append("{preLabel:\"" + getPreLabel(resource.getURI()) + "\"}) return id(n) ");
        String finalCypher = builder.toString();
        StatementResult mRst = getSession().run(finalCypher);
        if(mRst.hasNext()){
            return true;
        }
        return false;
    }


    /**
     * 将某个OWL类作为Node节点写入Neo4j,该节点目前有两个属性uri和preLabel
     * 将该类的所有rdfs:label和rdfs:comment都作为节点,与该类节点以对应关系相连
     * @param ontClass
     * @param provider
     * @return
     */
    private void loadClassAsNode(OntClass ontClass,FileRdfProvider provider) throws Exception {
        if(isResourceExisted(ontClass,Words.OWL_CLASS)){
            return;
        }
        String uri = ontClass.getURI();
        String preLabel = getPreLabel(uri);
        HashSet<String> labels = (HashSet<String>)provider.allLabelsOf(ontClass);
        HashSet<String> comments = (HashSet<String>)provider.allCommentsOf(ontClass);
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append("match(wn:OWL_WORD {preLabel:\"owl:Class\"}) ");  //查询到用来辅助定义类的节点
        cypherbd.append("create(n:OWL_CLASS {uri:\"" + uri + "\",preLabel:\"" + preLabel + "\"}) ");//创建代表类的节点
        cypherbd.append("create (n)-[:RDF_TYPE {uri:\"" + RDF.type.getURI() + "\",preLabel:\"rdf:type\"}]->(wn) ");
        //建立类和它的labels之间的关系
        Iterator<String> labelIter = labels.iterator();
        while(labelIter.hasNext()){
            cypherbd.append("create(n)-[:RDFS_LABEL {uri:\""+ RDFS.label.getURI() + "\",preLabel:\"rdfs:label\"}]->({value:\"" + labelIter.next() + "\"}) ");
        }
        //建立类和它的comments之间的关系
        Iterator<String > commentIter = comments.iterator();
        while(commentIter.hasNext()){
            cypherbd.append("create(n)-[:RDFS_COMMENT {uri:\""+ RDFS.comment.getURI() + "\",preLabel:\"rdfs:comment\"}]->({value:\"" + commentIter.next() + "\"}) ");
        }
        cypherbd.append("return id(n)");
        String finalCypher = cypherbd.toString();
        getSession().writeTransaction(new TransactionWork<Integer>() {
            @Override
            public Integer execute(Transaction transaction) {
                StatementResult mRst = transaction.run(finalCypher);
                return mRst.single().get(0).asInt();
            }
        });
    }

    /**
     * 将某个OWL属性作为Node写入Neo4j,写入时将该属性区分为数据类型属性和对象属性两类,属性节点目前有两个属性uri和preLabel
     * 将该属性的所有rdfs:label和rdfs:comment都作为节点,与该属性节点以对应关系相连
     * @param ontProperty
     * @param provider
     * @return
     */
    private void loadPropertyAsNode(OntProperty ontProperty,FileRdfProvider provider) throws Exception{
        if(ontProperty.hasProperty(RDF.type,OWL.DatatypeProperty)){
            if(isResourceExisted(ontProperty,Words.OWL_DATATYPEPROPERTY)){
                return;
            }
        }else if(isResourceExisted(ontProperty,Words.OWL_OBJECTPROPERTY)){
            return;
        }
        String uri = ontProperty.getURI();
        String preLabel = getPreLabel(uri);
        HashSet<String> labels = (HashSet<String>)provider.allLabelsOf(ontProperty);
        HashSet<String> comments = (HashSet<String>)provider.allCommentsOf(ontProperty);
        StringBuilder cypherbd = new StringBuilder();
        if(ontProperty.hasProperty(RDF.type, OWL.DatatypeProperty)){
            cypherbd.append("match(wn:OWL_WORD {preLabel:\"owl:DatatypeProperty\"}) ");  //查询到用来辅助定义DP的节点
            cypherbd.append("create(n:OWL_DATATYPEPROPERTY {uri:\"" + uri + "\",preLabel:\"" + preLabel + "\"}) ");//创建代表DP的节点
        }else if(ontProperty.hasProperty(RDF.type,OWL.ObjectProperty)){
            cypherbd.append("match(wn:OWL_WORD {preLabel:\"owl:ObjectProperty\"}) ");  //查询到用来辅助定义OP的节点
            cypherbd.append("create(n:OWL_OBJECTPROPERTY {uri:\"" + uri + "\",preLabel:\"" + preLabel + "\"}) ");//创建代表OP的节点
        }
        cypherbd.append("create (n)-[:RDF_TYPE {uri:\"" + RDF.type.getURI() + "\",preLabel:\"rdf:type\"}]->(wn) ");
        //建立属性和它的labels之间的关系
        Iterator<String> labelIter = labels.iterator();
        while(labelIter.hasNext()){
            cypherbd.append("create(n)-[:RDFS_LABEL {uri:\""+ RDFS.label.getURI() + "\",preLabel:\"rdfs:label\"}]->({value:\"" + labelIter.next() + "\"}) ");
        }
        //建立属性和它的comments之间的关系
        Iterator<String > commentIter = comments.iterator();
        while(commentIter.hasNext()){
            cypherbd.append("create(n)-[:RDFS_COMMENT {uri:\""+ RDFS.comment.getURI() + "\",preLabel:\"rdfs:comment\"}]->({value:\"" + commentIter.next() + "\"}) ");
        }
        cypherbd.append("return id(n)");
        String finalCypher = cypherbd.toString();
        getSession().writeTransaction(new TransactionWork<Integer>() {
            @Override
            public Integer execute(Transaction transaction) {
                StatementResult mRst = transaction.run(finalCypher);
                return mRst.single().get(0).asInt();
            }
        });
    }

    /**
     * 将某个OWL实例作为Node导入Neo4j,除了将实例的uri和preLabel作为Node的Property之外,实例的数据类型属性也作为Node的Property
     * 同时,该实例的所有rdfs:label和rdfs:comment都作为节点,与该实例节点以对应关系相连
     * @param individual
     * @param provider
     * @return
     * TODO://代码的健壮性需要提高
     */
    private int loadIndividualAsNode(Individual individual,FileRdfProvider provider) {
        String uri = individual.getURI();
        String preLabel = getPreLabel(uri);
        HashSet<String> labels = (HashSet<String>)provider.allLabelsOf(individual);
        HashSet<String> comments = (HashSet<String>)provider.allCommentsOf(individual);
        HashMap<DatatypeProperty,List<Literal>> dpValues = (HashMap<DatatypeProperty, List<Literal>>)provider.allDatatypePropertyValuesOf(individual);
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append("match(wn:OWL_WORD {preLabel:\"owl:NamedIndividual\"}) ");  //查询到用来辅助定义实例的节点
        cypherbd.append("create(n:OWL_NAMEDINDIVIDUAL {uri:\"" + uri + "\",preLabel:\"" + preLabel + "\"}) ");//创建代表实例的节点
        //将实例的数据类型属性作为与实例Node直接相连的Relationship加入,其中该DatatypeProperty的uri,preLabel和rdfs:label作为该Relationship的Property
        Iterator<Map.Entry<DatatypeProperty,List<Literal>>> dpValsIter = dpValues.entrySet().iterator();
        while(dpValsIter.hasNext()){
            Map.Entry<DatatypeProperty,List<Literal>> dpVal = dpValsIter.next();
            if(dpVal.getValue().size() == 0){   //该数据类型属性取值为空时不加入
                continue;
            }
            HashSet<String> dpLabels = (HashSet<String>) provider.allLabelsOf(dpVal.getKey());
            cypherbd.append("create(n)-[:`" + getPreLabel(dpVal.getKey().getURI())  + "` {uri:\"" + dpVal.getKey().getURI() +
                    "\",preLabel:\"" + getPreLabel(dpVal.getKey().getURI()) + "\"");
            if(dpLabels.size() > 0){  //该DP有rdfs:label
                cypherbd.append(",`rdfs:label`:[");
                Iterator<String> dpLabelIter = dpLabels.iterator();
                while(dpLabelIter.hasNext()){
                    cypherbd.append("\"" + dpLabelIter.next() + "\",");
                }
                cypherbd.delete(cypherbd.length() - 1,cypherbd.length());    //删掉最后一个逗号
                cypherbd.append("]");
            }
            cypherbd.append("}]->(:DP_VALUE {value:");
            RDFDatatype datatype = dpVal.getValue().get(0).getDatatype();  //当前属性取值的数据类型(目前只有整型和String)
            if(dpVal.getValue().size() == 1){  //当前属性取值只有一个
                if(datatype.getURI().contains("integer")){
                    cypherbd.append("toInteger(" + dpVal.getValue().get(0).getString() + ")");
                }else {
                    cypherbd.append("\"" + dpVal.getValue().get(0).getString().replace("\\","/") + "\"");
                }
            }else{    //当前属性取值是一个列表
                Iterator<Literal> valIter = dpVal.getValue().iterator();
                cypherbd.append("[");
                while(valIter.hasNext()){
                    if(datatype.getURI().contains("integer")){
                        cypherbd.append(valIter.next().getInt() + ",");
                    }else{
                        cypherbd.append("\"" + valIter.next().getString() + "\",");
                    }
                }
                cypherbd.delete(cypherbd.length() - 1,cypherbd.length()); //列表情况下去掉最后一个逗号
                cypherbd.append("]");
            }
            cypherbd.append("}) ");
        }
        cypherbd.append("create (n)-[:RDF_TYPE {uri:\"" + RDF.type.getURI() + "\",preLabel:\"rdf:type\"}]->(wn) ");//建立实例和OWL_NAMEDINDIVIDUAL词汇间的关系
        //建立实例和它的labels之间的关系
        Iterator<String> labelIter = labels.iterator();
        while(labelIter.hasNext()){
            cypherbd.append("create(n)-[:RDFS_LABEL {uri:\""+ RDFS.label.getURI() + "\",preLabel:\"rdfs:label\"}]->({value:\"" + labelIter.next() + "\"}) ");
        }
        //建立实例和它的comments之间的关系
        Iterator<String > commentIter = comments.iterator();
        while(commentIter.hasNext()){
            cypherbd.append("create(n)-[:RDFS_COMMENT {uri:\""+ RDFS.comment.getURI() + "\",preLabel:\"rdfs:comment\"}]->({value:\"" + commentIter.next() + "\"}) ");
        }
        cypherbd.append("return id(n)");
        String finalCypher = cypherbd.toString();
        int result = getSession().writeTransaction(new TransactionWork<Integer>() {
            @Override
            public Integer execute(Transaction transaction) {
                StatementResult mRst = transaction.run(finalCypher);
                return mRst.single().get(0).asInt();
            }
        });
        return result;
    }

    public void createIndex(String label,String proName){
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append("create index on :").append(label).append("(").append(proName).append(");");
        String finalCypher = cypherbd.toString();
        getSession().writeTransaction(new TransactionWork<Void>() {
            @Override
            public Void execute(Transaction transaction) {
                transaction.run(finalCypher);
                return null;
            }
        });
    }

    /**
     * 初始化Neo4j数据库,将命名空间与简写的映射 与 简写与命名空间的映射写入一个节点,
     * 将用来定义类,属性和实例的OWL词汇分别写成对应节点
     */
    public void initGraph(){
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append("create(ns:NS_MAP {");
        Iterator<Map.Entry<String,String>> nsMapIter = nsMap.entrySet().iterator();
        while(nsMapIter.hasNext()){
            Map.Entry<String,String> keyVal = nsMapIter.next();
            if(keyVal.getKey().contains("#")){
                cypherbd.append("`" + keyVal.getKey() + "`:\"" + keyVal.getValue() + "\" ,");
            }else{
                cypherbd.append(keyVal.getKey() + ":\"" + keyVal.getValue() + "\" ,");
            }
        }
        cypherbd.delete(cypherbd.length() - 1,cypherbd.length());  //将最后一个逗号去掉
        cypherbd.append("}) return id(ns)");
        String finalCypher = cypherbd.toString();
        getSession().writeTransaction(new TransactionWork<Integer>() {
            @Override
            public Integer execute(Transaction transaction) {
                StatementResult mRst = transaction.run(finalCypher);
                return mRst.single().get(0).asInt();
            }
        });
        loadWordAsNode(Words.OWL_CLASS);
        loadWordAsNode(Words.OWL_DATATYPEPROPERTY);
        loadWordAsNode(Words.OWL_NAMEDINDIVIDUAL);
        loadWordAsNode(Words.OWL_OBJECTPROPERTY);
        loadWordAsNode(Words.OWL_TOPDATAPROPERTY);
        loadWordAsNode(Words.OWL_TOPOBJECTPROPERTY);
        createIndex("OWL_WORD","preLabel");
        createIndex("OWL_CLASS","preLabel");
        createIndex("OWL_DATATYPEPROPERTY","preLabel");
        createIndex("OWL_OBJECTPROPERTY","preLabel");
        createIndex("OWL_NAMEDINDIVIDUAL","preLabel");
    }

    /**
     * 将某个本体模型中的所有本体类都作为Node存入Neo4j数据库
     * @param provider
     */
    public void loadAllOntClasses(FileRdfProvider provider){
        HashSet<OntClass> ontClasses = (HashSet<OntClass>)provider.allOntClasses();
        Iterator<OntClass> ontClassIter = ontClasses.iterator();
        OntClass tmpClass = null;
        while (ontClassIter.hasNext()){
            try {
                tmpClass = ontClassIter.next();
                loadClassAsNode(tmpClass,provider);
            }catch (Exception e){
                System.out.println("在写入类Node:" + tmpClass.getURI() + "时出错!");
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 将某个本体模型中的所有本体属性都作为Node写入Neo4j数据库
     * @param provider
     */
    public void loadAllOntProperties(FileRdfProvider provider){
        HashSet<OntProperty> ontProperties = (HashSet<OntProperty>) provider.allOntProperties();//这里面只有DP和OP
        Iterator<OntProperty> ontProIter = ontProperties.iterator();
        OntProperty tmpOntProperty = null;
        while (ontProIter.hasNext()){
            try{
                tmpOntProperty = ontProIter.next();
                loadPropertyAsNode(tmpOntProperty,provider);
            }catch (Exception e){
                System.out.println("在写入属性Node:" + tmpOntProperty.getURI() + "时出错!");
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 将某个本体模型中的所有实例都作为Node写入Neo4j数据库
     * @param provider
     */
    public void loadAllIndividuals(FileRdfProvider provider){
        HashSet<Individual> individuals = (HashSet<Individual>) provider.allIndividuals();
        Iterator<Individual> insIter = individuals.iterator();
        Individual tmpIns = null;
        while (insIter.hasNext()){
            try{
                tmpIns = insIter.next();
                loadIndividualAsNode(tmpIns,provider);
            }catch (Exception e){
                System.out.println("在写入实例Node:" + tmpIns.getURI() + "时出错!");
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 将某个类与其所有直接父类之间的rdfs:subClassOf关系写入neo4j数据库
     * @param ontClass
     * @param provider
     * @return
     */
    public int loadAllDirectSubClassRelOfClass(OntClass ontClass,FileRdfProvider provider){
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append("match(n:OWL_CLASS {preLabel:\"" + getPreLabel(ontClass.getURI()) + "\"}) "); //找到子类节点
        Iterator<OntClass> supClassIter = provider.allSupClassesOf(ontClass).iterator();
        int i = 0;
        while(supClassIter.hasNext()){
            OntClass tmpSupClass = supClassIter.next();
            cypherbd.append("match(sup" + i + ":OWL_CLASS {preLabel:\"" + getPreLabel(tmpSupClass.getURI()) + "\"}) "); //找到当前父类节点
            cypherbd.append("create(n)-[:RDFS_SUBCLASSOF {uri:\"" + RDFS.subClassOf.getURI() + "\",preLabel:\"rdfs:subClassOf\"}]->(sup" + i + ") with n ");
            i++;
        }
        cypherbd.append(" return id(n)");
        int result = -1;
        try{
            String finalCypher = cypherbd.toString();
            result = getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(finalCypher);
                    return mRst.single().get(0).asInt();
                }
            });
        }catch (Exception e){
            System.out.println("在为类Node:" + getPreLabel(ontClass.getURI()) + "和它的所有直接父类之间建立父子关系时出错!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将某个属性与其直接父属性之间的rdfs:subPropertyOf关系写入Neo4j数据库
     * @param ontProperty
     * @param provider
     * @return
     */
    public int loadAllDirectSubProRelOfProperty(OntProperty ontProperty,FileRdfProvider provider){
        StringBuilder cypherbd = new StringBuilder();
        if(ontProperty.hasProperty(RDF.type,OWL.DatatypeProperty)){
            cypherbd.append("match(n:OWL_DATATYPEPROPERTY {preLabel:\"" + getPreLabel(ontProperty.getURI()) + "\"}) ");//找到子DP节点
        }else if(ontProperty.hasProperty(RDF.type,OWL.ObjectProperty)){
            cypherbd.append("match(n:OWL_OBJECTPROPERTY {preLabel:\"" + getPreLabel(ontProperty.getURI()) + "\"}) ");//找到子OP节点
        }else{
            throw new InvalidStateException("遇到非数据类型和非对象类型的属性");
        }
        Iterator<Property> supPropIter = provider.allSupPropertiesOf(ontProperty).iterator();
        int i = 0;
        while(supPropIter.hasNext()){
            Property tmpSupProp = supPropIter.next();
            if(tmpSupProp.hasProperty(RDF.type,OWL.DatatypeProperty)){
                cypherbd.append("match(sup" + i + ":OWL_DATATYPEPROPERTY {preLabel:\"" + getPreLabel(tmpSupProp.getURI()) + "\"}) "); //找到当前父DP节点
            }else if(tmpSupProp.hasProperty(RDF.type,OWL.ObjectProperty)){
                cypherbd.append("match(sup" + i + ":OWL_OBJECTPROPERTY {preLabel:\"" + getPreLabel(tmpSupProp.getURI()) + "\"}) "); //找到当前父OP节点
            }else if(tmpSupProp.getURI().contains("topDataProperty")){
                cypherbd.append("match(sup" + i + ":OWL_WORD {preLabel:\"" + getPreLabel(tmpSupProp.getURI()) + "\"}) ");  //找到owl:topDataProperty节点
            }else if(tmpSupProp.getURI().contains("topObjectProperty")){
                cypherbd.append("match(sup" + i + ":OWL_WORD {preLabel:\"" + getPreLabel(tmpSupProp.getURI()) + "\"}) "); //找到owl:topObjectProperty节点
            }else{
                throw new InvalidStateException("还有其他类型的父属性存在!");
            }
            cypherbd.append("create(n)-[:RDFS_SUBPROPERTYOF {uri:\"" + RDFS.subPropertyOf.getURI() + "\",preLabel:\"rdfs:subPropertyOf\"}]->(sup" + i + ") with n ");
            i++;
        }
        cypherbd.append(" return id(n)");
        int result = -1;
        try{
            String finalCypher = cypherbd.toString();
            result = getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(finalCypher);
                    return mRst.single().get(0).asInt();
                }
            });
        }catch (Exception e){
            System.out.println("在为属性Node:" + getPreLabel(ontProperty.getURI()) + "和它的所有直接父属性之间建立父子关系时出错!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将某个实例和它直接所属的所有类之间的IsA(也即rdf:type)关系写入Neo4j数据库
     * @param individual
     * @param provider
     * @return
     */
    public int loadAllDirectIsARelOfIndividual(Individual individual,FileRdfProvider provider){
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append("match(n:OWL_NAMEDINDIVIDUAL {preLabel:\"" + getPreLabel(individual.getURI()) + "\"}) "); //找到实例节点
        Iterator<OntClass> classIter = provider.allOntClassesOf(individual).iterator(); //枚举该实例所有直接所属的类
        int i = 0;
        while(classIter.hasNext()){
            OntClass tmpClass = classIter.next();
            cypherbd.append("match(class" + i + ":OWL_CLASS {preLabel:\"" + getPreLabel(tmpClass.getURI()) + "\"}) "); //找到当前实例所属的类节点
            cypherbd.append("create(n)-[:RDF_TYPE {uri:\"" + RDF.type.getURI() + "\",preLabel:\"rdf:type\"}]->(class" + i + ") with n ");
            i++;
        }
        cypherbd.append(" return id(n)");
        int result = -1;
        try{
            String finalCypher = cypherbd.toString();
            result = getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(finalCypher);
                    return mRst.single().get(0).asInt();
                }
            });
        }catch (Exception e){
            System.out.println("在为实例Node:" + getPreLabel(individual.getURI()) + "和它的所有直接所属的类之间建立IsA关系时出错!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 对于一个实例,将和它有对象关系的所有实例找出来,与该实例建立对应的关系并按照规范写入Neo4j数据库
     * @param individual
     * @param provider
     * @return
     */
    public int loadAllOpRelOfIndividual(Individual individual,FileRdfProvider provider){
        StringBuilder cypherbd = new StringBuilder();
        cypherbd.append("match(n:OWL_NAMEDINDIVIDUAL {preLabel:\"" + getPreLabel(individual.getURI()) + "\"}) ");//找到主语实例节点
        cypherbd.append("match(bkCls:OWL_CLASS {preLabel:\"meta:blankNode\"}) ");   //找到代表blankNode类的节点
        HashMap<ObjectProperty,Map<Individual,Map<DatatypeProperty,List<Literal>>>> opVals = (HashMap<ObjectProperty, Map<Individual, Map<DatatypeProperty, List<Literal>>>>)provider.allObjectPropertyValuesOf(individual);
        int i = 0;
        for (Map.Entry<ObjectProperty,Map<Individual,Map<DatatypeProperty,List<Literal>>>> opEntry : opVals.entrySet()){
            ObjectProperty tmpOp = opEntry.getKey();  //核心对象属性
            for (Map.Entry<Individual,Map<DatatypeProperty,List<Literal>>> insEntry : opEntry.getValue().entrySet()) {
                Individual thridIns = insEntry.getKey(); // meta:实例 的宾语实例
                cypherbd.append("match(ins" + i + ":OWL_NAMEDINDIVIDUAL {preLabel:\"" + getPreLabel(thridIns.getURI()) + "\"}) "); //找到宾语实例节点
                String newBkNodeUri = nsMap.get("meta") + UUID.randomUUID().toString();
                cypherbd.append("create(bkIns" + i + ":OWL_NAMEDINDIVIDUAL {uri:\"" + newBkNodeUri + "\",preLabel:\"" + getPreLabel(newBkNodeUri) + "\"}) ");  //创建空节点
                for (Map.Entry<DatatypeProperty,List<Literal>> bkDp : insEntry.getValue().entrySet()){ //将空节点的DP作为与空节点有Relationship的节点
                    if(bkDp.getValue().size() == 0){
                        continue;
                    }
                    HashSet<String> bkDpLabels = (HashSet<String>) provider.allLabelsOf(bkDp.getKey());
                    cypherbd.append("create(bkIns" + i + ")-[:`" + getPreLabel(bkDp.getKey().getURI())  + "` {uri:\"" + bkDp.getKey().getURI() +
                            "\",preLabel:\"" + getPreLabel(bkDp.getKey().getURI()) + "\"");
                    if(bkDpLabels.size() > 0){
                        cypherbd.append(",`rdfs:label`:[");
                        Iterator<String> bkDpLabelIter = bkDpLabels.iterator();
                        while(bkDpLabelIter.hasNext()){
                            cypherbd.append("\"" + bkDpLabelIter.next() + "\",");
                        }
                        cypherbd.delete(cypherbd.length() - 1,cypherbd.length());    //删掉最后一个逗号
                        cypherbd.append("]");
                    }
                    cypherbd.append("}]->(:DP_VALUE {value:");
                    RDFDatatype datatype = bkDp.getValue().get(0).getDatatype();  //当前DP取值的数据类型(目前只有整型和String)
                    if(bkDp.getValue().size() == 1){  //当前属性取值只有一个
                        if(datatype.getURI().contains("integer")){
                            cypherbd.append("toInteger(" + bkDp.getValue().get(0).getString() + ")");
                        }else {
                            cypherbd.append("\"" + bkDp.getValue().get(0).getString().replace("\\","/") + "\"");
                        }
                    }else{    //当前属性取值是一个列表
                        Iterator<Literal> valIter = bkDp.getValue().iterator();
                        cypherbd.append("[");  //列表开始
                        while(valIter.hasNext()){
                            if(datatype.getURI().contains("integer"))
                                cypherbd.append(valIter.next().getInt() + ",");
                            else
                                cypherbd.append("\"" + valIter.next().getString() + "\",");
                        }
                        cypherbd.delete(cypherbd.length() - 1,cypherbd.length()); //列表情况下去掉最后一个逗号
                        cypherbd.append("]"); //列表结束
                    }
                    cypherbd.append("}) ");
                }
                HashSet<String> opLabels = (HashSet<String>) provider.allLabelsOf(tmpOp);
                cypherbd.append("create(bkIns" + i + ")-[:RDF_TYPE {uri:\"" + RDF.type.getURI() + "\",preLabel:\"rdf:type\"}]->(bkCls) "); //空节点指向blankNode类Node
                cypherbd.append("create(n)-[:`" + getPreLabel(tmpOp.getURI()) + "` {uri:\"" + tmpOp.getURI() + "\",preLabel:\"" + getPreLabel(tmpOp.getURI()) + "\"");
                if(opLabels.size() > 0){
                    cypherbd.append(",`rdfs:label`:[");
                    Iterator<String> opLabelIter = opLabels.iterator();
                    while(opLabelIter.hasNext()){
                        cypherbd.append("\"" + opLabelIter.next() + "\",");
                    }
                    cypherbd.delete(cypherbd.length() - 1,cypherbd.length());
                    cypherbd.append("]");
                }
                cypherbd.append("}]->(bkIns" + i + ") ");
                ObjectProperty instanceIs = provider.getInstanceIs();
                cypherbd.append("create(bkIns" + i + ")-[:`" + getPreLabel(instanceIs.getURI()) + "` {uri:\"" + instanceIs.getURI() + "\",preLabel:\"" + getPreLabel(instanceIs.getURI()) + "\"}]->(ins" + i + ")  with n,bkCls ");
                i++;
            }
        }
        cypherbd.append(" return id(n)");
        int result = -1;
        try{
            String finalCypher = cypherbd.toString();
            result = getSession().writeTransaction(new TransactionWork<Integer>() {
                @Override
                public Integer execute(Transaction transaction) {
                    StatementResult mRst = transaction.run(finalCypher);
                    return mRst.single().get(0).asInt();
                }
            });
        }catch (Exception e){
            System.out.println("在为实例Node:" + getPreLabel(individual.getURI()) + "和它的所有对象属性的值实例之间建立对象关系时出错!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将本体模型中类与类之间的所有rdfs:subClassOf关系写入Neo4j数据库
     * @param provider
     */
    public void loadAllSubClassOfRel(FileRdfProvider provider){
        Iterator<OntClass> classIter = provider.allOntClasses().iterator();
        OntClass tmpClass = null;
        while(classIter.hasNext()){
            try{
                tmpClass = classIter.next();
                loadAllDirectSubClassRelOfClass(tmpClass,provider);
            }catch (Exception e){
                System.out.println("在写入类Node:" + getPreLabel(tmpClass.getURI()) + "与其所有直接父类之间的父子关系时报错!");
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 将本体模型中属性与属性之间的所有rdfs:subPropertyOf关系写入Neo4j数据库
     * @param provider
     */
    public void loadAllSubPropOfRel(FileRdfProvider provider){
        Iterator<OntProperty> ontPropIter = provider.allOntProperties().iterator();
        OntProperty tmpOntProp = null;
        while (ontPropIter.hasNext()){
            try{
                tmpOntProp = ontPropIter.next();
                loadAllDirectSubProRelOfProperty(tmpOntProp,provider);
            }catch (Exception e){
                System.out.println("在写入属性Node:" + getPreLabel(tmpOntProp.getURI()) + "与其所有直接父属性之间的父子关系时报错!");
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 将本体模型中所有的IsA关系写入Neo4j数据库
     * @param provider
     */
    public void loadAllIsARel(FileRdfProvider provider){
        Iterator<Individual> insIter = provider.allIndividuals().iterator();
        Individual tmpIns = null;
        while (insIter.hasNext()){
            try {
                tmpIns = insIter.next();
                loadAllDirectIsARelOfIndividual(tmpIns,provider);
            }catch (Exception e){
                System.out.println("在写入实例Node:" + getPreLabel(tmpIns.getURI()) + "与其所有直接所属类之间的IsA关系时报错!");
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 将本体模型中实例与实例之间的所有对象属性关系按照既定规范(节点-中介节点-节点)写入Neo4j数据库
     * @param provider
     */
    public void loadAllObjectPropBetweenIns(FileRdfProvider provider){
        Iterator<Individual> insIter = provider.allIndividuals().iterator();
        Individual tmpIns = null;
        while(insIter.hasNext()){
            try{
                tmpIns = insIter.next();
                loadAllOpRelOfIndividual(tmpIns,provider);
            }catch (Exception e){
                System.out.println("在写入实例Node:" + getPreLabel(tmpIns.getURI()) + "和其他实例之间的所有对象关系时报错!");
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 得到一个Uri的简称,比如 'http://www.w3.org/2002/07/owl#DatatypeProperty'的简称是'owl:DatatypeProperty'
     * @param uri
     * @return
     */
    private String getPreLabel(String uri){
        if(!uri.contains("#")){
            throw new InvalidParameterException("不是合法的uri!");
        }
        return nsMap.get(uri.substring(0,uri.indexOf("#") + 1)) + ":" + uri.substring(uri.indexOf("#") + 1);
    }

}
