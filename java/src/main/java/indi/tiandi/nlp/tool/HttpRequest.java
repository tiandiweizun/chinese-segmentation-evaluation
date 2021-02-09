package indi.tiandi.nlp.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Http下载
 *
 * @author tiandi
 * @date 2021/2/18
 */
public class HttpRequest {
    /**
     * 从网络Url中下载文件
     *
     * @param httpUrl  url地址
     * @param saveFile 保存文件路径和名称
     * @throws IOException
     */
    public static boolean download(String httpUrl, String saveFile) throws IOException {
        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;
        URL url = new URL(httpUrl);
        URLConnection conn = url.openConnection();
        File file = new File(saveFile);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (InputStream inStream = conn.getInputStream();
             FileOutputStream fs = new FileOutputStream(saveFile);
        ) {
            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                fs.write(buffer, 0, byteread);
            }
            return true;
        } catch (IOException e) {
            throw e;
        }
    }
}