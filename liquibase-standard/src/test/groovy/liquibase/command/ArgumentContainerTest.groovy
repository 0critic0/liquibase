
package liquibase.command

import spock.lang.Specification

class ArgumentContainerTest extends Specification {

    def "stores an argument value"() {
        given:
        def container = new ArgumentContainer()

        when:
        container.addArgumentValue("username", "admin")

        then:
        container.getArgumentValues().get("username") == "admin"
    }

    def "stores an argument using CommandArgumentDefinition"() {
        given:
        def container = new ArgumentContainer()

        def argument = Stub(CommandArgumentDefinition) {
            getName() >> "username"
        }

        when:
        container.addArgumentValue(argument, "admin")

        then:
        container.getArgumentValues().get("username") == "admin"
    }

    def "overwrites an existing argument value"() {
        given:
        def container = new ArgumentContainer()

        when:
        container.addArgumentValue("username", "admin")
        container.addArgumentValue("username", "developer")

        then:
        container.getArgumentValues().get("username") == "developer"
        container.getArgumentValues().size() == 1
    }

    def "stores arguments in sorted order"() {
        given:
        def container = new ArgumentContainer()

        when:
        container.addArgumentValue("username", "admin")
        container.addArgumentValue("database", "test")
        container.addArgumentValue("password", "secret")

        then:
        container.getArgumentValues().keySet().toList() ==
                ["database", "password", "username"]
    }
}
