package test;

/**
 * Created by The Illsionist on 2018/8/16.
 */
public class Test {
    public static void main(String args[]) {
//        RdfProvider rdfProvider = new FileRdfProvider("G:\\");
//        ClassImporter clsImporter = new CypherClassImporter();
//        PropImporter propImporter = new CypherPropImporter();
//        InsImporter insImporter = new CypherInsImporter();
//        //分组的量是可以由用户指定的,后面想想如何实现程序自主调控
//        int clsGroup = 50;  //导入类分组
//        int clsRelGroup = 20;  //导入类关系分组
//        int propGroup = 20;  //导入属性分组
//        int propRelGroup = 15;  //导入属性关系分组
//        int insGroup = 600;  //导入实例分组
//        int insRelGroup = 300;  //导入实例关系分组
//        //导入类任务
//        Iterator<OntClass> clsIter = rdfProvider.allOntClasses().iterator();
//        while (true) {
//            Queue<OntClass> classes = new LinkedList<>();
//            int count = 0;
//            while (clsIter.hasNext() && ++count < clsGroup) {
//                classes.add(clsIter.next());
//            }
//            new Thread(new ImportClassThread(classes, clsImporter)).start();
//            if (!clsIter.hasNext()) {  //已经分配任务完成
//                break;
//            }
//        }
//        while(Thread.activeCount() > 1){  //等待类导入完成
//            Thread.yield();
//        }
//        Iterator<Relation<OntClass, CLASS_REL>> clsRelIter = rdfProvider.allClassRels().iterator();
//        while(true){
//            Queue<Relation<OntClass,CLASS_REL>> clsRels = new LinkedList<>();
//            int count = 0;
//            while()
//        }
    }
}
