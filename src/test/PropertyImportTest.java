package test;

import datasource.RdfProvider;
import datasource.impl.FileRdfProvider;
import org.apache.jena.ontology.OntProperty;
import rdfImporter.PropImporter;
import rdfImporter.impl.cypherImpl.CypherPropImporter;
import util.PROPERTY_REL;

import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

/**
 * Created by The Illsionist on 2018/8/15.
 */
public class PropertyImportTest {
    public static void main(String args[]) throws Exception {
        RdfProvider rdfProvider = new FileRdfProvider("G:\\");
        PropImporter importer = new CypherPropImporter();
        Set<OntProperty> props = rdfProvider.allOntProperties();
        Queue<Pair<OntProperty,OntProperty>> subProRels = rdfProvider.allSubPropertyOfRels();
        Queue<Pair<OntProperty,OntProperty>> equProRels = rdfProvider.allEqualPropertyRels();
        Queue<Pair<OntProperty,OntProperty>> disProRels = rdfProvider.allDisjointPropRels();
        Queue<Pair<OntProperty,OntProperty>> invProRels = rdfProvider.allInversePropRels();
        Thread propThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Iterator<OntProperty> propIterator = props.iterator();
                OntProperty tmp = null;
                try{
                    while(propIterator.hasNext()){
                        tmp = propIterator.next();
                        importer.loadPropertyIn(tmp);
                    }
                }catch (Exception e){
                    System.out.println("在导入属性: " + tmp.getURI() + "时报错!");
                    e.printStackTrace();
                }
            }
        });

        Thread subRelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<OntProperty,OntProperty> tmpRel = null;
                try{
                    while(!subProRels.isEmpty()){
                        tmpRel = subProRels.poll();
                        if(!importer.loadPropertyRelIn(tmpRel.getFirst(),tmpRel.getSecond(), PROPERTY_REL.SUBPROPERTY_OF)){
                            subProRels.offer(tmpRel);
                        }
                    }
                }catch (Exception e){
                    System.out.println("在导入父子属性关系: 子-" + tmpRel.getFirst().getURI() +
                            ", 父-" + tmpRel.getSecond().getURI() + "时出错!");
                    e.printStackTrace();
                }
            }
        });
        Thread equRelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<OntProperty,OntProperty> tmpRel = null;
                try{
                    while(!equProRels.isEmpty()){
                        tmpRel = equProRels.poll();
                        if(!importer.loadPropertyRelIn(tmpRel.getFirst(),tmpRel.getSecond(), PROPERTY_REL.EQUIVALENT_PROPERTY)){
                            equProRels.offer(tmpRel);
                        }
                    }
                }catch (Exception e){
                    System.out.println("在导入等价属性关系: 属性1-" + tmpRel.getFirst().getURI() +
                            ", 属性2-" + tmpRel.getSecond().getURI() + "时出错!");
                    e.printStackTrace();
                }
            }
        });
        Thread disRelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<OntProperty,OntProperty> tmpRel = null;
                try{
                    while(!disProRels.isEmpty()){
                        tmpRel = disProRels.poll();
                        if(!importer.loadPropertyRelIn(tmpRel.getFirst(),tmpRel.getSecond(), PROPERTY_REL.DISJOINT_PROPERTY)){
                            disProRels.offer(tmpRel);
                        }
                    }
                }catch (Exception e){
                    System.out.println("在导入不相交属性关系: 属性1-" + tmpRel.getFirst().getURI() +
                            ", 属性2-" + tmpRel.getSecond().getURI() + "时出错!");
                    e.printStackTrace();
                }
            }
        });
        Thread invRelThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<OntProperty,OntProperty> tmpRel = null;
                try{
                    while(!invProRels.isEmpty()){
                        tmpRel = invProRels.poll();
                        if(!importer.loadPropertyRelIn(tmpRel.getFirst(),tmpRel.getSecond(), PROPERTY_REL.INVERSE_OF)){
                            invProRels.offer(tmpRel);
                        }
                    }
                }catch (Exception e){
                    System.out.println("在导入相反语义属性关系: 属性1-" + tmpRel.getFirst().getURI() +
                            ", 属性2-" + tmpRel.getSecond().getURI() + "时出错!");
                    e.printStackTrace();
                }
            }
        });
        propThread.start();
        subRelThread.start();
        equRelThread.start();
        disRelThread.start();
        invRelThread.start();
    }
}
