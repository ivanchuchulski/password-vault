package bg.sofia.uni.fmi.mjt.password.vault.server.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserCommandCreatorTest {
    @Test
    public void testCommandCreationWithNoArguments() {
        String commandText = "test";
        UserCommand cmd = UserCommandCreator.createCommand(commandText);

        assertEquals("unexpected command returned for command 'test'", commandText, cmd.command());
        assertNotNull("command arguments should not be null", cmd.arguments());
        assertEquals("unexpected command arguments count", 0, cmd.arguments().length);
    }

    @Test
    public void testCommandCreationWithOneArgument() {
        String command = "test abcd";
        UserCommand cmd = UserCommandCreator.createCommand(command);

        int expectedNumberOfArguments = 1;
        String expectedArguments = command.split(" ")[1];

        assertEquals("unexpected command returned for command", command.split(" ")[0], cmd.command());
        assertNotNull("command arguments should not be null", cmd.arguments());
        assertEquals("unexpected command arguments count", expectedNumberOfArguments, cmd.numberOfArguments());
        assertEquals("unexpected argument returned for command", expectedArguments, cmd.arguments()[0]);
    }

    @Test
    public void testCommandCreationWithOneArgumentInQuotes() {
        String command = "test abcd 1234";
        UserCommand cmd = UserCommandCreator.createCommand(command);

        int expectedNumberOfArguments = 2;
        String expectedArguments = command.split(" ")[1];

        assertEquals("unexpected command returned for command '", command.split(" ")[0], cmd.command());
        assertNotNull("command arguments should not be null", cmd.arguments());
        assertEquals("unexpected command arguments count", expectedNumberOfArguments, cmd.numberOfArguments());
        assertEquals("multi-word argument is not respected", expectedArguments, cmd.arguments()[0]);
    }

}