package cypherelement.basic;

import org.apache.commons.lang.builder.EqualsBuilder;
import java.util.List;
import java.util.Map;

/**
 * Cypher取值对象,本质上是一个Literal
 * Neo4j的Cypher支持多种数据结构,在当前对象中提供了对基本数据类型,List和Map的支持(目前Neo4j社区版不支持Map数据类型)
 */
public class CypherValue implements ToCypher {

    private Object value = null;  //值对象
    private DataType dataType = null;  //值的数据类型,如果值是List或Map,则代表List中每个元素或者Map中每个键值对的值的类型
    private int valFormat = 0;  //值的格式,0表示单值,1表示List,2表示Map

    public CypherValue(String value){
        this(value,DataType.STR,0);
    }

    public CypherValue(int value){
        this(value,DataType.INT,0);
    }

    public CypherValue(double value){
        this(value,DataType.DOUBLE,0);
    }

    public CypherValue(Object value,DataType dataType,int valFormat){
        this.value = value;
        this.dataType = dataType;
        this.valFormat = valFormat;
        if(valFormat == 0){  //在要求单值格式情况下传入一个List的时候需要提取值
            if(this.value instanceof List){
                this.value = ((List) this.value).get(0);
            }
        }
    }

    public Object getValue() {
        return value;
    }

    public DataType getDataType() {
        return dataType;
    }


    public int getValFormat(){
        return this.valFormat;
    }


    @Override
    public String toCypherStr(){
        return toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        switch (valFormat){
            case 0:{  //单个属性值
                builder.append(dataType == DataType.INT ? (int)value : dataType == DataType.DOUBLE ? (double)value : "\"" + String.valueOf(value) + "\"");
            }break;
            case 1:{ //列表属性值
                builder.append("[");
                List<Object> vals = (List<Object>)value;
                for (Object val:vals) {
                    builder.append(dataType == DataType.INT ? (int)val : dataType == DataType.DOUBLE ? (double)val : "\"" + String.valueOf(val) + "\"");
                    builder.append(",");
                }
                builder.delete(builder.length() - 1,builder.length());  //去掉最后一个逗号
                builder.append("]");
            }break;
            case 2:{ //字典属性值
                builder.append("[");
                Map<String,Object> vals = (Map<String, Object>)value;
                for(Map.Entry<String,Object> pair : vals.entrySet()){
                    builder.append("{");
                    builder.append(pair.getKey()).append(":");
                    builder.append(dataType == DataType.INT ? (int)pair.getValue() : dataType == DataType.DOUBLE ? (double)pair.getValue() : "\"" + String.valueOf(pair.getValue()) + "\"");
                    builder.append("}");
                    builder.append(",");
                }
                builder.delete(builder.length() - 1,builder.length());  //去掉最后一个逗号
                builder.append("]");
            }
        }
        return builder.toString();
    }

    /**
     * Literal没有引用名称,所以返回空串
     * @return
     */
    @Override
    public String referenceName() {
        return "";
    }

    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || this.getClass() != obj.getClass()){
            return false;
        }
        CypherValue newVal = (CypherValue)obj;
        return new EqualsBuilder()
                .append(value,newVal.value)
                .append(dataType,newVal.dataType)
                .append(valFormat,newVal.valFormat)
                .isEquals();
    }
}
