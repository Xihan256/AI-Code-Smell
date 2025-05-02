# ARCADE

该包会分析指定目录下的项目，将指定目录下的每个文件夹视为一个项目，通过分析项目内的jar包生成类依赖关系（deps.rsf），再通过类依赖关系和源码（.java）分析出架构视图（clustered.rsf）

目录说明：

```
.
└── ArchitectureRecovery
    ├── arcade_java11.jar # 编译后的jar包，适用于java11
    ├── arcadepy   # 跑pkg的py代码
    ├── cfg        # 跑ARC时需要的文件
    ├── mallet-2.0.7 # 跑ARC时需要外部库
    ├── res        # 跑ARC时需要的文件夹
    ├── stoplists  # 跑ARC时需要的文件夹 
    ├── python2.7  # 跑pkg需要的python
    ├── testSYS    # 用于测试该工具的系统源代码
```

## arcaade_java11_forSYS.jar

- 取消jar包路径的指定
- 修复了当使用绝对路径调用jar包，ARC找不到依赖的目录的问题
- 如果ARC_Clustering运行成功，会删除base文件夹及其内容
- 架构复现结果的名字由`版本名字_复现算法_clustered.rsf`变为`复现算法_clustered.rsf`，因此如果一次复现多个版本，结果会发生覆盖
- 生成的依赖文件也由`版本名字_deps.rsf`变为`deps.rsf`
- 分析的项目名字最好不要名为“output”
- 修改了空项目目录也会生成deps.rsf的bug

输入参数

| 参数    | 例子                       | 备注                                                         | 约束 |
| ------- | -------------------------- | ------------------------------------------------------------ | ---- |
| args[0] | ACDC                       | ACDC \| ARC_Pipe \| ARC_Clustering                           | 非空 |
| args[1] | subject_systems/Ant/src    | 该目录存放项目各个版本的源码和编译后的jar包                  | 非空 |
| args[2] | subject_systems/Ant/output | 输出的路径，输出格式见代码重构                               | 非空 |
| args[3] | lib_ant                    | 已废弃，随便填一个字段，如abc                                | 可空 |
| args[4] | org.apache.tools.ant       | 要分析的主包，用于过滤一些不相关的依赖关系，ARC_Pipe可空     | 可空 |
| args[5] | 20                         | 可空，指定ACDC的线程池中线程个数，最终取min(3/4可用核数, 任务数, 用户指定数量 ) | 可空 |

### 单独运行测试命令（全路径版本）

```shell
# ACDC
java -jar D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\arcade_java11_forSYS.jar ACDC D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID\output abc org.apache.tools.ant 1

# ARC
java -jar D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\arcade_java11_forSYS.jar ARC_Pipe D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID\apache-ant-1.10.0 D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID\output abc org.apache.tools.ant 1

# windows 配置mellet环境变量，linux不用
setx MALLET_HOME "D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\mallet-2.0.7"

D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\mallet-2.0.7\bin\mallet import-dir --input D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID --remove-stopwords TRUE --keep-sequence TRUE --output D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID/output/arc/base/topicmodel.data 

D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\mallet-2.0.7\bin\mallet train-topics --input D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID/output/arc/base/topicmodel.data  --inferencer-filename D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID/output/arc/base/infer.mallet --num-top-words 50 --num-topics 100 --num-threads 3 --num-iterations 100 --doc-topics-threshold 0.1

java -jar D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\arcade_java11_forSYS.jar ARC_Clustering D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID\output abc org.apache.tools.ant 1

# PKG 需要python2环境
D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\python2.7\python D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\arcadepy\src\arc\batchpackager.py --startdir D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID/output/deps --outputfile D:\12138\Desktop\架构复现系统\architecture_recovery_sys\src\tools\ArchitectureRecovery\testSYS\projectID/output/pkg --pkgprefixes org.apache.tools.ant
```



## 注意

在本地调用mallet时可能会出现JRE分配内存不够的问题，可以修改`mallet.bat`中设置JRE内存的参数

```
set MALLET_MEMORY=5G
```

