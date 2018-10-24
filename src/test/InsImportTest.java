package test;

import Appender.Appender;
import Appender.impl.CpElementAppender;
import Model.Relation;
import TaskThread.ImportInsRelThread;
import TaskThread.ImportInsThread;
import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import rdfImporter.InsImporter;
import rdfImporter.impl.CypherInsImporter;
import util.Words;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by The Illsionist on 2018/10/16.
 */
public class InsImportTest {
    public static void main(String args[]){
        RdfProvider rdfProvider = new FileRdfProvider("G:\\");
        Appender appender = new CpElementAppender();
        InsImporter importer = new CypherInsImporter(appender);
        Queue<Individual> inses = rdfProvider.allIndividuals();
        Queue<Relation<Individual,ObjectProperty>> insObjRels = rdfProvider.relsBetweenIndividuals();
        Queue<Relation<Individual, Words>> insWordRels = rdfProvider.relsBetweenIndividuals(Words.OWL_SAME_AS);
        insWordRels.addAll(rdfProvider.relsBetweenIndividuals(Words.OWL_DFINS));
        int groupCount = 200;
        int tCount = 0;
        while(true){
            Queue<Individual> tmp = new LinkedList<>();
            while(!inses.isEmpty() && groupCount >= 0){
                tmp.offer(inses.poll());
                groupCount--;
            }
            new Thread(new ImportInsThread(tmp,importer)).start();  //启动一个线程
            System.out.println("线程数: " + tCount++);
            if(inses.isEmpty())
                break;
            groupCount = 200;
        }
        new Thread(new ImportInsRelThread(insObjRels,importer)).run();
    }
}
