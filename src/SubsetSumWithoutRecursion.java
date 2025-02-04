import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SubsetSumWithoutRecursion {

    // Função para contar subconjuntos que somam x usando máscaras de bits
    private static int countSubsets(int[] S, int x) {
        int n = S.length; // Tamanho do conjunto
        int totalSubsets = 1 << n; // Número total de subconjuntos (2^n)
        int count = 0; // Contador de subconjuntos válidos

        // Itera sobre todas as combinações possíveis
        for (int mask = 0; mask < totalSubsets; mask++) {
            int sum = 0;

            // Calcula a soma do subconjunto atual
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) { // Verifica se o i-ésimo elemento está no subconjunto
                    sum += S[i];
                }
            }

            // Verifica se a soma é igual a x
            if (sum == x) {
                count++;
            }
        }

        return count;
    }

    // Função para processar uma instância
    private static void processInstance(int[] S, int x) {
        long startTime = System.currentTimeMillis();
        int totalCount = countSubsets(S, x);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

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
            processInstance(S, x);

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