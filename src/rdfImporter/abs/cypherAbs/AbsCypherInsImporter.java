package rdfImporter.abs.cypherAbs;

import concurrentannotation.GuardedBy;
import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import org.apache.jena.ontology.Individual;
import org.neo4j.driver.v1.StatementResult;
import rdfImporter.InsImporter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by The Illsionist on 2018/8/13.
 */
public abstract class AbsCypherInsImporter implements InsImporter{
    /**
     * 实例缓存与实例关系缓存
     * 为提升性能,实现将实例和实例间关系同时导入,需要将实例和实例关系分别缓存
     * 两块缓存的读写分别由两个不同的锁保护,两块缓存的一致性通过程序实现维护
     * 存在多个线程读/写实例缓存,也存在多个线程读/写关系缓存,而在对两者的读写时又都需要解决“先检查-后执行”竞态条件,因此选择ReentrantReadWriteLock锁
     */
    @ThreadSafe
    protected static class CacheInstance{
        private final static int DEFAULT_INSCOUNT = 3000;
        private final static int DEFAULT_RELCOUNT = 6000;
        private final static ReentrantReadWriteLock insLock = new ReentrantReadWriteLock();
        private final static ReentrantReadWriteLock relLock = new ReentrantReadWriteLock();
        @GuardedBy("insLock") private final static Map<String,Individual> individuals = new HashMap<>();
        @GuardedBy("relLock") private final static Map<String,Map<String,String>> insRels = new HashMap<>();
        static {
            StatementResult mRst = Neo4jConnection.getSession().run("");
        }
    }
}
