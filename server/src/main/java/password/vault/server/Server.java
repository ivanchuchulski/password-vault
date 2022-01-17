package password.vault.server;

import password.vault.api.ServerCommand;
import password.vault.server.communication.UserCommand;
import password.vault.server.communication.UserCommandCreator;
import password.vault.server.exceptions.SocketChannelReadException;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;
import password.vault.server.password.vault.PasswordVault;
import password.vault.server.communication.CommandResponse;
import password.vault.server.user.repository.UserRepository;
import password.vault.server.user.repository.in.memory.UserRepositoryInMemory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private static final int BUFFER_SIZE = 4096;
    private static final String SERVER_HOST = "localhost";

    private final Selector selector;
    private final ByteBuffer messageBuffer;
    private final ServerSocketChannel serverSocketChannel;
    private boolean runServer;

    private final CommandExecutor commandExecutor;

    public Server(int port, Path registeredUsersFile, Path credentialsFile,
                  PasswordSafetyChecker passwordSafetyChecker, PasswordGenerator passwordGenerator) {
        try {
            messageBuffer = ByteBuffer.allocate(BUFFER_SIZE);

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, port));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            runServer = true;

            UserRepository userRepository = new UserRepositoryInMemory(registeredUsersFile);
            PasswordVault passwordVault = new PasswordVault(credentialsFile);
            commandExecutor = new CommandExecutor(userRepository, passwordVault, passwordSafetyChecker,
                                                  passwordGenerator);
        } catch (IOException ioException) {
            throw new RuntimeException("error : creating server", ioException);
        }
    }

    public void start() {
        System.out.println("started server...");
        while (runServer) {
            try {
                int readyChannels = selector.select();
                if (readyChannels <= 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        acceptConnection(key);
                    } else if (key.isReadable()) {
                        executeClientRequest(key);
                    }
                }
            } catch (IOException ioException) {
                System.out.println("caught exception during ");
                ioException.printStackTrace();
            }
        }

        try {
            freeResources();
        } catch (IOException ioException) {
            throw new RuntimeException("error : closing ServerSocketChannel and Selector", ioException);
        }
    }

    public void stop() {
        runServer = false;
    }

    // public static void main(String[] args) {
    //     final int serverPort = 7777;
    //     final Path usersFilePath = Path.of("resources" + File.separator + "users.txt");
    //     final Path credentialsFile = Path.of("resources" + File.separator + "credentials.txt");
    //
    //     final HttpClient httpClientForPasswordSafetyChecker = HttpClient.newBuilder().build();
    //     final HttpClient httpClientForPasswordGenerator = HttpClient.newBuilder().build();
    //     final PasswordSafetyChecker passwordSafetyChecker =
    //             new PasswordSafetyChecker(httpClientForPasswordSafetyChecker);
    //     final PasswordGenerator passwordGenerator = new PasswordGenerator(httpClientForPasswordGenerator);
    //
    //     Server server = new Server(serverPort, usersFilePath, credentialsFile, passwordSafetyChecker,
    //                                passwordGenerator);
    //     server.start();
    // }

    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel clientSocketChannel = serverSocketChannel.accept();

        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void executeClientRequest(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        String request = readClientRequest(socketChannel);

        if (request == null) {
            return;
        }

        UserCommand clientUserCommand = UserCommandCreator.createCommand(request);

        CommandResponse commandResponse = commandExecutor.executeCommand(socketChannel, clientUserCommand);

        writeResponseToClient(socketChannel, commandResponse.response());

        if (commandResponse.toDisconnect()) {
            socketChannel.close();
        }
    }

    private void freeResources() throws IOException {
        serverSocketChannel.close();
        selector.close();
    }

    private String readClientRequest(SocketChannel socketChannel) throws IOException {
        try {
            readFromChannelIntoBuffer(socketChannel);

            String messageFromBuffer = readClientRequestFromBuffer();

            // removing the line separator at the end
            return messageFromBuffer.replace(System.lineSeparator(), "");
        } catch (SocketChannelReadException socketChannelReadException) {

            commandExecutor.executeCommand(socketChannel,
                                           new UserCommand(ServerCommand.LOGOUT.getCommandText(), new String[]{}));

            socketChannel.close();

            return null;
        }
    }

    private void readFromChannelIntoBuffer(SocketChannel socketChannel) throws SocketChannelReadException {
        try {
            messageBuffer.clear(); // switch to writing mode
            int bytesReadFromSocketChannel = socketChannel.read(messageBuffer);

            if (bytesReadFromSocketChannel <= 0) {
                System.out.println("nothing to read, will close channel");
                throw new SocketChannelReadException("nothing read from channel");
            }

        } catch (IOException ioException) {
            // this exception can happen if for example you close the terminal emulator for a client
            System.out.println("i/o exception while reading from user SocketChannel");
            throw new SocketChannelReadException("i/o exception while reading from user SocketChannel", ioException);
        }
    }

    private String readClientRequestFromBuffer() {
        messageBuffer.flip(); // switch to reading mode

        return StandardCharsets.UTF_8.decode(messageBuffer).toString();
    }

    private void writeResponseToClient(SocketChannel socketChannel, String response) throws IOException {
        try {
            writeResponseToBuffer(response);
            writeResponseToSocketChannel(socketChannel);
        } catch (IOException ioException) {
            commandExecutor.executeCommand(socketChannel,
                                           new UserCommand(ServerCommand.LOGOUT.getCommandText(), new String[]{}));

            socketChannel.close();
        }
    }

    private void writeResponseToBuffer(String response) {
        messageBuffer.clear(); // switch to writing mode
        byte[] responseAsBytes = (response + System.lineSeparator()).getBytes();
        messageBuffer.put(responseAsBytes);
    }

    private void writeResponseToSocketChannel(SocketChannel socketChannel) throws IOException {
        messageBuffer.flip(); // switch to reading mode
        socketChannel.write(messageBuffer);
    }
}
