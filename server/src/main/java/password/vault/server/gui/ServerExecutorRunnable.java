package password.vault.server.gui;

import password.vault.server.Server;

public class ServerExecutorRunnable implements Runnable {

    private final Server server;

    public ServerExecutorRunnable(Server server) {
        this.server = server;
    }


    @Override
    public void run() {
        server.start();
    }
}
