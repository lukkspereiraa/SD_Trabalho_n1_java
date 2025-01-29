import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SubsetSumParallel {

    // Classe para armazenar o resultado de cada thread
    private static class Result {
        int count = 0;
    }

    // Função recursiva para contar subconjuntos
    private static void countSubsets(int[] S, int n, int x, int index, int currentSum, Result result) {
        if (currentSum == x) {
            result.count++;
            return;
        }
        if (currentSum > x || index >= n) {
            return;
        }
        // Inclui o elemento atual e verifica
        countSubsets(S, n, x, index + 1, currentSum + S[index], result);
        // Exclui o elemento atual e verifica
        countSubsets(S, n, x, index + 1, currentSum, result);
    }

    // Classe que representa uma thread
    private static class SubsetSumTask implements Runnable {
        private int[] S;
        private int n;
        private int x;
        private int startIndex;
        private Result result;

        public SubsetSumTask(int[] S, int n, int x, int startIndex, Result result) {
            this.S = S;
            this.n = n;
            this.x = x;
            this.startIndex = startIndex;
            this.result = result;
        }

        @Override
        public void run() {
            countSubsets(S, n, x, startIndex, 0, result);
        }
    }

    // Função para processar uma instância
    private static int processInstance(int[] S, int x, int numThreads) {
        int n = S.length;
        List<Result> results = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            results.add(new Result());
        }

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            int startIndex = i; // Cada thread começa de um índice diferente
            SubsetSumTask task = new SubsetSumTask(S, n, x, startIndex, results.get(i));
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        // Aguarda todas as threads terminarem
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Soma os resultados de todas as threads
        int totalCount = 0;
        for (Result result : results) {
            totalCount += result.count;
        }

        return totalCount;
    }

    // Função para ler o arquivo .zip e processar as instâncias
    private static void processZipFile(String zipFilePath) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            zipFile.stream().forEach(entry -> {
                if (!entry.isDirectory()) {
                    processZipEntry(zipFile, entry);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Função para processar cada entrada no arquivo .zip
    private static void processZipEntry(ZipFile zipFile, ZipEntry entry) {
        try (InputStream inputStream = zipFile.getInputStream(entry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            List<Integer> numbers = new ArrayList<>();
            int x = 0;

            // Lê o arquivo de instância
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("x:")) {
                    x = Integer.parseInt(line.split(":")[1].trim());
                } else {
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (!part.isEmpty()) {
                            numbers.add(Integer.parseInt(part));
                        }
                    }
                }
            }

            // Converte a lista para um array
            int[] S = numbers.stream().mapToInt(i -> i).toArray();

            // Processa a instância com 1, 2, 3 e 4 threads
            for (int numThreads = 1; numThreads <= 4; numThreads++) {
                long startTime = System.currentTimeMillis();
                int totalCount = processInstance(S, x, numThreads);
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                System.out.println("Instância: " + entry.getName());
                System.out.println("Threads: " + numThreads);
                System.out.println("Subconjuntos encontrados: " + totalCount);
                System.out.println("Tempo de execução: " + duration + " ms");
                System.out.println("--------------------------");

                // Interrompe se o tempo exceder 3600 segundos
                if (duration > 3600 * 1000) {
                    System.out.println("Tempo excedido para " + numThreads + " threads.");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String zipFilePath = "instancias.zip"; // Nome fixo do arquivo .zip

        // Verifica se o arquivo .zip existe
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            System.out.println("Arquivo 'instancias.zip' não encontrado no diretório atual.");
            return;
        }

        System.out.println("Arquivo .zip encontrado: " + zipFilePath);
        processZipFile(zipFilePath);
    }
}