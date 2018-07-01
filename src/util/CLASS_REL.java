package util;

/**
 * owl中类与类之间的关系
 */
public enum CLASS_REL {
    SUBCLASS_OF,  //父子关系(A类是B类的子类)
    EQUIVALENT_TO, //等价关系(当A类和B类等价时,A类可以在任何位置替换B类)
    DISJOINT_WITH,  //不相交关系(A类和B类不相交时,A类和B类没有公共的实例)
    //此外还有补集关系,并集关系,交集关系等目前未设计如何表示
}
