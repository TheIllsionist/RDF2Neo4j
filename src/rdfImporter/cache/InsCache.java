package rdfImporter.cache;

import connection.Neo4jConnection;
import org.apache.jena.ontology.Individual;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by The Illsionist on 2018/8/16.
 * 实例缓存,多线程读,多线程写
 * 虽然存在多个线程写实例缓存,但是因为每个线程所写内容不存在交集,因此即使存在“先检查-后执行”竞态条件,仍能保证线程安全性
 */
public class InsCache {

    private final static int DEFAULT_INSCOUNT = 3000;  //TODO:默认初始容量的选择还有待调研
    private final static ConcurrentHashMap<String,Individual> individuals = new ConcurrentHashMap<>();

    static {
        Record rec = null;
        StatementResult mRst = Neo4jConnection.getSession().run("match(ins:OWL_NAMEDINDIVIDUAL) return ins.preLabel");
        while(mRst.hasNext()){
            rec = mRst.next();
            String ins = rec.get(0).asString();
            individuals.put(ins,null);
        }
    }

    public static boolean insContained(String preLabel){
        return individuals.containsKey(preLabel);
    }

    public static void addIndividual(String preLabel){
        individuals.put(preLabel,null);
    }

}
