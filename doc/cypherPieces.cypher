//查询所有类的preLabel
match(cls:OWL_CLASS) return cls.preLabel as preLabel;

//查询所有的类的preLabel以及它们间的关系,0表示子类,1表示等价,2表示不相交
match(cls:OWL_CLASS)
optional match(cls)-[r:RDFS_SUBCLASSOF|EQUIVALENT_CLASS|DISJOINT_CLASS]->(anCls:OWL_CLASS)
return cls.preLabel as cls,
	   case r.preLabel
	   		when "rdfs:subClassOf" then 0
	   		when "owl:equivalentClass" then 1
	   		when "owl:disjointWith" then 2
	   	end as index,
	   	anCls.preLabel as anCls

//查询所有属性的preLabel