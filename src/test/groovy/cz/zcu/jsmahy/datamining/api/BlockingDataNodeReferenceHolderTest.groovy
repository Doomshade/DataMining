package cz.zcu.jsmahy.datamining.api

import spock.lang.Shared
import spock.lang.Specification

class BlockingDataNodeReferenceHolderTest extends Specification {
    @Shared
    BlockingDataNodeReferenceHolder ref

    void setup() {
        ref = new BlockingDataNodeReferenceHolder()
    }

    def "Reference should be finished once marked as finished"() {
        when:
        ref.finish()
        then:
        ref.isFinished()
    }

    def "Reference should be finished once the thread is unlocked"() {
        when:
        ref.unlock()
        then:
        ref.isFinished()
    }

    def "Reference should not be finished by default"() {
        expect:
        !ref.isFinished()
    }

    def "Reference's finished property should not be finished by default"() {
        expect:
        !ref.finishedProperty().get()
    }
}
