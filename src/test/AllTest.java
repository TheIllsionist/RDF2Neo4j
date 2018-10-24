package test;

import Appender.Appender;
import Appender.impl.CpElementAppender;
import Model.Relation;
import TaskThread.*;
import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import rdfImporter.ClassImporter;
import rdfImporter.InsImporter;
import rdfImporter.PropImporter;
import rdfImporter.impl.CypherClassImporter;
import rdfImporter.impl.CypherInsImporter;
import rdfImporter.impl.CypherPropImporter;
import util.Words;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by The Illsionist on 2018/10/24.
 */
public class AllTest {
    public static void main(String args[]){
        RdfProvider rdfProvider = new FileRdfProvider("G:\\");
        int tCount = 1;   //线程数统计
        Appender appender = new CpElementAppender();
        ClassImporter clsIpt = new CypherClassImporter(appender);
        Queue<OntClass> classes = rdfProvider.allOntClasses();
        ImportClassThread clsIn = new ImportClassThread(classes,clsIpt);
        clsIn.run();   //要先把所有的类都导入才可以接着导入其他资源和关系
        //导入类间关系
        Queue<Relation<OntClass, Words>> clsRels = rdfProvider.allClassRels();
        ImportClassRelThread clsRelIn = new ImportClassRelThread(clsRels,clsIpt);
        new Thread(clsRelIn).start();
        tCount++;
        System.out.println("启动线程 " + tCount + " , 导入所有的类间关系");
        //导入属性及属性间关系
        PropImporter propIpt = new CypherPropImporter(appender);
        Queue<OntProperty> props = rdfProvider.allOntProperties();
        Queue<Relation<OntProperty,Words>> propRels = rdfProvider.allPropertyRels();
        ImportPropThread propIn = new ImportPropThread(props,propIpt);
        ImportPropRelThread propRelIn = new ImportPropRelThread(propRels,propIpt);
        new Thread(propIn).start();
        tCount++;
        System.out.println("启动线程 " + tCount + " , 导入所有属性");
        new Thread(propRelIn).start();
        tCount++;
        System.out.println("启动线程 " + tCount + " , 导入所有的属性间关系");
        //导入实例及实例间关系
        InsImporter insIpt = new CypherInsImporter(appender);
        Queue<Individual> inses = rdfProvider.allIndividuals();
        int insCount = 300;  //每组实例数
        while(true){
            Queue<Individual> tmp = new LinkedList<>();
            while(!inses.isEmpty() && insCount >= 0){
                tmp.offer(inses.poll());
                insCount--;
            }
            new Thread(new ImportInsThread(tmp,insIpt)).start();  //启动一个线程
            tCount++;
            System.out.println("启动线程 " + tCount + " , 导入 " + (300 - (insCount + 1)) + " 个实例");
            if(inses.isEmpty())
                break;
            insCount = 300;
        }
        Queue<Relation<Individual,ObjectProperty>> insObjRels = rdfProvider.relsBetweenIndividuals();
        int insRelCount = 300;
        while(true){
            Queue<Relation<Individual,ObjectProperty>> tmp = new LinkedList<>();
            while(!insObjRels.isEmpty() && insRelCount >= 0){
                tmp.offer(insObjRels.poll());
                insRelCount--;
            }
            new Thread(new ImportInsRelThread(tmp,insIpt)).start();
            tCount++;
            System.out.println("启动线程 " + tCount + " , 导入 " + (300 - (insRelCount + 1)) + " 条实例关系");
            if(insObjRels.isEmpty())
                break;
            insRelCount = 300;
        }
    }
}
