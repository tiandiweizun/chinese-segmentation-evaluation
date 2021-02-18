package indi.tiandi.nlp.evaluation;

import indi.tiandi.nlp.Seg;
import indi.tiandi.nlp.Term;
import indi.tiandi.nlp.tool.HttpRequest;
import indi.tiandi.nlp.tool.ZipUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * SegEvaluation
 * 分词评估器
 *
 * @author tiandi
 * @date 2019/3/4
 */
public class SegEvaluation {
    public static final String testSentence = "这是一个测试句子";

    public static final String helpMessage;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("thank you for using nlp-evaluation\n");
        sb.append("github: https://github.com/tiandiweizun/chinese-segmentation-evaluation\n");
        sb.append("\t-i or -input\t\t\tfile to segment,jar default using the file in ./data/seg_data_big.txt and debug model using chinese-segmentation-evaluation/data/seg_data_big.txt\n");
        sb.append("\t-o or -output\t\t\tpath to save the result, default is not saving\n");
        sb.append("\t-n or -max_line_number\t\tmaximum number of read rows, default reading all\n");
        sb.append("\t-c or -contains\t\t\t segmentor to evaluate，default contains HanLP，jieba，thulac\n");
        sb.append("\t-h or -help\t\t\tmessage for help\n");
        sb.append("\n");
        sb.append("\te.g., java -jar nlp-evaluation-java-1.0.0.jar -n=10\n");
        sb.append("\te.g., java -jar nlp-evaluation-java-1.0.0.jar nlp-evaluation/data/seg.data_big -n=10\n");
        helpMessage = sb.toString();
    }

    public static String getFileNameWithExtension(File file) {
        String fileName = file.getName();
        int i = fileName.lastIndexOf(".");
        if (i <= 0) {
            i = fileName.length();
        }
        return fileName.substring(0, i);
    }

    public static void main(String[] args) throws Exception {
        Config config = parseParams(args);

        String rFileName = config.rFileName;
        String wFilePath = config.wFilePath;
        boolean writeResult = false;
        int maxLineCount = config.maxLineCount;

        InputStream inputStream = null;
        File file = new File(rFileName);
        if (!file.exists()) {
            URL resource = SegEvaluation.class.getClassLoader().getResource(rFileName);
            if (resource != null) {
                // 从jar包内部加载
                inputStream = SegEvaluation.class.getClassLoader().getResourceAsStream((rFileName));
            } else {
                File tempZipFile = new File(Config.zipFileName);
                if (getFileNameWithExtension(file).equals(getFileNameWithExtension(tempZipFile))) {
                    boolean download = true;
                    if (tempZipFile.exists()) {
                        try {
                            //解压
                            ZipUtil.unZip(tempZipFile, tempZipFile.getParent());
                            download = false;
                        } catch (Exception e) {
                            //删除错误zip文件
                            tempZipFile.delete();
                        }
                    }
                    // 从互联网下载并解压
                    if (download) {
                        System.out.println(String.format("从 %s 下载文件，如果下载较慢，亦可手动下载，保存到 %s 即可", Config.url, tempZipFile.getAbsolutePath()));
                        try {
                            // 下载
                            HttpRequest.download(Config.url, Config.zipFileName);
                            System.out.println("下载完成");
                            // 解压
                            ZipUtil.unZip(tempZipFile, tempZipFile.getParent());
                        } catch (IOException e) {
                            System.out.println(String.format("下载或解压错误：%s", e.getMessage()));
                            System.exit(1);
                        }
                    }
                } else {
                    // 自定义的文件未找到
                    System.out.println("未从本地和jar包内找到文件:" + rFileName);
                    System.exit(1);
                }
            }
        }
        if (file.exists()) {
            inputStream = new FileInputStream(rFileName);
            System.out.println("读入分词文件地址:" + file.getAbsolutePath());
        }
        if (wFilePath.length() > 0) {
            writeResult = true;
            System.out.println("分词结果写入地址:" + new File(wFilePath).getAbsolutePath());
        }
        calcPFRScore(inputStream, wFilePath, writeResult, maxLineCount, config.segmentorNames);
    }

    public static void calcPFRScore(InputStream inputStream, String wFilePath, boolean writeResult, int maxLineCount,
                                    List<String> segmentorNames) {
        String line = "";
        try {
            List<Evaluator> evaluators = new ArrayList<>();
            List<String> classesFromPackage = getClassNames("indi.tiandi.nlp.evaluation.impl");
            for (String segmentorName : segmentorNames) {
                for (String className : classesFromPackage) {
                    int i = className.lastIndexOf(".") + 1;
                    String simpleClassName = className.substring(i);
                    if (!simpleClassName.toLowerCase().startsWith(segmentorName.toLowerCase())) {
                        continue;
                    }
                    Class<?> aClass = Class.forName(className);
                    if (Seg.class.isAssignableFrom(aClass)) {
                        evaluators.add(new Evaluator(aClass.asSubclass(Seg.class), segmentorName));
                        break;
                    }
                }
            }

//            evaluators.add(new Evaluator(JiebaAnalysisImpl.class));
//            evaluators.add(new Evaluator(ThulacImpl.class));
//            分词太慢
//            evaluators.add(new Evaluator(new StanfordCoreNLPImpl()));
//            以下分词都存在bug，导致分词后的句子与分词前的句子不一样
//            evaluators.add(new Evaluator(WordImpl.class));
//            evaluators.add(new Evaluator(AnsjImpl.class));
//            evaluators.add(new Evaluator(JcsegImpl.class));
//            evaluators.add(new Evaluator(MMSeg4jImpl.class));
            // 获得项目根目录的绝对路径
            if (evaluators.size() == 0) {
                System.out.println("没有任何待评测分词器");
                System.exit(-1);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            List<List<String>> gold = new ArrayList<>();
            List<String> test = new ArrayList<>();
            int charCount = 0;
            boolean calcScore = true;
            int lineCount = 0;
            // -1 表示读取所有数据
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] s = line.split(" ");
                gold.add(Arrays.asList(s));
                test.add(line.replace(" ", ""));
                charCount += test.get(test.size() - 1).length();

                lineCount += 1;
                if (maxLineCount > 0 && lineCount >= maxLineCount) {
                    break;
                }
            }
            System.out.println();
            System.out.println(String.format("总行数:%d\t总字符数:%d", gold.size(), charCount));
            for (Evaluator item : evaluators) {

                System.out.println();
                System.out.println(item.seg.getName() + " 评测开始");
                if (!item.init) {
                    System.out.println(item.seg.getName() + " 初始化错误,跳过");
                    continue;
                }
                BufferedWriter bw = null;
                if (writeResult) {
                    wFilePath = wFilePath.replace("\\", "/");
                    if (!wFilePath.endsWith("/")) {
                        wFilePath += "/";
                    }
                    String wFileName = wFilePath + item.seg.getName();
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wFileName), "utf-8"));
                }
                long start = System.currentTimeMillis();
                int right_num = 0;
                int gold_num = 0;
                int predict_num = 0;
                for (int i = 0; i < test.size(); i++) {
                    line = test.get(i);
                    List<Term> segment = item.seg.segment(line);
                    List<String> predict = new ArrayList<>();
                    for (Term term : segment) {
                        predict.add(term.getWord());
                    }
                    if (calcScore) {
                        if (!StringUtils.join(predict, "").equals(line)) {
                            System.out.println(item.seg.getName() + "\t" + line);
                            continue;
                        }
                        int result[] = calcScore(gold.get(i), predict);
                        right_num += result[0];
                        gold_num += result[1];
                        predict_num += result[2];
                    }
                    if (writeResult) {
                        bw.write(StringUtils.join(predict, " ") + "\n");
                    }
                }
                if (writeResult) {
                    bw.close();
                }
                item.time = System.currentTimeMillis() - start;
                double precision = 0;
                double recall = 0;
                double f = 0;
                if (predict_num != 0) {
                    precision = right_num * 1.0 / predict_num;
                }
                if (gold_num != 0) {
                    recall = right_num * 1.0 / gold_num;
                }
                if (precision + recall > 0) {
                    f = 2 * precision * recall / (precision + recall);
                }
                System.out.println(String.format("precision:%f \t recall:%f \t f1:%f", precision, recall, f));
                System.out.println(String.format("耗时:%d ms,\t速度:%f 字符/毫秒", item.time, charCount * 1.0 / item.time));
            }
        } catch (Exception e) {
            System.out.println(line);
            e.printStackTrace();
        }
    }

    public static int[] calcScore(List<String> gold, List<String> predict) {
        int gold_offset = 0;
        int predict_offset = 0;

        int gold_term_index = 0;
        int predict_term_index = 0;

        int right = 0;
        int total = gold.size();
        int right_and_wrong = predict.size();
        while (gold_term_index < total || predict_term_index < right_and_wrong) {
            if (gold_offset == predict_offset) {
                if (gold.get(gold_term_index).equals(predict.get(predict_term_index))) {
                    right += 1;
                }
                int[] result = update(gold_offset, gold_term_index, gold);
                gold_offset = result[0];
                gold_term_index = result[1];
                result = update(predict_offset, predict_term_index, predict);
                predict_offset = result[0];
                predict_term_index = result[1];
            } else if (gold_offset < predict_offset) {
                int[] result = update(gold_offset, gold_term_index, gold);
                gold_offset = result[0];
                gold_term_index = result[1];
            } else {
                int[] result = update(predict_offset, predict_term_index, predict);
                predict_offset = result[0];
                predict_term_index = result[1];
            }
        }
        int[] result = {right, total, right_and_wrong};
        return result;
    }

    public static int[] update(int offset, int index, List<String> terms) {
        offset += terms.get(index).length();
        index += 1;
        int[] result = {offset, index};
        return result;
    }

    public static Config parseParams(String[] args) {
        // message for help
        try {

            Config config = new Config();
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help")) {
                    System.out.println(helpMessage);
                    System.exit(0);
                }
            }
            boolean containsOption = false;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-") && arg.contains("=")) {
                    containsOption = true;
                    String[] split = arg.split("=");
                    String paramName = split[0].trim().substring(1).toLowerCase();
                    String paramValue = split[1].trim();
                    switch (paramName) {
                        case "i":
                        case "input":
                            config.rFileName = paramValue;
                            break;
                        case "o":
                        case "output":
                            config.wFilePath = paramValue;
                            break;
                        case "n":
                        case "max_line_number":
                            config.maxLineCount = Integer.parseInt(paramValue);
                            break;
                        case "c":
                        case "contains":
                            String[] segmentorNames = paramValue.split(",");
                            config.segmentorNames = Arrays.asList(segmentorNames);
                            break;
                    }
                } else if (containsOption) {
                    System.out.println("optional argument follows keyword argument");
                } else {
                    if (i == 0) {
                        config.rFileName = args[0].trim();
                    } else if (i == 1) {
                        config.wFilePath = args[1].trim();
                    } else if (i == 2) {
                        config.maxLineCount = Integer.parseInt(args[2].trim());
                    }
                }
            }
            return config;
        } catch (Exception e) {
            System.out.println("参数错误：" + e.getMessage());
            System.out.println(helpMessage);
            System.exit(0);
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param packageName
     * @return
     */

    public static List<String> getClassNames(String packageName) {
        //第一个class类的集合
        List<String> classes = new ArrayList<>();
        //是否循环迭代
        boolean recursive = true;
        //获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去
            while (dirs.hasMoreElements()) {
                //获取下一个元素
                URL url = dirs.nextElement();
                //得到协议的名称
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    //获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    //如果是jar包文件
                    //定义一个JarFile
                    JarFile jar;
                    try {
                        //获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        //从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        //同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            //如果是以/开头的
                            if (name.charAt(0) == '/') {
                                //获取后面的字符串
                                name = name.substring(1);
                            }
                            //如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                //如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    //获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                //如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    //如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        //去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        //添加到classes
                                        classes.add(packageName + '.' + className);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */

    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
                                                        List<String> classes) {
        //获取此包的目录 建立一个File
        File dir = new File(packagePath);
        //如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        //循环所有文件
        for (File file : dirfiles) {
            //如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                //如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                classes.add(packageName + '.' + className);
            }
        }
    }
}

class Evaluator {

    public Seg seg;
    public long time = 0;
    public boolean init = false;

    public Evaluator(Class<? extends Seg> segClass) {
        this(segClass, segClass.getSimpleName());
    }

    public Evaluator(Class<? extends Seg> segClass, String name) {
        try {
            long start = System.currentTimeMillis();
            System.out.println(name + " 初始化开始");
            this.seg = segClass.newInstance();
            this.seg.setName(name);
            List<Term> terms = this.seg.segment(SegEvaluation.testSentence);
            StringBuilder sb = new StringBuilder();
            for (Term term : terms) {
                sb.append(term.getWord());
            }
            long end = System.currentTimeMillis();
            long cost = end - start;
            if (!sb.toString().equals(SegEvaluation.testSentence)) {
                System.out.println(name + " 初始化错误,句子:" + SegEvaluation.testSentence + ",分词结果:" + terms);
            } else {
                this.init = true;
                System.out.println(name + " 初始化结束,耗时:" + cost + " ms");
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

class Config {
    public static final String zipFileName = "data/seg_data_big.zip";
    public static final String url = "https://github.com/tiandiweizun/chinese-segmentation-evaluation/releases/download/v1.0.1/seg_data_big.zip";
    public String rFileName = "data/seg_data_big.txt";
    public String wFilePath = "";
    public boolean writeResult = false;
    public int maxLineCount = -1;
    public List<String> segmentorNames = new ArrayList<>(Arrays.asList("HanLP", "Jieba", "Thulac", "mynlp"));
}