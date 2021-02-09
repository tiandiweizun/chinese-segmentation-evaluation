package indi.tiandi.nlp.tool;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 压缩和解压缩（https://www.bbsmax.com/A/x9J2bZLMJ6/）
 * 更多压缩文件格式可以参见https://www.bookstack.cn/read/hutool/a56da94bbb16617b.md
 *
 * @author tiandi
 * @date 2021/2/1
 */
public class ZipUtil {
    public static boolean unZip(File zipFile, String descDir) throws IOException {
        boolean flag = false;
        // 指定编码，否则压缩包里面不能有中文目录
        InputStream in = null;
        OutputStream out = null;
        ZipFile zip = new ZipFile(zipFile, Charset.forName("gbk"));
        try {
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                File file = Paths.get(descDir, zipEntryName).toFile();
                File dir = file;
                if (!zipEntryName.endsWith("/") && !zipEntryName.endsWith("\\")) {
                    // 非文件夹获取父文件
                    dir = file.getParentFile();
                }
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (file.isDirectory()) {
                    continue;
                }
                in = zip.getInputStream(entry);
                out = new FileOutputStream(file);
                byte[] buf1 = new byte[2048];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            flag = true;
            // 必须关闭，否则无法删除该zip文件
        } catch (IOException exception) {
            zip.close();
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            throw exception;
        }
        return flag;
    }
}
