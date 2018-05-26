package CypherElement;

public class CypherRightRelationship extends CypherRelationship{  //右连接关系,由类名表达右连接语义

    @Override
    public String toCypherStr(){
        return new StringBuilder().append("-").append(super.toCypherStr()).append("->").toString();
    }

}
