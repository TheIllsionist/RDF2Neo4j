package Appender.impl;

import Appender.Appender;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import util.CLASS_REL;
import util.INSTANCE_REL;
import util.PROPERTY_REL;

/**
 * Created by The Illsionist on 2018/8/28.
 */
public class SqlAppender implements Appender{
    
    @Override
    public String intoCls(OntClass ontClass) throws Exception {
        return null;
    }

    @Override
    public String intoProp(OntProperty ontProperty) throws Exception {
        return null;
    }

    @Override
    public String intoIns(Individual individual) throws Exception {
        return null;
    }

    @Override
    public String intoRel(OntClass class1, OntClass class2, CLASS_REL rel) throws Exception {
        return null;
    }

    @Override
    public String intoRel(OntProperty prop1, OntProperty prop2, PROPERTY_REL rel) throws Exception {
        return null;
    }

    @Override
    public String intoRel(Individual ins1, Individual ins2, INSTANCE_REL rel) throws Exception {
        return null;
    }

    @Override
    public String intoRel(Individual ins1, Individual ins2, ObjectProperty prop) throws Exception {
        return null;
    }
}
