package online.yardstrong.netty.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常处理工具
 *
 * @author yuanqiang
 * @date 2021-10-15
 */
public class ExceptionUtil {

    public static String translateToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
