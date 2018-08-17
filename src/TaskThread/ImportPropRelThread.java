package TaskThread;

import Model.Relation;
import org.apache.jena.ontology.OntProperty;
import rdfImporter.PropImporter;
import util.PROPERTY_REL;
import java.util.Queue;

/**
 * Created by The Illsionist on 2018/8/17.
 */
public class ImportPropRelThread implements Runnable{

    private final Queue<Relation<OntProperty, PROPERTY_REL>> rels;
    private final PropImporter importer;

    public ImportPropRelThread(Queue<Relation<OntProperty, PROPERTY_REL>> rels,PropImporter importer){
        this.rels = rels;
        this.importer = importer;
    }

    @Override
    public void run() {
        try{
            Relation<OntProperty, PROPERTY_REL> rel = null;
            while(!rels.isEmpty()){
                rel = rels.poll();
                if(!importer.loadPropertyRelIn(rel.getFirst(),rel.getSecond(),rel.getRel())){
                    rels.offer(rel);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
