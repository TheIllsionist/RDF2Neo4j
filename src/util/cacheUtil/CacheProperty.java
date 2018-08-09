package util.cacheUtil;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The Illsionist on 2018/7/17.
 * 属性缓存
 * 知识库中属性
 */
public class CacheProperty {

    private static ConcurrentHashMap<String,ConcurrentHashMap<String,Integer>> propWithRels = new ConcurrentHashMap<>();

    static {

    }

}
