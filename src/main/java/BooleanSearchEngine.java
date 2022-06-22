import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> index = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        indexPdfs(pdfsDir);
    }

    private void indexPdfs(File pdfsDir) throws IOException {
        for (File pdf :
                Objects.requireNonNull(pdfsDir.listFiles())) {
            String pdfName = pdf.getName();
            PdfDocument doc = new PdfDocument(new PdfReader(pdf));
            for (int i = 1; i < doc.getNumberOfPages(); i++) {
                PdfPage page = doc.getPage(i);
                String text = PdfTextExtractor.getTextFromPage(page);
                String[] words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> counts = new HashMap<>();
                for (String word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    counts.put(word.toLowerCase(), counts.getOrDefault(word.toLowerCase(), 0) + 1);
                }
                for (Map.Entry<String, Integer> pair : counts.entrySet()) {
                    PageEntry pageEntry = new PageEntry(pdfName, i, pair.getValue());
                    List<PageEntry> value = index.getOrDefault(pair.getKey(), new ArrayList<>());
                    value.add(pageEntry);
                    index.put(pair.getKey(), value);
                }
            }
        }
        for (List<PageEntry> list:
        index.values()){
                Collections.sort(list);
                Collections.reverse(list);
        }
    }

    public String listToJson(List<PageEntry> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<PageEntry>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    @Override
    public List<PageEntry> search(String word) {
        String lowerCaseWord = word.toLowerCase();
        return index.get(lowerCaseWord);
    }
}
