package cz.zcu.jsmahy.datamining.api.dbpedia

import com.google.inject.Guice
import com.google.inject.Injector
import cz.zcu.jsmahy.datamining.api.DataNodeFactory
import cz.zcu.jsmahy.datamining.query.RequestHandler
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Jakub Šmrha
 * @version $VERSION
 * @since 19.12.2022
 */
class DBPediaSpecification extends Specification {
    @Shared
    static Injector injector
    @Shared
    static DataNodeFactory<?> nodeFactory

    void setupSpec() {
        injector = Guice.createInjector(new DBPediaModule())
        nodeFactory = injector.getInstance(DataNodeFactory)
    }


    def "Should throw NPE when passing null query to request handler"() {
        given:
        def requestHandler = injector.getInstance(RequestHandler.class)

        when:
        requestHandler.query(null, null)

        then:
        thrown(NullPointerException)
    }
}
