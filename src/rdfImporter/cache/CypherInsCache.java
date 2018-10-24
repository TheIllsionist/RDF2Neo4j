package rdfImporter.cache;

import connection.Neo4jConnection;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The Illsionist on 2018/8/16.
 * 实例缓存,多线程读,多线程写
 * 多线程读/写实例缓存,由于存在“先检查-后执行”竞态条件,因此必须要保证每个线程所写入的实例集间互不相交才可保证不重复写
 */
public class CypherInsCache {

    private final static int DEFAULT_INSCOUNT = 3000;  //TODO:默认初始容量的选择还有待调研
    private final static ConcurrentHashMap<String,Object> individuals = new ConcurrentHashMap<>();

    static {  //缓存所有已在数据库中存在的实例
        Record rec = null;
        StatementResult mRst = Neo4jConnection.getSession().run("match(ins:OWL_NAMEDINDIVIDUAL) return ins.preLabel");
        while(mRst.hasNext()){
            rec = mRst.next();
            String ins = rec.get(0).asString();
            if(ins == null || ins.equals("null"))  //防止null值出现
                continue;
            individuals.put(ins,new Object());
        }
    }

    /**
     * 判断某实例是否已存在于数据库中
     * @param preLabel
     * @return
     */
    public static boolean insContained(String preLabel){
        return individuals.containsKey(preLabel);
    }

    /**
     * 往缓存中加入新实例
     * @param preLabel
     */
    public static void addIndividual(String preLabel){
        individuals.put(preLabel,new Object());  //ConcurrentHashMap的key和value都不能为空
    }

}
