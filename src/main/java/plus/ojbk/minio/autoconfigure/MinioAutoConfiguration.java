package plus.ojbk.minio.autoconfigure;

import io.minio.MinioClient;
import plus.ojbk.minio.autoconfigure.properties.MinioProperties;
import plus.ojbk.minio.core.MinioTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wxm
 * @version 1.0
 * @since 2021/6/5 13:39
 */
@Configuration
@EnableConfigurationProperties({MinioProperties.class})
@ConditionalOnProperty(prefix = "file.minio", value = "enable", matchIfMissing = true)
public class MinioAutoConfiguration {

    public MinioAutoConfiguration(){

    }

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(MinioProperties minioProperties){
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }


    @Bean
    public MinioTemplate minioTemplate(MinioProperties minioProperties){
        return new MinioTemplate(minioProperties);
    }


}
