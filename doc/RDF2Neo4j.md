## RDF到Neo4j的转储规则 ##

版本：0.7

转储原则：转储规则的设计本着“简单，直觉上直接”的原则，这样容易让人理解，后续便于大家一起扩展

转储步骤：先进行模式层的转换，再进行实例层的转换

#### 基础概念 ####

+ RDF知识表示规范（必看）以及RDB2RDF转换规范（选看）
+ 理解Neo4j的基本元素
  + Node：可以拥有多个Label和多个Property
  + Relationship：可以拥有一个Relationship Type和多个Property
  + Node和Relationship都可以代表RDF中的Resource

#### 模式层转换 ####

​    转换模式层时，RDF中的类和属性都用Neo4j中的Node表示，这样便于后面定义类之间和属性之间的父子关系

+ 为定义类和属性，先把几个OWL词汇作为Node写入数据库（下面几个Node一个库中只有一个）：
  + owl:Class，以 rdf:type关系（Relationship）指向该Node的Node代表类
  + owl:DatatypeProperty，以rdf:type关系指向该Node的Node代表数据类型属性
  + owl:ObjectProperty，以rdf:type关系指向该Node的Node代表对象属性
  + owl:NamedIndividual，以rdf:type关系指向该Node的Node代表实例
+ 除了上面提到的rdf:type，还有几个词汇也作为Relationship：
  + rdfs:subClassOf，如果node1以该关系指向node2，则node1是node2的子类
  + rdfs:subPropertyOf，如果node1以该关系指向node2，则node1是node2的子属性
  + rdfs:domain，如果node1以该关系指向node2，则node1的定义域是node2代表的类
  + rdfs:range，如果node1以该关系指向node2，则node1的值域是node2代表的类
+ 对于某个类或者某个属性的描述信息，比如rdfs:label或者rdfs:comment，作为表示该类或该属性的Node的Property
+ 思考：在RDF中，当某个属性的定义域或者值域是多个类的并集或者交集时，如何将这种情况用Neo4j存储?

#### 实例层转换 ####

+ 实例作为Neo4j的Node存在，该Node除了有一个rdf:type的Relationship指向owl:NamedIndividual Node之外，还可以有多个rdf:type的Relationship指向多个其他代表类的Node，表示该实例属于哪些类
+ 实例的数据类型属性作为表示实例的Node的Property存在
+ 实例的对象属性作为表示该实例的Node与其他Node之间的Relationship存在，CVT类型的对象属性的其他属性作为该Relationship的Property存在

接下来要解决的问题：Neo4j是否支持对uri相同的一个Node和一个Relationship在uri属性上构建索引?

如何应对sparql中类似 " insA pro* ?x   和 proA  sub*  ?proB"的查询？