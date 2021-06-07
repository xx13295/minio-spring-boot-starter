package plus.ojbk.minio.core;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
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
import java.io.InputStream;
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
     *
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
    private void initDefaultBucket() {
        createBucket(bucket);
        log.info("Init Default Bucket :{} .", bucket);
    }

    /**
     * 创建bucket
     *
     * @param bucket
     */
    public void createBucket(String bucket) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取整个对象的数据作为给定存储桶中的InputStream 。 InputStream 必须在使用后关闭，否则连接将保持打开状态。
     * 例子：
     * <p>
     * InputStream in = minioTemplate.getObjectInputStream("bucket", "object");
     * OutputStream out = response.getOutputStream();
     * byte[] buf = new byte[1024];
     * int len = 0;
     * response.reset();
     * response.setContentType("application/x-msdownload");
     * response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(MinioUtils.getFileName(object), "UTF-8"));
     * while ((len = in.read(buf)) > 0) {
     * out.write(buf, 0, len);
     * }
     * in.close();
     * out.close();
     *
     * @param bucket 存储桶名称
     * @param object 存储桶中的对象名称
     * @return
     */
    public InputStream getObjectInputStream(String bucket, String object) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .offset(0L).length(null)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取默认 bucket 中的文件 InputStream
     *
     * @param object 存储桶中的对象名称
     * @return
     */
    public InputStream getObjectInputStream(String object) {
        return getObjectInputStream(bucket, object);
    }


    /**
     * 获取文件url 自定义过期时间
     *
     * @param bucket   存储桶名称
     * @param object   存储桶中的对象名称
     * @param duration 时长
     * @param unit     单位
     * @return
     */
    public String getObject(String bucket, String object, int duration, TimeUnit unit) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).expiry(duration, unit)
                    .bucket(bucket)
                    .object(object).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件url
     * 过期时间 默认1小时
     *
     * @param object 存储桶中的对象名称
     * @return
     */
    public String getObject(String object) {
        return getObject(object, 1, TimeUnit.HOURS);
    }

    /**
     * 获取文件url 自定义过期时间
     *
     * @param object   存储桶中的对象名称
     * @param duration 时长
     * @param unit     单位
     * @return
     */
    public String getObject(String object, int duration, TimeUnit unit) {
        return getObject(bucket, object, duration, unit);
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
    public ObjectWriteResponse putObject(MultipartFile multipartFile) {
        try {
            String[] folders = MinioUtils.getDateFolder();
            String fileName = MinioUtils.getUUID() + "." + MinioUtils.getSuffix(multipartFile.getOriginalFilename());
            // 年/月/日/file
            String finalPath = String.join(URI_DELIMITER, folders) + URI_DELIMITER + fileName;
            return minioClient.putObject(PutObjectArgs.builder()
                    .stream(multipartFile.getInputStream(), multipartFile.getSize(), ObjectWriteArgs.MIN_MULTIPART_SIZE)
                    .object(finalPath)
                    .contentType(multipartFile.getContentType())
                    .bucket(bucket)
                    .build());
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
                String finalPath = String.join(URI_DELIMITER, folders) + URI_DELIMITER + fileName;
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
