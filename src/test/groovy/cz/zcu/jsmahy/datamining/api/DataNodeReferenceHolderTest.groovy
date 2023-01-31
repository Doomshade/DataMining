package cz.zcu.jsmahy.datamining.api

import com.google.inject.Guice
import javafx.collections.FXCollections
import spock.lang.Shared
import spock.lang.Specification

class DataNodeReferenceHolderTest extends Specification {
    @Shared
    static DataNodeFactory nodeFactory

    void setupSpec() {
        def mocks = new Mocks()
        def injector = Guice.createInjector(mocks.module())
        nodeFactory = injector.getInstance(DataNodeFactory)
    }


    @Shared
    DataNodeReferenceHolder<?> ref
    @Shared
    DataNodeRoot root

    void setup() {
        ref = new DataNodeReferenceHolder<>()
        root = nodeFactory.newRoot("Root")
    }

    def "Should set restrictions"() {
        when:
        ref.setRestrictions(FXCollections.observableArrayList(new Restriction("namespace", "link")))

        then:
        ref.getRestrictions().size() == 1
    }

    // TODO: rename
    def "Should return a valid reference when one is set and have a single reference"() {
        when:
        ref.set(nodeFactory.newNode(root))

        then:
        ref.get() != null
        !ref.hasMultipleReferences()
    }

    def "Should return a valid reference when one is set and have a single reference"() {
        when:
        ref.set(Arrays.asList(nodeFactory.newNode(root)))

        then:
        ref.get() != null
        !ref.hasMultipleReferences()
    }

    def "Should return a null reference when none is set and have a single reference"() {
        expect:
        ref.get() == null
        !ref.hasMultipleReferences()
    }

    def "Should throw ISE if multiple references are set"() {
        given:
        ref.add(nodeFactory.newNode(root))
        ref.add(nodeFactory.newNode(root))

        when:
        ref.get()

        then:
        thrown(IllegalStateException)
    }

    def "Should return list of references and have multiple references"() {
        when:
        ref.add(nodeFactory.newNode(root))
        ref.add(nodeFactory.newNode(root))

        then:
        ref.getList().size() == 2
        ref.hasMultipleReferences()
    }
}