如果想在linux系统上运行，需要把`mallet.bat`换成`mallet`，如果linux也出现内存不足的问题，则适当增加下列配置：

```
MEMORY=5g
```



## Spring Boot整合说明

### 配置工具的路径：`application.yml`

```yaml
archiViewAlg:
  jarDir: D:\Workspace\srp\git仓库\code_analyse_sys\后端\CodeAnalyezeSystem\code-reverse-engineering\src\tools\ArchitectureRecovery
```

### 实体类（用于保存架构图的节点和边对象）：

#### AVD_Node

```java
import lombok.AllArgsConstructor;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
  
import java.util.Objects;  

/**  
 * 架构图的节点
 */
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class AVD_Edge {  
	private Integer key;  
	private String text;  
	private boolean isGroup;  
	private String category = "";  
	private Integer group;
	
    }  
}
```

#### AVD_Edge

```java
import lombok.AllArgsConstructor;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
  
import java.util.Objects;  

/**  
 * 架构图的边  
 */
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class AVD_Edge {  
    private Integer from;  
    private Integer to;  
  
    @Override  
    public boolean equals(Object o) {  
        if (this == o) return true;  
        if (o == null || getClass() != o.getClass()) return false;  
        AVD_Edge avd_edge = (AVD_Edge) o;  
        return Objects.equals(from, avd_edge.from) && Objects.equals(to, avd_edge.to);  
    }  
  
    @Override  
    public int hashCode() {  
        return Objects.hash(from, to);  
    }  
}
```

#### ArchiViewDiagramVo

```java
import java.util.List;  
  
import com.hlc.codeanalyzesystem.entity.AbstractGraphVo;  
import lombok.AllArgsConstructor;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
  
/**  
 * 架构图实体类，用于转换成JSON对象返回给前端  
 */  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class ArchiViewDiagramVo extends AbstractGraphVo {  
    private List<AVD_Node> nodeDataArray;  
    private List<AVD_Edge> linkDataArray;  
}
```



### Util类：`ArchiViewAlg`

