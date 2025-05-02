package cn.scut.aicodesmell.core.ardoco.task;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author wanghy
 */
public class CodeToPCM implements ITaskHandler {

    private String uploadPath = System.getProperty("user.dir") + "/" + "data/files/upload/";

    /*
     * 固定的指令
     */
    private static String cmd0 = "java";
    private static String cmd1 = "-jar";
    private static String cmd2 = "F:/javaPlace/AICodeSmell/ArchitectureRecovery/arcade_java11_forSYS.jar";
    private static String cmd3 = "ACDC";
    /**
     * 测试根目录
     */
    private static String ROOT_DIR = "F:/javaPlace/AICodeSmell/data/testSYS";
    /**
     * 测试输出根目录
     */
    private String OUTPUT_DIR = "F:/javaPlace/AICodeSmell/data/testSYS/output/";
    /*
     * 固定的指令
     */
    private static String cmd6 = "abc";

    @Override
    public void handle(TaskContext context) {
        //code解压
        File code = context.getCode();
        String fileExtension = code.getName().substring(code.getName().lastIndexOf("."));
        File rootDir = new File(ROOT_DIR);
        switch (fileExtension) {
            case ".zip": {
                try (ZipFile zipFile = new ZipFile(code)) {
                    Enumeration enumeration = zipFile.entries();
                    while (enumeration.hasMoreElements()) {
                        //依次获取压缩包内的文件实体对象
                        ZipEntry entry = (ZipEntry) enumeration.nextElement();
                        String name = entry.getName();
                        if (entry.isDirectory()) {
                            continue;
                        }
                        try (BufferedInputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry))) {
                            // 需要判断文件所在的目录是否存在，处理压缩包里面有文件夹的情况
                            String outName = rootDir + "/" + name;
                            File outFile = new File(outName);
                            File tempFile = new File(outName.substring(0, outName.lastIndexOf("/")));
                            if (!tempFile.exists()) {
                                tempFile.mkdirs();
                            }
                            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
                                int len;
                                byte[] buffer = new byte[1024];
                                while ((len = inputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, len);
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("解压zip失败", e);
                }
                break;
            }
            case ".rar": {
                try (Archive archive = new Archive(code)) {

                    FileHeader fileHeader;
                    while ((fileHeader = archive.nextFileHeader()) != null) {
                        if (fileHeader.isDirectory()) {
                            continue;
                        }

                        String fileName = fileHeader.getFileNameString().trim().replaceAll("\\\\", "/");
                        File outputFile = new File(rootDir, fileName);
                        File parent = outputFile.getParentFile();
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        ;

                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            archive.extractFile(fileHeader, fos);
                        }

                        System.out.println("已解压：" + outputFile.getAbsolutePath());
                    }

                } catch (IOException | RarException e) {
                    throw new RuntimeException("rar解压失败", e);
                }
                break;
            }
            default:
                throw new RuntimeException("不合法的压缩类型");

        }

        String outDir = OUTPUT_DIR + context.getProjectId();
        String mainPackage = context.getMainPackage();
        String[] cmdArgs = new String[]{cmd0, cmd1, cmd2, cmd3, ROOT_DIR, outDir, cmd6, mainPackage};
        try {
            //运行
            Process process = Runtime.getRuntime().exec(cmdArgs);
            // 输出arcade的日志
            new Thread(() -> {
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream(), "GBK"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            process.waitFor();

            File acdc = new File(outDir + "/acdc/acdc_clustered.rsf");
            Set<String> pkgs = processAcdc(acdc, context);
            if (CollectionUtils.isEmpty(pkgs)) {
                throw new RuntimeException("acdc数据为空");
            }
            writeXml(pkgs, uploadPath, context.getProjectId());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        File pcm = new File(uploadPath + context.getProjectId() + ".repository");
        context.setPcm(pcm);
    }

    private Set<String> processAcdc(File acdc, TaskContext context) {
        Set<String> pkgSet = ConcurrentHashMap.newKeySet();
        //并行处理acdc数据
        try (Stream<String> lines = Files.lines(acdc.toPath())) {
            lines.parallel().forEach(line -> {
                String[] components = line.split(" ");
                if (components.length == 3) {
                    //把.ss 去掉, 那玩意没有用
                    String pkg = components[1].substring(0, components[1].lastIndexOf('.'));
                    String absolutePkg = pkg.substring(pkg.lastIndexOf('.') + 1);
                    if (!pkgSet.contains(absolutePkg)) {
                        pkgSet.add(absolutePkg);
                        context.getCodeComponent2CodePackageMap().put(absolutePkg, pkg);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pkgSet;
    }

    private void writeXml(Set<String> pkgs, String targetPath, String fileName) {
        String fullFilePath = targetPath + "/" + fileName + ".repository";
        Path fullFilePathP = Path.of(fullFilePath);
        try {
            File outputFile = null;
            //把旧的删了
            Files.deleteIfExists(fullFilePathP);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            String XMI_NS = "http://www.omg.org/XMI";
            String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
            String REPO_NS = "http://palladiosimulator.org/PalladioComponentModel/Repository/5.2";

            Element root = doc.createElementNS(REPO_NS, "repository:Repository");
            root.setAttributeNS(XMI_NS, "xmi:version", "2.0");
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xmi", XMI_NS);
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", XSI_NS);
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:repository", REPO_NS);
            root.setAttribute("id", java.util.UUID.randomUUID().toString().substring(0, 22));
            root.setAttribute("entityName", fileName);
            doc.appendChild(root);

            for (String pkg : pkgs) {
                String id = java.util.UUID.randomUUID().toString().substring(0, 22);
                addComponent(doc, root, id, pkg, REPO_NS);
            }

            // 写入文件
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // 输出到文件
            outputFile = new File(fullFilePath);
            transformer.transform(new DOMSource(doc), new StreamResult(outputFile));
            System.out.println("XML 文件已写入: " + outputFile.getAbsolutePath());
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private void addComponent(Document doc, Element root, String id, String name, String REPO_NS) {
        Element comp = doc.createElement("components__Repository");
        comp.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "repository:BasicComponent");
        comp.setAttribute("id", id);
        comp.setAttribute("entityName", name);
        root.appendChild(comp);
    }
}
