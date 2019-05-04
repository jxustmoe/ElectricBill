package cn.jxust.electricBill.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * POST请求发送类
 */
public class PostSender {

    //目标url连接对象
    private HttpURLConnection connection;

    //POST传递的参数
    private String parms;

    //请求时携带的Cookie
    private String cookie;

    public PostSender(String url) throws IOException {
        connection = (HttpURLConnection) new URL(url).openConnection();
    }

    /**
     * 返回连接对象,方便设置connection的其它属性
     *
     * @return 连接对象
     */
    public HttpURLConnection getConnection() {
        return connection;
    }

    /**
     * 设置并解析POST的请求参数
     */
    public void setParms(Map<String, String> parms) {
        StringBuffer sb = new StringBuffer();

        //构建POST参数字符串
        for (Map.Entry<String, String> entry : parms.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue() + "&");
        }

        this.parms = sb.deleteCharAt(sb.length() - 1).toString();
    }

    /**
     * 设置请求时携带的cookie
     *
     * @param cookie cookie
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /**
     * 获取响应头中的字段信息
     *
     * @param fieldName 响应头字段名
     * @return 字段信息
     */
    public String getHeaderField(String fieldName) {
        return connection.getHeaderField(fieldName);
    }

    /**
     * 获取响应报文输入流
     *
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        String encoding = connection.getHeaderField("Content-Encoding");
        if (encoding != null && encoding.contains("gzip")) {
            return new GZIPInputStream(connection.getInputStream());
        } else {
            return connection.getInputStream();
        }
    }

    /**
     * 发送POST请求
     *
     * @throws IOException
     */
    public void send() throws IOException {

        //设置相关请求数据
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(5000);

        //判断是否携带cookie
        if (cookie != null) {
            connection.setRequestProperty("Cookie", cookie);
        }

        //写入POST请求体
        OutputStream os = connection.getOutputStream();
        os.write(parms.getBytes("UTF-8"));
        os.close();

    }
}
