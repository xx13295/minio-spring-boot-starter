# minio-spring-boot-starter

# 使用方法

### 依赖

```

<dependency>
    <groupId>plus.ojbk</groupId>
    <artifactId>minio-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>

```

### 配置

```

file:
  minio:
    enable: true
    endpoint: http://172.16.1.240:9000
    bucket: test
    access-key: test
    secret-key: test123456789

```








老鸟止步.












# Minio整合Springboot保姆级使用教程 （新手观看）


### POM 文件加入 依赖

```

<dependency>
    <groupId>plus.ojbk</groupId>
    <artifactId>minio-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- 测试用例页面使用 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<dependency>
     <groupId>org.projectlombok</groupId>
     <artifactId>lombok</artifactId>
     <optional>true</optional>
</dependency>
```

### yml 配置


```

file:
  minio:
#    enable: true
    endpoint: http://172.16.1.240:9000
    bucket: test
    access-key: test
    secret-key: test123456789



spring:
  servlet:
    multipart:
      #指定上传文件允许的最大大小。 默认为 1MB
      max-file-size: 100MB
      #指定多部分/表单数据请求允许的最大大小。 默认值为 10MB。
      max-request-size: 100MB

```

#### 编写一个controller

```

import io.minio.ObjectWriteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import plus.ojbk.minio.core.MinioTemplate;

/**
 * @author wxm
 * @version 1.0
 * @since 2021/6/7 10:03
 */
@Slf4j
@RestController
public class TestController {
    @Autowired
    private MinioTemplate minioTemplate;

    @GetMapping("/index")
    public Object index() {
        ModelAndView modelAndView = new ModelAndView("index");
        return modelAndView;
    }

    @PostMapping("/upload")
    public Object upload(@RequestParam("file") MultipartFile multipartFile) {
        ObjectWriteResponse res = minioTemplate.putObject(multipartFile);
        String path = res.object();
        // String bucket = res.bucket();
        log.info("File Path = {}", path);
        // TODO 你具体的业务 比如存储 这个path 到数据库等
        return "ok";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("path") String object) {
        minioTemplate.deleteObject(object);
        return "ok";
    }


    @GetMapping("/get")
    public String get(@RequestParam("path") String object) {
        //默认 1 小时
        String url = minioTemplate.getObject(object);
        //自定义时间
        //String url = minioTemplate.getObject(name, 10, TimeUnit.MINUTES);
        log.info("Get File Url = {}", url);
        return url;
    }

}


```


#### 编写一个 测试文件上传的页面index.html



```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>我是主页</title>
</head>
<body>
<p>测试上传</p>
<form action="/upload" method="post" enctype="multipart/form-data">
    <input type="file" name="file">
    <input type="submit" value="上传">
</form>


</body>
</html>


```

# 关于minio 服务的安装 

中文文档
https://docs.min.io/cn/minio-quickstart-guide.html 

### docker 安装


```

docker run -itd -p 9000:9000 --name minio \
-e "MINIO_ACCESS_KEY=test" \
-e "MINIO_SECRET_KEY=test123456789" \
-v /disk1/dockerContainer/minio/data:/data \
-v /disk1/dockerContainer/minio/config:/root/.minio \
minio/minio server /data

```

解释：

-e为环境变量参数

MINIO_ACCESS_KEY 和 MINIO_SECRET_KEY 后面的值可根据自己喜好设置

-v 为挂载盘

/disk1/dockerContainer/minio/data:/data

意思为  docker 内的/data 目录映射 你本地linux 的 /disk1/dockerContainer/minio/data 



