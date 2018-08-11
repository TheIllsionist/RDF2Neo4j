//查询所有类的preLabel
match(cls:OWL_CLASS) return cls.preLabel as preLabel;

//查询所有的类的preLabel以及它们间的关系,1表示子类,2表示等价,3表示不相交
match(cls:OWL_CLASS)
optional match(cls)-[r:RDFS_SUBCLASSOF|EQUIVALENT_CLASS|DISJOINT_CLASS]->(anCls:OWL_CLASS)
return cls.preLabel as cls,
	   case r.preLabel
	   		when "rdfs:subClassOf" then 1
	   		when "owl:equivalentClass" then 2
	   		when "owl:disjointWith" then 3
	   	end as index,
	   	anCls.preLabel as anCls

//查询所有属性的preLabel
match(pro:OWL_DATATYPEPROPERTY) return pro.preLabel as preLabel
union
match(pro:OWL_OBJECTPROPERTY) return pro.preLabel as preLabel

//查询所有属性的preLabel以及它们之间的关系,1表示子属性关系,2表示等价,3表示不相交,4表示语义反转
match(pro:OWL_DATATYPEPROPERTY)
optional match(p)-[r:RDFS_SUBPROPERTYOF|:EQUIVALENT_PROPERTY|:DISJOINT_PROPERTY|:INVERSE_OF]->(anop:OWL_DATATYPEPROPERTY)
return p.preLabel as p, case r.preLabel when "rdfs:subPropertyOf" then 1 when "owl:equivalentProperty" then 2
                       when "owl:disjointProperty" then 3 when "owl:inverseOf" then 4
       end as index,
       anop.preLabel as anop
union
match(pro:OWL_OBJECTPROPERTY)
optional match(p)-[r:RDFS_SUBPROPERTYOF|:EQUIVALENT_PROPERTY|:DISJOINT_PROPERTY|:INVERSE_OF]->(anop:OWL_OBJECTPROPERTY)
return p.preLabel as p,
       case r.preLabel when "rdfs:subPropertyOf" then 1 when "owl:equivalentProperty" then 2
                       when "owl:disjointProperty" then 3 when "owl:inverseOf" then 4
       end as index,
       anop.preLabel as anop