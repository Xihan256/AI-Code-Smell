package cn.scut.aicodesmell.core.ardoco.task;

import cn.scut.aicodesmell.exception.CoreTaskException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author wanghy
 */
public class DocToText implements ITaskHandler {

    private String uploadPath = System.getProperty("user.dir") + "/" + "data/files/upload/";

    @Override
    public void handle(TaskContext context) {
        File doc = context.getDoc();
        String extension = doc.getName().substring(doc.getName().lastIndexOf('.'));

        //todo 其实这里还没完成. 可能会有翻译成英文或者提取doc核心内容的工作
        String text;
        if(".doc".equals(extension)){
            try (FileInputStream fis = new FileInputStream(doc)) {
                HWPFDocument document = new HWPFDocument(fis);
                WordExtractor extractor = new WordExtractor(document);
                text = extractor.getText();
            } catch (IOException e) {
                e.printStackTrace();
                throw new CoreTaskException(context.getProjectId());
            }
        }else if(".docx".equals(extension)){
            try (FileInputStream fis = new FileInputStream(doc)){
                 XWPFDocument docx = new XWPFDocument(fis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
                text = extractor.getText();
            } catch (IOException e) {
                e.printStackTrace();
                throw new CoreTaskException(context.getProjectId());
            }
        }else{
            throw new CoreTaskException(context.getProjectId());
        }

        //输出到txt
        Path outTxtPath = Path.of(uploadPath + context.getProjectId() + ".txt");
        File txt = outTxtPath.toFile();
        try (BufferedWriter writer = Files.newBufferedWriter(outTxtPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CoreTaskException(context.getProjectId());
        }

        context.setTxt(txt);
    }
}
