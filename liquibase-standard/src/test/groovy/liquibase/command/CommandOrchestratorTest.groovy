
package liquibase.command

import spock.lang.Specification

class CommandOrchestratorTest extends Specification {

    def "executes all steps in the pipeline"() {
        given:
        def step1 = Mock(CommandStep)
        def step2 = Mock(CommandStep)

        def definition = Stub(CommandDefinition) {
            getPipeline() >> [step1, step2]
        }

        def results = Mock(CommandResults)
        def builder = Stub(CommandResultsBuilder) {
            build() >> results
        }

        def orchestrator = new CommandOrchestrator()

        when:
        def result = orchestrator.orchestrate(null, definition, builder)

        then:
        1 * step1.run(builder)
        1 * step2.run(builder)
        result == results
    }

    def "stops executing steps when an exception occurs"() {
        given:
        def step1 = Mock(CommandStep)
        def step2 = Mock(CommandStep)

        def definition = Stub(CommandDefinition) {
            getPipeline() >> [step1, step2]
        }

        def builder = Mock(CommandResultsBuilder)
        def orchestrator = new CommandOrchestrator()

        when:
        orchestrator.orchestrate(null, definition, builder)

        then:
        1 * step1.run(builder) >> {
            throw new RuntimeException("Step failed")
        }
        0 * step2.run(_)

        thrown(RuntimeException)
    }

    def "executes cleanup steps in reverse order"() {
        given:
        def step1 = Mock(CommandStep, additionalInterfaces: [CleanUpCommandStep])
        def step2 = Mock(CommandStep, additionalInterfaces: [CleanUpCommandStep])

        def definition = Stub(CommandDefinition) {
            getPipeline() >> [step1, step2]
        }

        def builder = Stub(CommandResultsBuilder)
        def orchestrator = new CommandOrchestrator()

        when:
        orchestrator.orchestrate(null, definition, builder)

        then:
        1 * step1.run(builder)
        1 * step2.run(builder)

        then:
        1 * step2.cleanUp(builder)
        1 * step1.cleanUp(builder)
    }

    def "executes cleanup after a pipeline step fails"() {
        given:
        def step1 = Mock(CommandStep, additionalInterfaces: [CleanUpCommandStep])
        def step2 = Mock(CommandStep)

        def definition = Stub(CommandDefinition) {
            getPipeline() >> [step1, step2]
        }

        def builder = Stub(CommandResultsBuilder)
        def orchestrator = new CommandOrchestrator()

        when:
        orchestrator.orchestrate(null, definition, builder)

        then:
        1 * step1.run(builder)

        then:
        1 * step2.run(builder) >> {
            throw new RuntimeException("Pipeline failed")
        }

        then:
        1 * step1.cleanUp(builder)

        thrown(RuntimeException)
    }
}
