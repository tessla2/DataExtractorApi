package io.github.tessla2.dataextractorapi.service;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class WebScraping{

    private final ExtractorService extractorService;


    public WebScraping(ExtractorService extractorService) {
        this.extractorService = extractorService;
    }
    private static final String DOWNLOAD_DIR = "/Anexos";
    private static final String ZIP_FILE_PATH = "Anexos/anexo.zip";


    public void linksSearch() {
        List<String> pdfList = new ArrayList<>();
        Set<String> processedLinks = new HashSet<>();
        try {
            Document document = Jsoup.connect("https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos").get();

            Elements linksPDF = document.select("a[href$=.pdf]");

            for (Element link : linksPDF) {
                String pdfUrl = link.attr("abs:href");

                if (pdfUrl.contains("Anexo") && processedLinks.add(pdfUrl)) {
                    System.out.println("Pdf Encontrado: " + pdfUrl);
                    pdfList.add(pdfUrl);
                    downloadPdf(pdfUrl, DOWNLOAD_DIR);
                }
            }
            zipFiles();
           extractorService.zipPdfToCsv();
        } catch (IOException e) {
            System.err.println("Erro ao conectar e buscar PDFs: " + e.getMessage());
        }
    }

    public void downloadPdf(String pdfUrl, String downloadDir) {
        try {
            String fileName = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(downloadDir, fileName);

            if (!Files.exists(Paths.get(downloadDir))) {
                Files.createDirectories(Paths.get(downloadDir));
            }
            if (!Files.isWritable(Paths.get(downloadDir))) {
                System.err.println("Acesso negado: não é possível escrever no diretório " + downloadDir);
                return;
            }
            if (!Files.exists(filePath)) {
                FileUtils.copyURLToFile(new URL(pdfUrl), filePath.toFile());
            } else {
                System.out.println("O arquivo já existe: " + filePath.toAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao salvar PDF: " + e.getMessage());
        }
    }

    public void zipFiles() {
        Path zipPath = Paths.get(ZIP_FILE_PATH);
        Path zipDirPath = zipPath.getParent();

        try {
            //Cria o diretório pai do ZIP se não existir
            if (!Files.exists(zipDirPath)) {
                Files.createDirectories(zipDirPath);
            }

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                Path dirPath = Paths.get(DOWNLOAD_DIR);

                Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // Evita adicionar o próprio ZIP no pacote
                        if (!file.toString().endsWith(".zip")) {
                            String fileName = file.getFileName().toString();
                            zos.putNextEntry(new ZipEntry(fileName));
                            Files.copy(file, zos);
                            zos.closeEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                System.out.println("Arquivos compactados com sucesso em: " + zipPath);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro ao compactar arquivos: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao criar diretório para o ZIP: " + e.getMessage());
        }
        deleteDownloadedPdfs();
    }

    private void deleteDownloadedPdfs() {
        try {
            Files.walk(Paths.get(DOWNLOAD_DIR))
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().endsWith(".zip")) // Mantém apenas o ZIP
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Erro ao deletar arquivo: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}