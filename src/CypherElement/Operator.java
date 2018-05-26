package CypherElement;

public enum  Operator{
    EQ_TO,  //等于
    UNEQ_TO,  //不等于
    BIG_THAN,  //大于
    SM_THAN,  //小于
    BIG_OR_EQ,  //大于等于
    SM_OR_EQ, //小于等于
    IN,  //在某些值中
    CONTAINS,  //包含某值
    STARTS_WITH,  //以某值为开始
    ENDS_WITH,  //以某值为结束
    IS_NULL,  //某值为NULL
    IS_NOT_NULL,  //某值不为NULL
    MATCH  //字符串匹配某正则表达式
    ;


    @Override
    public String toString() {
        switch (this){
            case EQ_TO:return " = ";
            case UNEQ_TO:return " <> ";
            case BIG_THAN:return " > ";
            case SM_THAN:return " < ";
            case BIG_OR_EQ:return " >= ";
            case SM_OR_EQ:return " <= ";
            case IN:return " IN ";
            case CONTAINS:return " CONTAINS ";
            case STARTS_WITH:return " STARTS WITH ";
            case ENDS_WITH:return " ENDS WITH ";
            case IS_NULL:return " IS NULL ";
            case IS_NOT_NULL:return " IS NOT NULL ";
            case MATCH:return " =~ ";
        }
        return " = ";
    }
}
