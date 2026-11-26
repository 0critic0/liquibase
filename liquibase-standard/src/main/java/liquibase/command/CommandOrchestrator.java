package liquibase.command;

import liquibase.Scope;
import liquibase.logging.mdc.MdcKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandOrchestrator {

    public CommandResults orchestrate(CommandDefinition definition, CommandResultsBuilder resultsBuilder) throws Exception {
        final List<CommandStep> pipeline = definition.getPipeline();
        final List<CommandStep> executedCommands = new ArrayList<>();
        Optional<Exception> thrownException = Optional.empty();

        // Forward Loop
        for (CommandStep command : pipeline) {
            try {
                Scope.getCurrentScope()
                        .addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, getCommandStepName(command));
                command.run(resultsBuilder);
            } catch (Exception runException) {
                thrownException = Optional.of(runException);
                break;
            }
            executedCommands.add(command);
        }

        // Reverse Cleanup Loop
        for (int i = executedCommands.size() - 1; i >= 0; i--) {
            CommandStep command = pipeline.get(i);
            if (command instanceof CleanUpCommandStep cleanUpStep) {
                cleanUpStep.cleanUp(resultsBuilder);
            }
        }

        // Throw error
        if (thrownException.isPresent()) {
            throw thrownException.get();
        }

        return resultsBuilder.build();
    }

    private String getCommandStepName(CommandStep commandStep) {
        StringBuilder commandStepName = new StringBuilder();
        String[][] commandDefinition = commandStep.defineCommandNames();
        if (commandDefinition != null) {
            for (String[] commandNames : commandDefinition) {
                if (!commandStepName.isEmpty()) {
                    commandStepName.append(" ");
                }
                commandStepName.append(String.join(" ", commandNames));
            }
        }
        return commandStepName.toString();
    }
}