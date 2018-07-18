package test;

import cypherelement.basic.CypherNode;
import cypherelement.basic.CypherProperty;
import cypherelement.basic.CypherValue;
import cypherelement.basic.PropValPair;
import cypherelement.clause.Cypher;
import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Literal;
import util.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by The Illsionist on 2018/7/1.
 */
public class FileRdfProviderTest {
    public static void main(String args[]){
        RdfProvider rdfProvider = new FileRdfProvider("F:\\");
        CypherNode clsWord = null;
        HashSet<PropValPair> props = new HashSet<>();
        props.add(new PropValPair(new CypherProperty("preLabel"),new CypherValue("owl:Class")));
        clsWord = new CypherNode("word","OWL_WORD",props){
            @Override
            public String propsToStr(){
                if(properties == null || properties.size() == 0){
                    return "";
                }
                StringBuilder builder = new StringBuilder();
                builder.append("{");
                for(PropValPair pair:properties){
                    builder.append(pair.toInnerString()).append(",");
                }
                builder.delete(builder.length() - 1,builder.length());
                builder.append("}");
                return builder.toString();
            }
        };
        System.out.println(clsWord.toCypherStr());
        System.out.println(new Cypher().match(clsWord).getCypher());
        clsWord.setProperties(null);
        clsWord.setLabel(null);
        System.out.println(clsWord.toCypherStr());
//        Thread classThread = new Thread(new Runnable() {    //迭代类的线程
//            @Override
//            public void run() {
//                int count = 0;
//                for (OntClass cls:rdfProvider.allOntClasses()) {
//                    Thread clsInsThread = new Thread(new Runnable() {  //在迭代类的线程中又分发迭代该类实例的线程
//                        @Override
//                        public void run() {
//                            for (Individual ins:rdfProvider.allIndividualsOfClass(cls)) {
//                                System.out.println("类" + cls.getURI() + "的实例:" + ins.getURI() + " ");
//                                //每个写入实例的线程都要迭代实例Dp
//                                Map<DatatypeProperty,List<Literal>> dpVals = rdfProvider.allDpValuesOf(ins);
//                                //对于Op的迭代,会在其他的线程中进行
//                                Map<ObjectProperty,List<Individual>> opVals = rdfProvider.allOpValuesOf(ins);
//                            }
//                        }
//                    });
//                    clsInsThread.start();
//                    System.out.println("当前活动线程数:" + Thread.activeCount());
//                    System.out.println(cls.getURI());
//                    count++;
//                }
//                System.out.println("类数目为 : " + count + " ");
//            }
//        });
//        Thread dpThread = new Thread(new Runnable() {  //迭代数据类型属性的线程
//            @Override
//            public void run() {
//                int count = 0;
//                for (OntProperty prop:rdfProvider.allDatatypeProperties()) {
//                    System.out.println(prop.getURI());
//                    count++;
//                }
//                System.out.println("Dp属性数目为 : " + count + " ");
//            }
//        });
//        Thread opThread = new Thread(new Runnable() {  //迭代对象属性的线程
//            @Override
//            public void run() {
//                int count = 0;
//                for (OntProperty prop:rdfProvider.allObjectProperties()) {
//                    System.out.println(prop.getURI());
//                    count++;
//                }
//                System.out.println("Op属性数目为 : " + count + " ");
//            }
//        });
//        Thread subClsOfThread = new Thread(new Runnable() { //迭代父子类关系的线程
//            @Override
//            public void run() {
//                int count = 0;
//                Queue<Pair<OntClass,OntClass>> pairs = rdfProvider.allSubClassOfRels();
//                while (!pairs.isEmpty()) {
//                    Pair<OntClass,OntClass> tmpPair = pairs.poll();
//                    System.out.println("类:" + tmpPair.getFirst().getURI() + "是类:" + tmpPair.getSecond().getURI() + "的子类");
//                    count++;
//                }
//                System.out.println("父子类关系数目为 : " + count + " ");
//            }
//        });
//        Thread subPropOfThread = new Thread(new Runnable() {  //迭代父子属性关系的线程
//            @Override
//            public void run() {
//                int count = 0;
//                Queue<Pair<OntProperty,OntProperty>> pairs = rdfProvider.allSubPropertyOfRels();
//                while (!pairs.isEmpty()) {
//                    Pair<OntProperty,OntProperty> tmpPair = pairs.poll();
//                    System.out.println("属性:      " + tmpPair.getFirst().getURI() + "      是属性:      " + tmpPair.getSecond().getURI() + "     的子属性");
//                    count++;
//                }
//                System.out.println("父子属性关系数目为 : " + count + " ");
//            }
//        });
//        classThread.start();
//        dpThread.start();
//        opThread.start();
//        subClsOfThread.start();
//        subPropOfThread.start();//TODO:出现空指针异常!!!! 由于topDatatypeProperty不能根据uri被查到(263行)的原因
    }
}
