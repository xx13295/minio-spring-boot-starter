package plus.ojbk.minio.util;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @author wxm
 * @version 1.0
 * @since 2021/6/5 15:11
 */
public class MinioUtils {
    /**
     * 获取文件后缀
     *
     * @param fileName
     * @return
     */
    public static String getSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            String suffix = fileName.substring(index + 1);
            if (!suffix.isEmpty()) {
                return suffix;
            }
        }
        throw new IllegalArgumentException("非法文件名称：" + fileName);
    }


    /**
     * 获取年月日[2020, 09, 01]
     *
     * @return
     */
    public static String[] getDateFolder() {
        String[] retVal = new String[3];
        LocalDate localDate = LocalDate.now();
        retVal[0] = localDate.getYear() + "";
        int month = localDate.getMonthValue();
        retVal[1] = month < 10 ? "0" + month : month + "";
        int day = localDate.getDayOfMonth();
        retVal[2] = day < 10 ? "0" + day : day + "";
        return retVal;
    }

    /**
     * 获取UUID
     *
     * @return
     */
    public static String getUUID(){
       return  UUID.randomUUID().toString().replace("-", "");
    }
}
