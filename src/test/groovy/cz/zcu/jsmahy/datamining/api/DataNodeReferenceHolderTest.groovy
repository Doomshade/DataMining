package cz.zcu.jsmahy.datamining.api

import com.google.inject.Guice
import com.google.inject.Injector
import cz.zcu.jsmahy.datamining.query.Restriction
import javafx.collections.FXCollections
import spock.lang.Shared
import spock.lang.Specification

class DataNodeReferenceHolderTest extends Specification {
    @Shared
    static Injector injector
    @Shared
    static DataNodeFactory<?> nodeFactory

    void setupSpec() {
        injector = Guice.createInjector(new DataMiningModule() {})
        nodeFactory = injector.getInstance(DataNodeFactory)
    }


    @Shared
    DataNodeReferenceHolder<?> ref

    void setup() {
        ref = new DataNodeReferenceHolder<>()
    }

    def "GetRestrictions"() {

    }

    def "RestrictionsProperty"() {
    }

    def "Should set restrictions"() {
        when:
        ref.setRestrictions(FXCollections.observableArrayList(new Restriction("namespace", "link")))

        then:
        ref.getRestrictions().size() == 1
    }

    def "GetOntologyPathPredicate"() {
    }

    def "OntologyPathPredicateProperty"() {
    }

    def "SetOntologyPathPredicate"() {
    }

    def "HasMultipleReferences"() {
    }

    def "Should return a valid reference when one is set and have a single reference"() {
        when:
        ref.set(node)

        then:
        ref.get().getData() == _
        !ref.hasMultipleReferences()

        where:
        node << [nodeFactory.newNode(_) as DataNode<?>, Arrays.asList(nodeFactory.newNode(_)) as Collection<DataNode<?>>]
    }

    def "Should return a null reference when none is set and have a single reference"() {
        expect:
        ref.get() == null
        !ref.hasMultipleReferences()
    }

    def "Should throw ISE if multiple references are set"() {
        given:
        ref.add(nodeFactory.newNode(_))
        ref.add(nodeFactory.newNode(_))

        when:
        ref.get()

        then:
        thrown(IllegalStateException)
    }

    def "Add"() {
    }

    def "Get"() {
    }

    def "Should return list of references and have multiple references"() {
        when:
        ref.add(nodeFactory.newNode(_))
        ref.add(nodeFactory.newNode(_))

        then:
        ref.getList().size() == 2
        ref.hasMultipleReferences()
    }
}
