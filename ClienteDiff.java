import java.net.Socket;

public class ClienteDiff {
    private Socket socket;
    private long diferenca;

    public ClienteDiff(Socket socket, long diferenca) {
        this.socket = socket;
        this.diferenca = diferenca;
    }

    public Socket getSocket() {
        return socket;
    }

    public long getDiferenca() {
        return diferenca;
    }
}