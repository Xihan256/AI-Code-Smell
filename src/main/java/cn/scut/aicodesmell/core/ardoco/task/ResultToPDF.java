package cn.scut.aicodesmell.core.ardoco.task;

import cn.scut.aicodesmell.exception.CoreTaskException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;

/**
 * @author wanghy
 */
public class ResultToPDF implements ITaskHandler {

    private String downloadPath = System.getProperty("user.dir") + File.separator + "data/files/download/";

    @Override
    public void handle(TaskContext context) {
        File ardocoResult = context.getArdocoResult();

        String path = downloadPath + context.getProjectId() + ".pdf";
        File finalResult = new File(path);

        //输出pdf
        Document document = new Document();
        try(FileInputStream inputStream  = new FileInputStream(ardocoResult)) {
            finalResult.createNewFile();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                document.add(new Paragraph(line));
            }

        } catch (DocumentException | IOException e) {
            throw new CoreTaskException(context.getProjectId());
        }finally {
            document.close();
        }

        context.setFinalResult(finalResult);
    }
}
