package Main;

import RDFLoader.impl.CypherResourceLoader;
import dataSource.impl.FileRdfProvider;

public class Test1 {

    public static void main(String args[]){
        FileRdfProvider fileRdfProvider = new FileRdfProvider("F:\\WGBQ_V3.owl");
        CypherResourceLoader loader = new CypherResourceLoader("bolt://localhost:7687","neo4j","kseqa");
        loader.initGraph();  //首先要初始化
        loader.loadAllOntClasses(fileRdfProvider);  //加载本体模型中的所有类通过
        loader.loadAllOntProperties(fileRdfProvider); //加载本体模型中的所有属性(目前仅限数据类型属性和对象属性)通过
        loader.loadAllIndividuals(fileRdfProvider);  //加载本体模型中的所有实例(已将实例的数据类型属性作为Node的Property写入)通过
        loader.loadAllSubClassOfRel(fileRdfProvider);  //加载模型中所有的rdfs:subClassOf关系 通过
        loader.loadAllSubPropOfRel(fileRdfProvider); //加载模型中的所有rdfs:subPropertOf关系 通过
        loader.loadAllIsARel(fileRdfProvider);   //加载模型中的有关所有实例的IsA(rdf:type)关系 通过
        loader.loadAllObjectPropBetweenIns(fileRdfProvider); //加载模型中所有实例与实例之间的对象属性关系
        //TODO:1.需要为property加入Domain和Range,需要写入,但是暂时不写(后面再修改)
        System.out.println();
    }

}
