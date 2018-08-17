package test;

import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.OntClass;
import rdfImporter.ClassImporter;
import rdfImporter.impl.cypherImpl.CypherClassImporter;
import util.CLASS_REL;

import java.util.Iterator;
import java.util.Queue;
import java.util.Set;


/**
 * Created by The Illsionist on 2018/7/1.
 */
public class ClassImportTest {
    public static void main(String args[]) throws Exception {
        RdfProvider rdfProvider = new FileRdfProvider("F:\\");
        ClassImporter importer = new CypherClassImporter();
        Set<OntClass> classes = rdfProvider.allOntClasses();
        Queue<Pair<OntClass,OntClass>> subClsRels = rdfProvider.allSubClassOfRels();
        Queue<Pair<OntClass,OntClass>> equClsRels = rdfProvider.allEqualClassRels();
        Queue<Pair<OntClass,OntClass>> disClsRels = rdfProvider.allDisJointClassRels();
        Thread classThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Iterator<OntClass> classIterator = classes.iterator();
                OntClass tmp = null;
                try{
                    while(classIterator.hasNext()){
                        tmp = classIterator.next();
                        importer.loadClassIn(tmp);
                    }
                }catch (Exception e){
                    System.out.println("在导入类: " + tmp.getURI() + "报错!");
                    e.printStackTrace();
                }
            }
        });

        Thread subRelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<OntClass,OntClass> tmpRel = null;
                try{
                    while(!subClsRels.isEmpty()){
                        tmpRel = subClsRels.poll();
                        if(!importer.loadClassRelIn(tmpRel.getFirst(),tmpRel.getSecond(), CLASS_REL.SUBCLASS_OF)){
                            subClsRels.offer(tmpRel);
                        }
                    }
                }catch (Exception e){
                    System.out.println("在导入父子类关系: 子-" + tmpRel.getFirst().getURI() +
                            ", 父-" + tmpRel.getSecond().getURI() + "时出错!");
                    e.printStackTrace();
                }
            }
        });
        Thread equRelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<OntClass,OntClass> tmpRel = null;
                try{
                    while(!equClsRels.isEmpty()){
                        tmpRel = equClsRels.poll();
                        if(!importer.loadClassRelIn(tmpRel.getFirst(),tmpRel.getSecond(), CLASS_REL.EQUIVALENT_CLASS)){
                            equClsRels.offer(tmpRel);
                        }
                    }
                }catch (Exception e){
                    System.out.println("在导入等价类关系: 类1-" + tmpRel.getFirst().getURI() +
                            ", 类2-" + tmpRel.getSecond().getURI() + "时出错!");
                    e.printStackTrace();
                }
            }
        });
        Thread disRelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<OntClass,OntClass> tmpRel = null;
                try{
                    while(!disClsRels.isEmpty()){
                        tmpRel = disClsRels.poll();
                        if(!importer.loadClassRelIn(tmpRel.getFirst(),tmpRel.getSecond(), CLASS_REL.DISJOINT_CLASS)){
                            disClsRels.offer(tmpRel);
                        }
                    }
                }catch (Exception e){
                    System.out.println("在导入不相交类关系: 类1-" + tmpRel.getFirst().getURI() +
                            ", 类2-" + tmpRel.getSecond().getURI() + "时出错!");
                    e.printStackTrace();
                }
            }
        });
        classThread.start();
        subRelThread.start();
        equRelThread.start();
        disRelThread.start();
    }
}
