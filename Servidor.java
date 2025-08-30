import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Servidor {
    private ServerSocket servidor = new ServerSocket(9999);
    private LocalTime horaServidor = LocalTime.of(10, 30);
    private List<Socket> clientes = new ArrayList<>();

    public Servidor() throws IOException {
    }

    public int getNumeroClientes() {
        return clientes.size();
    }

    public void aceitarClientes() throws IOException {
        Socket cliente = servidor.accept();
        clientes.add(cliente);
        System.out.println("[Servidor] Cliente conectado: " + cliente.getInetAddress().getHostAddress());
    }

    public List<Socket> sincronizarRelogio() throws IOException {
        List<ClienteDiff> diferencas = capturarDiferencaEntreServidorEClientes();

        List<Long> diferencasRelogio = diferencas.stream()
            .map(ClienteDiff::getDiferenca)
            .collect(Collectors.toList());

        long media = calcularMediaDosRelogios(diferencasRelogio);
        ajustarHoraServidor(media);

        System.out.println("[Servidor] Media: " + media);
        enviarAjustesParaOsClientes(diferencas, media);

        System.out.println("[Servidor] The end!");
        return clientes;
    }

    private void ajustarHoraServidor(long media) {
        System.out.println("[Servidor] Servidor ajustando hora com a média: " + media);
        horaServidor = media >= 0 ? horaServidor.plusSeconds(media) : horaServidor.minusSeconds(-media);
        System.out.println("[Servidor] Hora do servidor ajustada: " + horaServidor);
    }

    private void enviarAjustesParaOsClientes(List<ClienteDiff> diferencas, long media) throws IOException {
        System.out.println("[Servidor] Enviando ajustes para os clientes...");
        for (ClienteDiff clienteDiff : diferencas) {
            Socket cliente = clienteDiff.getSocket();
            long diff = clienteDiff.getDiferenca();

            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
            long ajuste = Math.negateExact(diff - media);
            out.println(ajuste);
        }
    }

    private long calcularMediaDosRelogios(List<Long> diferencas) {
        System.out.println("[Servidor] Calculando a média...");
        long soma = diferencas.stream().reduce(0L, Long::sum);
        return soma / (diferencas.size() + 1); // Inclui o servidor
    }

    private List<ClienteDiff> capturarDiferencaEntreServidorEClientes() {
        List<ClienteDiff> diferencas = new ArrayList<>();
        for (Socket cliente : clientes) {
            try {
                PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

                out.println(horaServidor);
                long diferenca = Long.parseLong(in.readLine());

                System.out.println("[Servidor] Recebendo diferença do cliente: " + diferenca);
                diferencas.add(new ClienteDiff(cliente, diferenca));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return diferencas;
    }

    public LocalTime getHoraServidor() {
        return horaServidor;
    }

    public ServerSocket getServerSocket() {
        return servidor;
    }
}
