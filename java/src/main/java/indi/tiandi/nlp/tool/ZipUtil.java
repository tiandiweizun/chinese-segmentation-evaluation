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
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        // 指定编码，否则压缩包里面不能有中文目录
        InputStream in = null;
        OutputStream out = null;
        ZipFile zip = new ZipFile(zipFile, Charset.forName("gbk"));
        try {
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                in = zip.getInputStream(entry);
                String outPath = Paths.get(descDir, zipEntryName).toString();
                // 判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0,
                        outPath.lastIndexOf(File.separator)));
                if (!file.exists()) {
                    file.mkdirs();
                }
                // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                out = new FileOutputStream(outPath);
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
