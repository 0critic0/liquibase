package liquibase.command;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Extracted from CommandScope to manage the storage and retrieval of command arguments.
 * This separates the state management of arguments from the execution facade.
 */
public class ArgumentContainer {

    private final SortedMap<String, Object> argumentValues = new TreeMap<>();

    public void addArgumentValue(String argument, Object value) {
        this.argumentValues.put(argument, value);
    }

    public <T> void addArgumentValue(CommandArgumentDefinition<T> argument, T value) {
        this.argumentValues.put(argument.getName(), value);
    }

    public SortedMap<String, Object> getArgumentValues() {
        return this.argumentValues;
    }
}