## OWL2Neo4j转储规则 ##

版本：0.6

+ 转储原则：Neo4j只起到存储本体知识的作用，虽可满足部分查询，但目前无法基于Neo4j存储进行推理
+ 转储内容：大体（因为部分数据将模式层和实例层关联在了一起）可分为模式层转储和实例层转储
+ 转储特点：
  + 不丢失任何RDF中包含的信息；
  + 可逆转性，即可以由RDF的数据转回Neo4j，这是因为目前推理还是要基于OWL才可

#### 1.基础概念 ####

+ OWL知识表示规范（必看）
+ RDB2OWL转换规范（选看）
+ Neo4j基本元素
  + Node：可以拥有多个Label和多个Property
  + Relationship：可以拥有一个Type和多个Property
  + OWL中的某个Resource既可以用Node表示，也可以用Relationship表示

#### 2.转储模式层 ####

+ 需转储的模式层内容
  + OWL类、属性，都转储为Neo4j中的Node
  + OWL中类与类间、属性与属性间的父子关系，转储为表示类、属性的Node之间的Relationship
  + OWL中类与类间、属性与属性间的其他关系，比如等价，转储为表示类、属性的Node间的Relationship
  + OWL中属性的定义域、值域，转储为表示属性和类的Node间的Relationship
  + OWL中对于某属性的特性，比如定义某个属性是自反的，用类似于属性定义的方式存储（下文详述）
  + 类、属性的描述，如rdfs:label、rdfs:comment，转储为Relationship，从类、属性Node指向值Node


+ 转储方法
  + 初始化：将表达基础语义的OWL词汇作为Node写入Neo4j，这些词汇Node在每个库中只能有一个
    + owl:Class
    + owl:DatatypeProperty、owl:ObjectProperty 、owl:FunctionalProperty、owl:TransitiveProperty
    + owl:NamedIndividual  等等
  + 类与属性定义：为每个类、属性都建一个Node
    + 类Node**通过表示rdf:type的Relationship**指向表示owl:Class的Node
    + 根据是数据类型属性/对象属性将属性Node通过表示rdf:type的Relationship指向表示owl:DatatypeProperty或owl:ObjectProperty的Node
  + 类间关系与属性间关系：用Relationship表示类、属性Node间的各种关系，部分关系列举如下：
    + rdfs:subClassOf、owl:equivalentClass、owl:disjointWith
    + rdfs:subPropertyOf、owl:equivalentProperty、owl:inverseOf
  + 定义域值域：
    + 属性A的定义域是类B，加入从A(Node)到B(Node)之间表示rdfs:domain的Relationship
    + 属性A的值域是类B，加入从A(Node)到B(Node)之间表示rdfs:range的Relationship
  + 属性特性：（例子说明）
    + 属性A是功能性的，A(Node)通过表示rdf:type的Relationship指向owl:FunctionalProperty(Node)
    + 属性A是传递性的，A(Node)通过表示rdf:type的Relationship指向owl:TransitiveProperty(Node)等
  + 描述：（例子说明）
    + 类A的可读名为"A"，A(Node)通过表示rdfs:label的Relationship指向值为"A"的值Node
+ **思考**：复杂的类定义形式，如通过**属性限制、值约束、类交集、类并集**等定义的类，如何存储在Neo4j中？

#### 3.转储实例层 ####

+ 需转储的实例层内容
  + OWL实例，作为Neo4j中的Node存储
  + 每个实例与其所属的类之间的isA关系，用表示rdf:type的Relationship存储
  + 实例的数据类型属性，在实例层数据类型属性被存储为由属性的所有者指向属性取值的Relationship
  + 实例的对象属性，即实例之间的关系，存储为Relationship，但中间加入中介节点作为中转（下文详述）
  + 实例间的其他类关系，比如两个实例的sameAs关系等，直接通过表示owl:sameAs的Relationship相连
+ 转储方法
  + 实例定义：为每个实例新建Node，并通过表示rdf:type的Relationship指向owl:NamedIndividual(Node)
  + 实例所属类：为实例和它所属的每个类间创建表示rdf:type的Relationship，由实例Node指向类Node
  + 数据类型属性：
    + 由上文知，在模式层中数据类型属性作为Node存储
    + 实例层中数据类型属性作为Relationship存储，由某个实例Node指向该属性取值的值Node
  + 对象属性：
    + 由上文知，模式层中对象属性也是作为Node存储
    + 实例层中对象属性作为Relationship存储，但正如**OWL知识表示规范**中所注，表示对象属性的Relationship并不直接与目标Node连接，而是与一个中介Node连接，然后由该中介Node通过表示**meta:实例**的Relationship与目标Node连接
  + 实例间的其他关系：通过表示这些关系的Relationship直接相连，这些关系包括
    + owl:sameAs
    + owl:differentFrom 等等

​        在所有以上这些存储中，对于一个资源，无论它是被存储为Node还是Relationship，该资源的uri和preLabel都作为Property携带在Node或Relationship上。对于数据类型属性和对象属性，在模式层中作为Node存储，而它们的rdfs:label作为Relationship与它们相连；但在实例层中属性都作为Relationship存储，则此时它们的rdfs:label是作为Relationship的Property携带的，如果某个属性的rdfs:label有多个，则该Property的取值就是一个列表。

**结语**：

+ 目前的转储规范仍然只能转储简单OWL的知识，对于涉及到需要通过集合表示的复杂类定义，实例关系定义等等的知识仍然无法存储
+ 从查询的角度，利用图数据库进行存储和直接利用RDF数据库进行存储两者的查询效率还没有进行过比较，**目前没有发现任何使用图数据库存储本体知识的优势**

