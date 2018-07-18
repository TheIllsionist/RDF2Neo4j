package util;

public enum PROPERTY_REL {
    SUBPROPERTY_OF,  //父子关系(A属性是B属性的子属性)
    EQUIVALENT_PROPERTY,   //等价关系(当A属性和B属性等价时,A属性可以在任何位置替换B属性)
    INVERSE_OF,    //相反关系(A属性和B属性刚好表达了相反的语义,比如 hasParent和hasChild )
    DISJOINT_PROPERTY  //不相交关系(A属性不可能和B属性共存于同样的两个个体之间)
}
