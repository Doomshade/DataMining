package cz.zcu.jsmahy.datamining;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import cz.zcu.jsmahy.datamining.api.DataNode;
import cz.zcu.jsmahy.datamining.api.DataNodeFactory;
import cz.zcu.jsmahy.datamining.api.DataNodeRoot;
import cz.zcu.jsmahy.datamining.api.dbpedia.DBPediaModule;

import java.util.Arrays;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since
 */
public class Testttt {
    @Inject
    private DataNodeFactory<String> dataNodeFactory;

    public static void main(String[] args) {
        new Testttt().test();
    }

    private void test() {
        final Injector injector = Guice.createInjector(new DBPediaModule());
        injector.injectMembers(this);
        final DataNodeRoot<String> root = dataNodeFactory.newRoot();

        final DataNode<String> kaja4 = dataNodeFactory.newNode("karel iv");
        kaja4.addChild(dataNodeFactory.newNode("john of bohemia"));
        kaja4.addChild(dataNodeFactory.newNode("ludvik bavorsky"));

        root.addChild(kaja4);
        root.addChild(dataNodeFactory.newNode("john of bohemia"));
        root.addChild(dataNodeFactory.newNode("pred john of bohemia"));
        root.iterate((node, depth) -> {
            final char[] indent = new char[depth];
            Arrays.fill(indent, '\t');
            System.out.print(indent);
            System.out.println(node.getData());
        });
    }
}
