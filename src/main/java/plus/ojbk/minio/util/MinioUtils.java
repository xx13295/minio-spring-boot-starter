package plus.ojbk.minio.util;

import java.time.LocalDate;
import java.util.Arrays;
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
        return getDateFolder(null);
    }

    /**
     * 获取文件夹开头的年月日[folder,2020, 09, 01]
     *
     * @param folder
     * @return
     */
    public static String[] getDateFolder(String folder) {
        String[] retVal = new String[3];
        LocalDate localDate = LocalDate.now();
        retVal[0] = localDate.getYear() + "";
        int month = localDate.getMonthValue();
        retVal[1] = month < 10 ? "0" + month : month + "";
        int day = localDate.getDayOfMonth();
        retVal[2] = day < 10 ? "0" + day : day + "";
        String[] newRetVal = null;
        if (!(folder == null || "".equals(folder))) {
            newRetVal = Arrays.copyOf(retVal, retVal.length + 1);
            System.arraycopy(newRetVal, 0, newRetVal, 1, newRetVal.length - 1);
            newRetVal[0] = folder;
        }
        return newRetVal == null ? retVal : newRetVal;
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
