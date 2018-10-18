package Appender;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import util.Words;
import java.security.InvalidParameterException;
import java.util.HashMap;

/**
 * Created by The Illsionist on 2018/8/28.
 */
public abstract class Appender {

    private static final HashMap<String,String> nsMap;  //存储命名空间与简称对应

    static {
        nsMap = new HashMap<>();  //记录命名空间全称和简写的相互对应
        fillNsMap();         //将相互对应关系填充进去,后面可以修改为从配置文件中读取
    }

    /**
     * 将命名空间全称和前缀以及前缀和全称的对应加入nsMap
     */
    protected static void fillNsMap(){
        nsMap.put("http://kse.seu.edu.cn/rdb#","rdb");
        nsMap.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf");
        nsMap.put("http://www.w3.org/2000/01/rdf-schema#","rdfs");
        nsMap.put("http://www.w3.org/2002/07/owl#","owl");
        nsMap.put("http://www.w3.org/2001/XMLSchema#","xsd");
        nsMap.put("http://kse.seu.edu.cn/meta#","meta");
        nsMap.put("http://kse.seu.edu.cn/wgbq#","wgbq");
        nsMap.put("http://kse.seu.edu.cn/xgbg#","xgbg");
        nsMap.put("rdb","http://kse.seu.edu.cn/rdb#");
        nsMap.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        nsMap.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
        nsMap.put("owl","http://www.w3.org/2002/07/owl#");
        nsMap.put("xsd","http://www.w3.org/2001/XMLSchema#");
        nsMap.put("meta","http://kse.seu.edu.cn/meta#");
        nsMap.put("wgbq","http://kse.seu.edu.cn/wgbq#");
        nsMap.put("xgbg","http://kse.seu.edu.cn/xgbg#");
    }

    /**
     * 拼接初始化数据库的语句
     * @return
     * @throws Exception
     */
    public abstract String initBase() throws Exception;

    /**
     * 拼接导入类的语句
     * @param ontClass
     * @return
     * @throws Exception
     */
    public abstract String intoCls(OntClass ontClass) throws Exception;

    /**
     * 拼接导入属性的语句
     * @param ontProperty
     * @return
     * @throws Exception
     */
    public abstract String intoProp(OntProperty ontProperty) throws Exception;

    /**
     * 拼接导入实例的语句
     * @param individual
     * @return
     * @throws Exception
     */
    public abstract String intoIns(Individual individual) throws Exception;

    /**
     * 拼接导入两个类间关系的语句
     * @param class1
     * @param class2
     * @param rel
     * @return
     * @throws Exception
     */
    public abstract String intoRel(OntClass class1, OntClass class2, Words rel) throws Exception;

    /**
     * 拼接导入两个属性间关系的语句
     * @param prop1
     * @param prop2
     * @param rel
     * @return
     * @throws Exception
     */
    public abstract String intoRel(OntProperty prop1, OntProperty prop2, Words rel) throws Exception;

    /**
     * 拼接导入两个实例间语义关系的语句
     * @param ins1
     * @param ins2
     * @param rel 目前有owl:sameAs和owl:differentFrom
     * @return
     * @throws Exception
     */
    public abstract String intoRel(Individual ins1, Individual ins2, Words rel) throws Exception;

    /**
     * 拼接导入两个实例间属性关系的语句
     * @param ins1
     * @param ins2
     * @param prop
     * @return
     * @throws Exception
     */
    public abstract String intoRel(Individual ins1,Individual ins2,ObjectProperty prop) throws Exception;

    /**
     * 得到一个Uri的前缀,比如'http://www.w3.org/2002/07/owl#DatatypeProperty'的简称是'owl:DatatypeProperty'
     */
    public static String getPreLabel(String uri){
        if(!uri.contains("#")){
            throw new InvalidParameterException("非合法的uri!");
        }
        return nsMap.get(uri.substring(0,uri.indexOf("#") + 1)) + ":" + uri.substring(uri.indexOf("#") + 1);
    }

}
