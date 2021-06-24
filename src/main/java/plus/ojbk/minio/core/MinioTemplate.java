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
     * @return MinioClient
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
        createBucket(bucket);
        log.info("Init Default Bucket :{} .", bucket);
    }

    /**
     * 创建bucket
     *
     * @param bucket 存储桶名称
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
     *
     * @param bucket 存储桶名称
     * @param object 存储桶中的对象名称
     * @return InputStream
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
     * 使用默认的bucket
     *
     * @param object 存储桶中的对象名称
     * @return InputStream
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
     * @return url
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
     * 使用默认的bucket
     *
     * @param object 存储桶中的对象名称
     * @return url
     */
    public String getObject(String object) {
        return getObject(object, 1, TimeUnit.HOURS);
    }

    /**
     * 获取文件url 自定义过期时间
     * 使用默认的bucket
     *
     * @param object   存储桶中的对象名称
     * @param duration 时长
     * @param unit     单位
     * @return url
     */
    public String getObject(String object, int duration, TimeUnit unit) {
        return getObject(bucket, object, duration, unit);
    }

    /**
     * 删除文件
     * 使用默认的bucket
     *
     * @param object 存储桶中的对象名称
     */
    public void deleteObject(String object) {
        deleteObject(bucket, object);
    }

    /**
     * 删除文件
     *
     * @param bucket 存储桶名称
     * @param object 存储桶中的对象名称
     */
    public void deleteObject(String bucket, String object) {
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
     * @param bucket        存储桶名称
     * @param multipartFile 文件
     * @return ObjectWriteResponse
     */
    public ObjectWriteResponse putObject(String bucket, MultipartFile multipartFile) {
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
     * 使用默认的bucket
     *
     * @param multipartFile 文件
     * @return ObjectWriteResponse
     */
    public ObjectWriteResponse putObject(MultipartFile multipartFile) {
        return putObject(bucket, multipartFile);
    }

    /**
     * 上传文件
     *
     * @param bucket         存储桶名称
     * @param multipartFiles 文件
     * @return List
     */
    public List<ObjectWriteResponse> putObject(String bucket, MultipartFile... multipartFiles) {
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

    /**
     * 上传文件
     * 使用默认的bucket
     *
     * @param multipartFiles 文件
     * @return List
     */
    public List<ObjectWriteResponse> putObject(MultipartFile... multipartFiles) {
        return putObject(bucket, multipartFiles);
    }

    /**
     * 上传文件
     * bucket 和 object 自理
     * contentType 默认 application/octet-stream
     *
     * @param bucket 存储桶名称
     * @param object 存储桶中的对象名称
     * @param stream 文件输入流
     * @return
     */
    public ObjectWriteResponse putObject(String bucket, String object, InputStream stream) {
        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .stream(stream, -1, ObjectWriteArgs.MIN_MULTIPART_SIZE)
                    .object(object)
                    .contentType("application/octet-stream")
                    .bucket(bucket)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
