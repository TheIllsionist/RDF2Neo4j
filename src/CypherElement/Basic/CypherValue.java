package CypherElement.Basic;

import org.apache.commons.lang.builder.EqualsBuilder;
import java.util.List;
import java.util.Map;

public class CypherValue extends CypherStr {

    private Object value = null;  //具体的值
    private DataType dataType = null;  //值的数据类型
    private int valFormat = 0;  //值的格式,0表示单值,1表示List,2表示Map


    private PropValPair belongs = null;   //该值所属的属性值对
    /**
     * 设置当前CypherValue所属的属性值对
     * TODO://注!该方法只能此包内调用,其实只能由此对象的所属对象调用
     */
    void setBelongs(PropValPair propValPair){
        this.belongs = propValPair;
    }



    public CypherValue(String value){
        this(value,DataType.STR);
    }

    public CypherValue(int value){
        this(value,DataType.INT);
    }

    public CypherValue(double value){
        this(value,DataType.DOUBLE);
    }

    public CypherValue(Object value,DataType dataType){
        this.value = value;
        this.dataType = dataType;
        if(value instanceof List){
            valFormat = 1;
        }
        if(value instanceof Map){
            valFormat = 2;
        }
        hasChanged();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        hasChanged();
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
        hasChanged();
    }

    public int getValFormat(){
        return this.valFormat;
    }

    /**
     * 当本对象被改变需要重新拼接时,如果本对象存在所属对象,要求所属对象也要重新拼接
     */
    private void hasChanged(){
        this.hasChanged = true;
        if(this.belongs != null){  //必须要判断所属对象是否为null
            this.belongs.hasChanged();
        }
    }

    @Override
    public String toCypherStr(){
        return toString();
    }

    public String toString() {
        if(!hasChanged){    //关键元素没有被改变过,不需重新拼接,直接返回原来拼接好的Cypher片段
            return cypherFragment;
        }
        //关键元素被改变,重新拼接然后再返回
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
        cypherFragment = builder.toString();
        hasChanged = false;
        return cypherFragment;
    }

    /**
     * 一个具体的Literal是没有引用名称的
     * @return
     */
    @Override
    public String referencedName() {
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
