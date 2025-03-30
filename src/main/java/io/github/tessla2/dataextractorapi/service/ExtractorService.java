package io.github.tessla2.dataextractorapi.service;

import com.opencsv.CSVWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.RectangularTextContainer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ExtractorService {

    private final String zipFilePath = "C:\\Users\\HP_1\\Desktop\\APIS\\extratordedados\\Anexos\\anexo.zip";
    private final String pdfName = "Anexo_I_Rol_2021RN_465.2021_RN627L.2024.pdf";
    private final String csvPath = "C:\\Users\\HP_1\\Desktop\\APIS\\extratordedados\\Anexos\\output.csv";

    public void zipPdfToCsv() {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            ZipEntry pdfEntry = zipFile.getEntry(pdfName);

            if (pdfEntry == null) {
                System.out.println("PDF não encontrado dentro do ZIP");
                return;
            }

            System.out.println("Arquivo PDF encontrado!");

            //arquivo temporário para armazenar o PDF extraído
            File tempPdfFile = File.createTempFile("tempPdf", ".pdf");
            tempPdfFile.deleteOnExit();

            try (InputStream inputStream = zipFile.getInputStream(pdfEntry);
                 FileOutputStream outputStream = new FileOutputStream(tempPdfFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            List<String[]> tableData = extractTableFromPdf(tempPdfFile);
            saveTextAsCsv(tableData, csvPath);
            System.out.println("Texto extraído e salvo como CSV");

        } catch (IOException e) {
            System.err.println("Erro ao processar o arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<String[]> extractTableFromPdf(File pdfFile) throws IOException {
        List<String[]> extractedData = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

            for (int i = 1; i <= document.getNumberOfPages(); i++) {
                Page page = extractor.extract(i);
                List<Table> tables = sea.extract(page);

                for (Table table : tables) {
                    for (List<RectangularTextContainer> row : table.getRows()) {
                        extractedData.add(row.stream().map(RectangularTextContainer::getText).toArray(String[]::new));
                    }
                }
            }
        }
        return extractedData;
    }

    public static void saveTextAsCsv(List<String[]> tableData, String csvPath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath))) {
            for (String[] row : tableData) {
                writer.writeNext(row);
            }
        }
    }
}
