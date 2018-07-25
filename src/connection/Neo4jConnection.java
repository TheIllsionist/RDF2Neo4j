package connection;

import concurrentannotation.ThreadSafe;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

/**
 * 线程安全的Neo4j会话,用以获取Session和关闭Session
 */
@ThreadSafe
public class Neo4jConnection {

    private static Driver driver = null;  //静态成员,全局只有一个

    static {  //在静态初始化代码块中初始化,即在类加载时初始化
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","gwh"));
    }

    private static ThreadLocal<Session> session = new ThreadLocal<Session>(){
        @Override
        public Session initialValue(){
            return Neo4jConnection.driver.session();
        }
    };

    /**
     * 当某个线程第1次调用该方法时,会调用Neo4jSession.session的initialValue()方法,即给当前线程创建一个新的Neo4j的Session
     * 第2次调用该方法时则直接返回该线程绑定的session对象
     * @return
     */
    public static Session getSession(){
        return Neo4jConnection.session.get();
    }

    /**
     * 首先关闭与当前线程绑定的Session对象,这里只做关闭工作,清除工作留待线程销毁时进行
     */
    public static void closeSession(){
        Neo4jConnection.session.get().close();
    }

}
