package test;

import Model.Relation;
import TaskThread.ImportClassThread;
import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.OntClass;
import rdfImporter.ClassImporter;
import rdfImporter.InsImporter;
import rdfImporter.PropImporter;
import rdfImporter.impl.cypherImpl.CypherClassImporter;
import rdfImporter.impl.cypherImpl.CypherInsImporter;
import rdfImporter.impl.cypherImpl.CypherPropImporter;
import util.CLASS_REL;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by The Illsionist on 2018/8/16.
 */
public class Test {
    public static void main(String args[]) {
        RdfProvider rdfProvider = new FileRdfProvider("G:\\");
        ClassImporter clsImporter = new CypherClassImporter();
        PropImporter propImporter = new CypherPropImporter();
        InsImporter insImporter = new CypherInsImporter();
        //分组的量是可以由用户指定的,后面想想如何实现程序自主调控
        int clsGroup = 50;
        int clsRelGroup = 20;
        int propGroup = 20;
        int propRelGroup = 15;
        int insGroup = 600;
        int insRelGroup = 300;
        //导入类任务
        Iterator<OntClass> clsIter = rdfProvider.allOntClasses().iterator();
        while (true) {
            Queue<OntClass> classes = new LinkedList<>();
            int count = 0;
            while (clsIter.hasNext() && ++count < clsGroup) {
                classes.add(clsIter.next());
            }
            new Thread(new ImportClassThread(classes, clsImporter)).start();
            if (!clsIter.hasNext()) {  //已经分配任务完成
                break;
            }
        }
        while(Thread.activeCount() > 1){  //等待类导入完成
            Thread.yield();
        }
        Iterator<Relation<OntClass, CLASS_REL>> clsRelIter = rdfProvider.allClassRels().iterator();
        while(true){
            Queue<>
        }
    }
}
