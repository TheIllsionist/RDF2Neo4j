package cypherelement.basic;

class CypherLeftRelationship extends CypherRelationship{

    public String toCypherStr(){
        return new StringBuilder().append("<-").append(super.toCypherStr()).append("-").toString();
    }

}