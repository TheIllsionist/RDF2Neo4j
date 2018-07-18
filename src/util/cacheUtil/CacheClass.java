package util.cacheUtil;

import concurrentannotation.ThreadSafe;
import connection.Neo4jConnection;
import org.neo4j.driver.v1.StatementResult;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by The Illsionist on 2018/7/17.
 * 类缓存
 * 在知识库中类的数目是有限的,因此类缓存缓存全部的类
 * 只有一个线程写类缓存,多个线程读类缓存
 */
@ThreadSafe
public class CacheClass {

    private static HashSet<String> classes = new HashSet<>();  //类缓存,当前实现为只缓存preLabel字符串
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    static {  //类加载时即查询知识库中的已有类
        StatementResult res = Neo4jConnection.getSession().run("match(cls:OWL_CLASS) return cls.preLabel");
        while (res.hasNext()){
            classes.add(res.next().toString());
        }
    }

    /**
     * 判断某个类是否早已被写入知识库
     * @param preLabel &nbsp 唯一标识该类的preLabel(这只是目前的评价指标)
     * @return true表示该类已存在于知识库中,false表示该类未存在于知识库中(注:未存在可能出现误判,但程序容忍未存在误判)
     */
    public static boolean isContained(String preLabel){
        try{
            lock.readLock().lock();
            return classes.contains(preLabel);
        }finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 往缓存中写入新类,缓存写入紧接着知识库写入,因为只有一个线程写知识库和写缓存,所以知识库的写入和缓存的写入可以不在一个原子操作内
     * @param preLabel &nbsp 唯一标识该类的preLabel
     */
    public static void addClass(String preLabel){
        try{
            lock.writeLock().lock();
            classes.add(preLabel);
        }finally {
            lock.writeLock().unlock();
        }
    }


}
