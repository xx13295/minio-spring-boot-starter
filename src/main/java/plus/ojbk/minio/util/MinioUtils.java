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
     * @param fileName 文件名
     * @return String
     */
    public static String getSuffix(String fileName) {
        if(fileName == null){
            throw new IllegalArgumentException("非法文件名称");
        }
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
     * 获取文件名
     * @param object 存储桶中的对象名称
     * @return String
     */
    public static String getFileName(String object){
        if(object == null || "".equals(object)){
            throw new IllegalArgumentException("非法文件名称");
        }
        String[] a =  object.split("/");
        return a[a.length-1];
    }

    /**
     * 获取年月日[2020, 09, 01]
     *
     * @return String
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
     * @return String
     */
    public static String getUUID(){
       return  UUID.randomUUID().toString().replace("-", "");
    }
}
