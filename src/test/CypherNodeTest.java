package test;

import cypherelement.basic.CypherNode;
import cypherelement.basic.PropValPair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CypherNodeTest {

    public static void main(String args[]){
        Set<PropValPair> properties = new HashSet<>();
        properties.add(new PropValPair("uri","http://kse.seu.edu.cn/meta#海上平台"));
        properties.add(new PropValPair("preLabel","meta:海上平台"));
        List<String> labels = new ArrayList<>();
        labels.add("海上平台");
        labels.add("海上作战平台");
        PropValPair pair = new PropValPair("`rdfs:label`",labels);
        properties.add(pair);
        CypherNode node1 = new CypherNode(null,"OWL_CLASS",properties);
        System.out.println(node1.toInnerStr());
    }

}