```java
package com.hlc.codeanalyzesystem.utils;

import com.hlc.codeanalyzesystem.entity.AVD.AVD_Edge;
import com.hlc.codeanalyzesystem.entity.AVD.AVD_Node;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
@Slf4j
//@ConfigurationProperties(prefix = "archiViewAlg")
public class ArchiViewAlg {
    // Jar包所在路径，读取application.yml的配置
    @Value("${archiViewAlg.jarDir}")
    private String jarDir;
    // 工具包的相对路径
    private final String jar = "\\arcade_java11_forSYS.jar";
    private final String mallet = "\\mallet-2.0.7\\bin\\mallet.bat"; // windows 为mallet.bat，Linux 为 mallet
    private final String pkg = "\\arcadepy\\src\\arc\\batchpackager.py";
    private final String pyDir = "\\python2.7\\python.exe";
    
    // 该工具生成的结果名字
    private final String[] rsf_names = new String[]{"\\acdc\\acdc_clustered.rsf","\\arc\\arc_clusters.rsf","\\pkg\\pkg_clusters.rsf","\\deps\\deps.rsf"};
    
    // key为项目id，val为执行状态，0为未执行，ACDC和PKG执行后状态为1，ARC分四步运行，状态分别为1~4
    private static final Map<Integer, Integer> ACDC_state = new HashMap<>();
    private static final Map<Integer, Integer> ARC_state = new HashMap<>();
    private static final Map<Integer, Integer> PKG_state = new HashMap<>();

    // 测试类
    public void test() {
        System.out.println("ArchiViewAlg:::"+jarDir);
    }
	
    // 用于重设某个项目所有算法的运行状态，当项目过期或者类前缀发生变化可用
    public void reset_state(int pid){
        if (ACDC_state.containsKey(uid)){
            ACDC_state.replace(pid,0);
        }
        if (ARC_state.containsKey(uid)){
            ARC_state.replace(pid,0);
        }
        if (PKG_state.containsKey(uid)){
            PKG_state.replace(pid,0);
        }
    }
	
    // 用于执行命令行命令，cmd为要执行的命令行命令
    private Integer runCMD(String cmd) {
        Process p;
        System.out.println("ArchiViewAlg:::  运行："+cmd);
        try {
            //执行命令
            p = Runtime.getRuntime().exec(cmd);
            //获取输出流，并包装到BufferedReader中
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            Thread t1 = new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    String[] line = new String[1];
                    while((line[0] = br.readLine()) != null){
                        System.out.println("ArchiViewAlg:::\tSTD OUTPUT："+line[0]);
                    }

                }
            });

            Thread t2 = new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    String[] line = new String[1];
                    while((line[0] = stderrReader.readLine()) != null){
                        System.out.println("ArchiViewAlg:::\tERR OUTPUT："+line[0]);
                    }

                }
            });
            t1.start();
            t2.start();
            System.out.println("ArchiViewAlg:::  等待cmd执行结束");
            int exitValue = p.waitFor();
            stderrReader.close();
            br.close();
            System.out.println("ArchiViewAlg:::  进程返回值：" + exitValue);
            return exitValue;
        } catch (IOException | InterruptedException e) {
            System.out.println("ArchiViewAlg:::  cmd 运行失败");
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 运行ACDC
     * @param pid       所分析的项目id
     * @param input     要分析的项目路径
     * @param output    算法结果输出路径，会在该路径下自动生成acdc文件夹和deps文件夹
     * @param prefix    用于过滤项目外部类的类前缀
     * @return Boolean  返回是否执行成功
     */
    public Boolean runACDC(Integer pid, String input, String output, String prefix) {
        if (ACDC_state.getOrDefault(pid, 0) == 1) return true;

        System.out.println("ArchiViewAlg:::Running ACDC:");
        if (runCMD("java -jar " + jarDir + jar + " ACDC " + input + " " + output + " lib " + prefix + " 1") == 0) {
            ACDC_state.put(pid, 1);

            return true;
        } else {
            return false;
        }

    }
    
	/**
     * 运行ARC
     * @param pid       所分析的项目id
     * @param input     要分析的项目路径
     * @param output    算法结果输出路径，会在该路径下自动生成arc文件夹和deps文件夹
     * @param prefix    用于过滤项目外部类的类前缀
     * @return Boolean  返回是否执行成功
     */
    public Boolean runARC(Integer pid, String input, String output, String prefix) {
        int state = ARC_state.getOrDefault(pid, 0);
        // 不会每次都从头开始运行算法，判断state，防止重复运行
        if (state == 0){
            System.out.println("ArchiViewAlg:::Running ARC_pipe:");
            // run ARC_Pipe
            String cmd1 = "java -jar " + jarDir + jar + " ARC_Pipe " + input + " " + output + " lib " + prefix;
            if (runCMD(cmd1) == 0)
                ARC_state.put(pid, 1);
            else return false;
        }
        if (state < 2){
            System.out.println("ArchiViewAlg:::Running mallet step 1");
            // run import-dir
            String cmd2 = jarDir + mallet + " import-dir --input " + input + " --remove-stopwords TRUE --keep-sequence TRUE --output " + output + "\\arc\\base\\topicmodel.data";
            if (runCMD(cmd2) == 0)
                ARC_state.put(pid, 2);
            else return false;
        }

        if (state < 3) {
            System.out.println("ArchiViewAlg:::Running mallet step 2");
            // run train-topics
            String cmd3 = jarDir + mallet + " train-topics --input " + output + "\\arc\\base\\topicmodel.data --inferencer-filename " + output + "\\arc\\base\\infer.mallet --num-top-words 50 --num-topics 100 --num-threads 3 --num-iterations 100 --doc-topics-threshold 0.1";
            if (runCMD(cmd3) == 0)
                ARC_state.put(pid, 3);
            else return false;
        }
        // 第四阶段运行之后算法会自动删除前三个阶段所生成的中间文件，即arc/base文件夹内的文件
        if (state < 4) {
            System.out.println("ArchiViewAlg:::Running ARC_Clustering");
            // run ARC_Clustering
            String cmd4 = "java -jar " + jarDir + jar + " ARC_Clustering " + input + " " + output + " lib " + prefix;
            if (runCMD(cmd4) == 0) {
                ARC_state.put(pid, 4);
                return true;
            } else return false;
        }
        return true;
    }
	
    /**
     * 运行PKG
     * @param pid       所分析的项目id
     * @param input     要分析的项目路径
     * @param output    算法结果输出路径，会在该路径下自动生成pkg文件夹
     * @param prefix    用于过滤项目外部类的类前缀
     * @return Boolean  返回是否执行成功
     */
    public Boolean runPKG(Integer pid, String input, String output, String prefix) {
        if (PKG_state.getOrDefault(pid, 0) == 1) return true;
        String depsDir = output + "\\deps";
        File file = new File(depsDir);
        // 该算法需要分析deps.rsf，因此需要先运行ACDC
        if (!file.exists()) {
            System.out.println("ArchiViewAlg:::  没有依赖文件，先运行ACDC");
            if (runACDC(pid,input,output,prefix)){
                System.out.println("ArchiViewAlg:::  ACDC运行成功，继续运行PKG");
            }else{
                System.out.println("ArchiViewAlg:::  ACDC运行失败");
                return false;
            }
        }

        if (runCMD(jarDir + pyDir + " " + jarDir + pkg + " --startdir " + depsDir + " --outputfile " + output + "\\pkg" +  " --pkgprefixes " + prefix) == 0) {
            PKG_state.put(pid, 1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 解析复现算法的结果文件和依赖文件
     * @param outputDir 存有上述方法生成的文件的输出文件夹
     * @param alg_num   指定是哪个算法，0 = ACDC，1 = ARC，2 = PKG
     * @return Pair<List<AVD_Node>, List<AVD_Edge>>  返回是否执行成功
     */
    public Pair<List<AVD_Node>, List<AVD_Edge>> scanRSF(String outputDir, int alg_num) {
         List<AVD_Node> nodes = new ArrayList<AVD_Node>();
         List<AVD_Edge> edges;

         // 处理contains关系
         HashMap<Integer, String> map_ = new HashMap<>(); // key对应哪个节点(名字)
         HashMap<String, AVD_Node> map = new HashMap<>(); // 节点名字对应哪个节点对象
         int key = 1; // 节点标号

         // 打开文件
         String rsfFilePath =  outputDir + rsf_names[alg_num];

         File rsf_file = new File(rsfFilePath);
         if (!rsf_file.exists()) {
             System.out.println("ArchiViewAlg:::  rsf文件不存在："+rsfFilePath);
             return null;
         }

         // 打开文件
         String depsFilePath = outputDir + rsf_names[3];
         File deps_file = new File(depsFilePath);
         if (!deps_file.exists()) {
             System.out.println("ArchiViewAlg:::  deps文件不存在："+depsFilePath);
             return null;
         }

         System.out.println("ArchiViewAlg:::  开始扫描RSF");
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(rsf_file));
             String tempString;
             // 一次读入一行，直到读入null为文件结束
             while ((tempString = reader.readLine()) != null) {
                 // 显示行号
//                System.out.println("line " + line + ": " + tempString);

                 int group;
                 String[] info = tempString.split(" ");
                 if (!map.containsKey(info[1])) {
                     AVD_Node node = new AVD_Node();
                     group = key;
                     node.setKey(key);
                     node.setisGroup(true);
                     node.setCategory("OfNodes");
                     node.setText(info[1]);

                     map_.put(key, info[1]);
                     map.put(info[1], node);
                     key++;
                 } else {
                     AVD_Node node = map.get(info[1]);
                     group = node.getKey();
                     node.setisGroup(true);
                     node.setCategory("OfNodes");
                 }

                 if (!map.containsKey(info[2])) {
                     AVD_Node node = new AVD_Node();
                     node.setKey(key);
                     node.setisGroup(false);
                     node.setGroup(group);
                     node.setText(info[2]);

                     map_.put(key, info[2]);
                     map.put(info[2], node);
                     key++;
                 }
             }
             reader.close();


         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e1) {
                 }
             }
             for (Map.Entry<String, AVD_Node> entry : map.entrySet()) {
                 AVD_Node node = entry.getValue();
                 if (node.getGroup() != null && node.getisGroup()) {
                     map.get(map_.get(node.getGroup())).setCategory("ofGroups");
                 }
             }

             for (Map.Entry<String, AVD_Node> entry : map.entrySet()) {
                 nodes.add(entry.getValue());
             }
         }

         // 处理depends关系
         // 用Set防止重复
         Set<AVD_Edge> edgeSet = new HashSet<>();
         reader = null;
         try {
             reader = new BufferedReader(new FileReader(depsFilePath));
             String tempString;
             // 一次读入一行，直到读入null为文件结束
             while ((tempString = reader.readLine()) != null) {
                 // 显示行号
                 String[] info = tempString.split(" ");
                 String from = info[1]; // 起点
                 String to = info[2]; // 终点

                 AVD_Node fromNode = map.get(from);
                 AVD_Node toNode = map.get(to);

                 if (fromNode ==  null || toNode == null){
                     continue;
                 }
                 if (!fromNode.getisGroup() && !toNode.getisGroup() && !fromNode.getGroup().equals(toNode.getGroup())) {
                     // 若两个类不属于同一个组件
                     // 则在组件直接建立箭头，而不在类与类直接建立箭头
                    edgeSet.add(new AVD_Edge(fromNode.getGroup(), toNode.getGroup()));

                 } else {
                     // 若两个类属于同一个组件
                     // 则直接建立箭头
                     edgeSet.add(new AVD_Edge(fromNode.getKey(), toNode.getKey()));
                 }
             }
             reader.close();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e1) {
                 }
             }

             // 将edgeSet中的数据给到edges
             edges = new ArrayList<AVD_Edge>(edgeSet);
         }

         return new Pair<>(nodes, edges);
    }

}

```

