import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SubsetSumWithThreads {

    // Classe para armazenar o resultado de cada thread
    private static class Result {
        int count = 0;
    }

    // Classe que representa uma tarefa executada por uma thread
    private static class SubsetSumTask implements Runnable {
        private int[] S;
        private int x;
        private int startMask;
        private int endMask;
        private Result result;

        public SubsetSumTask(int[] S, int x, int startMask, int endMask, Result result) {
            this.S = S;
            this.x = x;
            this.startMask = startMask;
            this.endMask = endMask;
            this.result = result;
        }

        @Override
        public void run() {
            for (int mask = startMask; mask < endMask; mask++) {
                int sum = 0;
                for (int i = 0; i < S.length; i++) {
                    if ((mask & (1 << i)) != 0) { // Verifica se o i-ésimo elemento está no subconjunto
                        sum += S[i];
                    }
                }
                if (sum == x) {
                    result.count++;
                }
            }
        }
    }

    // Função para contar subconjuntos que somam x usando threads
    private static int countSubsets(int[] S, int x, int numThreads) {
        int n = S.length; // Tamanho do conjunto
        int totalSubsets = 1 << n; // Número total de subconjuntos (2^n)
        List<Result> results = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        // Divide o trabalho entre as threads
        int subsetsPerThread = totalSubsets / numThreads;
        for (int i = 0; i < numThreads; i++) {
            int startMask = i * subsetsPerThread;
            int endMask = (i == numThreads - 1) ? totalSubsets : startMask + subsetsPerThread;

            Result result = new Result();
            results.add(result);

            SubsetSumTask task = new SubsetSumTask(S, x, startMask, endMask, result);
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

    // Função para processar uma instância
    private static void processInstance(int[] S, int x, int numThreads) {
        long startTime = System.currentTimeMillis();
        int totalCount = countSubsets(S, x, numThreads);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Threads: " + numThreads);
        System.out.println("Subconjuntos encontrados: " + totalCount);
        System.out.println("Tempo de execução: " + duration + " ms");
        System.out.println("--------------------------");
    }

    // Função para ler o arquivo .zip e processar as instâncias
    private static void processZipFile(String zipFilePath, int x) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            zipFile.stream().forEach(entry -> {
                if (!entry.isDirectory()) {
                    processZipEntry(zipFile, entry, x);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Função para processar cada entrada no arquivo .zip
    private static void processZipEntry(ZipFile zipFile, ZipEntry entry, int x) {
        try (InputStream inputStream = zipFile.getInputStream(entry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            List<Integer> numbers = new ArrayList<>();

            // Lê o arquivo de instância
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        numbers.add(Integer.parseInt(part));
                    }
                }
            }

            // Converte a lista para um array
            int[] S = numbers.stream().mapToInt(i -> i).toArray();

            System.out.println("Processando instância: " + entry.getName());

            // Processa a instância com 1, 2, 3 e 4 threads
            for (int numThreads = 1; numThreads <= 4; numThreads++) {
                processInstance(S, x, numThreads);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String zipFilePath = "instancias.zip"; // Nome fixo do arquivo .zip
        int x = 100; // Valor de x fixo (pode ser ajustado conforme necessário)

        // Verifica se o arquivo .zip existe
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            System.out.println("Arquivo 'instancias.zip' não encontrado no diretório atual.");
            return;
        }

        System.out.println("Arquivo .zip encontrado: " + zipFilePath);
        processZipFile(zipFilePath, x);
    }
}