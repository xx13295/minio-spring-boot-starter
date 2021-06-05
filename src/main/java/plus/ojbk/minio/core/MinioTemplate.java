package plus.ojbk.minio.core;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteArgs;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import plus.ojbk.minio.autoconfigure.properties.MinioProperties;
import plus.ojbk.minio.util.MinioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wxm
 * @version 1.0
 * @since 2021/6/5 13:58
 */
public class MinioTemplate {

    private Logger log = LoggerFactory.getLogger(getClass());

    private MinioProperties properties;

    public MinioTemplate(MinioProperties properties) {
        this.properties = properties;
    }

    private String endpoint;
    private String bucket;
    private String accessKey;
    private String secretKey;

    /**
     * url分隔符
     */
    public static final String URI_DELIMITER = "/";

    @Autowired
    private MinioClient minioClient;

    private MinioClient expandClient;

    /**
     * 拓展使用
     * 获取 MinioClient
     * @return
     */
    public MinioClient getMinioClient() {
        if (expandClient == null) {
            synchronized (MinioTemplate.class) {
                if (expandClient == null) {
                    this.expandClient = minioClient;
                }
            }
        }
        return expandClient;
    }

    @PostConstruct
    private void init() {
        this.endpoint = properties.getEndpoint();
        this.bucket = properties.getBucket();
        this.accessKey = properties.getAccessKey();
        this.secretKey = properties.getSecretKey();
    }

    @PostConstruct
    private void initDefaultBucket() throws Exception {
        if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            log.info("Bucket :{} already exists.", bucket);
        } else {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    /**
     * 获取文件url
     * 过期时间 默认1小时
     *
     * @param object 文件名
     * @return
     */
    public String getObject(String object) {
        return getObject(object, 1, TimeUnit.HOURS);
    }

    /**
     * 获取文件url 自定义过期时间
     *
     * @param object   文件名
     * @param duration 时长
     * @param unit     单位
     * @return
     */
    public String getObject(String object, int duration, TimeUnit unit) {
        String url;
        try {
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).expiry(duration, unit)
                    .bucket(bucket)
                    .object(object).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    /**
     * 删除文件
     *
     * @param object
     */
    public void deleteObject(String object) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    public ObjectWriteResponse putObject(MultipartFile multipartFile){
        try {
        String[] folders = MinioUtils.getDateFolder();
        String fileName = MinioUtils.getUUID() + "." + MinioUtils.getSuffix(multipartFile.getOriginalFilename());
        // 年/月/日/file
        String finalPath = new StringBuilder(String.join(URI_DELIMITER, folders))
                .append(URI_DELIMITER)
                .append(fileName).toString();
        ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .stream(multipartFile.getInputStream(), multipartFile.getSize(), ObjectWriteArgs.MIN_MULTIPART_SIZE)
                    .object(finalPath)
                    .contentType(multipartFile.getContentType())
                    .bucket(bucket)
                    .build());
        return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 上传文件
     *
     * @param multipartFiles
     * @return
     */
    public List<ObjectWriteResponse> putObject(MultipartFile... multipartFiles) {
        try {
            List<ObjectWriteResponse> retVal = new ArrayList<>();
            String[] folders = MinioUtils.getDateFolder();
            for (MultipartFile multipartFile : multipartFiles) {
                String fileName = MinioUtils.getUUID() + "." + MinioUtils.getSuffix(multipartFile.getOriginalFilename());
                String finalPath = new StringBuilder(String.join(URI_DELIMITER, folders))
                        .append(URI_DELIMITER)
                        .append(fileName).toString();
                ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                        .stream(multipartFile.getInputStream(), multipartFile.getSize(), ObjectWriteArgs.MIN_MULTIPART_SIZE)
                        .object(finalPath)
                        .contentType(multipartFile.getContentType())
                        .bucket(bucket)
                        .build());

                retVal.add(response);
            }
            return retVal;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