### 示例：Service类中的方法：

```java
@Autowired  
private ArchiViewAlg archiViewAlg;

/**
     * 架构视图分析
     *
     * @param pid         所分析的项目id
     * @param projectName 所分析的项目名称
     * @param prefix      需要分析的类前缀
     * @param alg         所调用的算法
     * @return ArchiViewDiagramVo 架构试图对象
     */
public String avdAnalysis(Integer uid, Integer pid, String projectName, String prefix, String alg) throws Exception {
    if (prefix == null || "".equals(prefix)) {
        System.out.println("类前缀为空");
        return "prefix is empty";
    }

    int type;
    if ("ACDC".equals(alg)){
        type = 4;
    }else if ("ARC".equals(alg)){
        type = 5;
    }else if ("PKG".equals(alg)){
        type = 6;
    }else{
        return "wrong alg";
    }

    //1. 项目路径(文件夹)
    String projectPath = resourcePath + pid + File.separator;
    File folder = new File(projectPath);
    if (!folder.isDirectory()) {
        System.out.println("Folder path provided is not valid, please check -> " + projectPath);
        throw new Exception("文件路径错误");
    }

    //2. 从数据库中获取项目对应图(projectGraphDao)
    Projectgraph projectgraph = projectGraphDao.selectByPidAndType(pid, type);
    String _prefix = prefix;

    System.out.println("pid = "+pid+", alg = "+alg+", prefix = "+prefix);
    if (projectgraph != null) {
        System.out.println("数据库中存在图");
        String jsonPath = projectgraph.getGraphjsonpath();
        String[] path_split = jsonPath.split("\\\\");
        String temp = path_split[path_split.length-1];
        _prefix = temp.split("_")[0];
        if (_prefix.equals("")) System.out.println("为空");
        System.out.println("old prefix = "+_prefix+", cur prefix = "+prefix);
    }

    // 数据库存在图且状态为最新，且前缀与当前用户指定的前缀相同，则直接读库返回
    if (projectgraph != null && projectgraph.getState() == 2 && _prefix.equals(prefix)) {
        System.out.println("从数据库提取");//直接从数据库读取图
        String avdGraphJson;
        File file = new File(projectgraph.getGraphjsonpath());//获取图的json路径并新建文件
        avdGraphJson = FileUtils.readFileToString(file, "UTF-8");//转成字符串
        return avdGraphJson;
    } else {
        if(projectgraph != null){
            System.out.println("前缀不同 或 项目过期，重新运行算法");
            archiViewAlg.reset_state(pid);
        }
        //若数据库中不存在该图或不处于2状态(最新状态)或类前缀有变化
        List<File> files = new ArrayList<>();
        FileUtil.getAllFilesFromFolder(folder, files,"jar");//从项目文件夹中获取所有Java文件，存在列表中
        if (files.isEmpty()) {
            return "bad project";
        }
        files.clear();
        FileUtil.getAllFilesFromFolder(folder, files,"java");
        if (files.isEmpty()) {
            return "bad project";
        }
        
        //------------------------------调用算法生成图--------------------------------

        ArchiViewDiagramVo archiViewDiagramVo = null;
        Pair<List<AVD_Node>, List<AVD_Edge>> pair = null;
        System.out.println("运行算法："+alg);
        String outputDir = projectPath + "output";
        switch (alg) {
            case "ACDC":
                if (archiViewAlg.runACDC(pid, projectPath, outputDir, prefix)) {
                    pair = archiViewAlg.scanRSF(outputDir, 0);
                    if (pair == null){
                        break;
                    }
                    System.out.println("算法运行完毕，rsf成功读入");
                }
                break;
            case "ARC":
                if (archiViewAlg.runARC(pid, projectPath, outputDir, prefix)) {
                    pair = archiViewAlg.scanRSF(outputDir, 1);
                    if (pair == null){
                        break;
                    }
                    System.out.println("算法运行完毕，rsf成功读入");
                }
                break;
            case "PKG":
                if (archiViewAlg.runPKG(pid, projectPath, outputDir, prefix)) {
                    pair = archiViewAlg.scanRSF(outputDir, 2);
                    if (pair == null){
                        break;
                    }
                    System.out.println("算法运行完毕，rsf成功读入");
                }
                break;
        }


        if (pair != null) {
            System.out.println("将rsf内容写入对象");
            archiViewDiagramVo = new ArchiViewDiagramVo(pair.getValue0(), pair.getValue1());
            archiViewDiagramVo.setLabel("AVD of " + projectName);
        } else {
            return "error generating AVD";
        }

        //-------------------------保存json并获取最新的josn路径，将图保存到数据库-------------------------------------

        if (projectgraph == null) {//若数据库中不存在图
            projectgraph = new Projectgraph();//新建图
            projectgraph.setPid(pid);//设置项目id
            projectgraph.setType(type);//设置图类型(4)
            projectgraph.setState(2);//设置状态
            projectgraph.setGraphjsonpath("");
            projectGraphDao.insertSelective(projectgraph);//更新图并获取最新的图对象
        }else{
            // 若数据库中存在图则删除原来的json文件
            String jsonPath = projectgraph.getGraphjsonpath();
            File jsonFile = new File(jsonPath);
            if (jsonFile.exists()){
                jsonFile.delete();
            }
            projectgraph.setState(2);//设置状态
        }
        String projectGraphDir = Path.Project_dir + pid + File.separator + "projectgraph" + File.separator;

        if (!FileUtil.isDir(projectGraphDir)) {
            FileUtil.makeDirs(projectGraphDir);
        }
        String dir = projectGraphDir + prefix + "_" + alg + "_" + projectgraph.getId() + ".json";
        File file = new File(dir);
        if (!file.exists()) {
            file.createNewFile();
        }
        System.out.println("将Json写入文件");
        String res = JSON.toJSONString(archiViewDiagramVo);
        try {//将json格式的图写入文件中
            FileUtils.write(file, res, "utf-8", false);
        } catch (Exception e) {
            return "error saveing AVD";
        }
        System.out.println("更新数据库");
        projectGraphDao.updateByIdAndJsonPath(projectgraph.getId(), dir);//更新图的json路径

        System.out.println("完毕！");
        return res;
}
```

