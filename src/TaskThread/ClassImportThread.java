package TaskThread;

import org.apache.jena.ontology.OntClass;
import rdfImporter.ClassImporter;

import java.util.Queue;

/**
 * Created by The Illsionist on 2018/8/16.
 */
public class ClassImportThread implements Runnable{

    private final Queue<OntClass> classes;  //一批任务
    private final ClassImporter importer;  //导入器

    public ClassImportThread(Queue<OntClass> classes,ClassImporter importer){
        this.classes = classes;
        this.importer = importer;
    }

    @Override
    public void run() {
        try{
            while(!classes.isEmpty()){
                importer.loadClassIn(classes.poll());
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);  //先退出,后续这里可能要做保持知识库和缓存一致性的处理
        }
    }
}
