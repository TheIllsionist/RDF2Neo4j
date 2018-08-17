package TaskThread;

import Model.Relation;
import org.apache.jena.ontology.OntClass;
import rdfImporter.ClassImporter;
import util.CLASS_REL;

import java.util.Queue;

/**
 * Created by The Illsionist on 2018/8/16.
 */
public class ImportClassRelThread implements Runnable {

    private final Queue<Relation<OntClass, CLASS_REL>> rels;
    private final ClassImporter importer;

    public ImportClassRelThread(Queue<Relation<OntClass, CLASS_REL>> rels,ClassImporter importer){
        this.rels = rels;
        this.importer = importer;
    }

    @Override
    public void run() {
        try{
            Relation<OntClass, CLASS_REL> rel = null;
            while(!rels.isEmpty()){
                rel = rels.poll();
                if(!importer.loadClassRelIn(rel.getFirst(),rel.getSecond(),rel.getRel())){
                    rels.offer(rel);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
