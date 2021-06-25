package com.github.minio;

import io.minio.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * @author 杨中肖
 */
public class MinioBatchUpload {
    private static String url;
    private static String user;
    private static String pwd;
    private static String bucket;
    private static String minioPath;
    private static String localPath;
    private static MinioClient minioClient;

    static {
        Properties properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = MinioBatchUpload.class.getClassLoader().getResourceAsStream("minio.properties");
        // 使用properties对象加载输入流
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取key对应的value值
        url = properties.getProperty("url");
        user = properties.getProperty("user");
        pwd = properties.getProperty("pwd");
        bucket = properties.getProperty("bucket");
        minioPath = properties.getProperty("minioPath");
        localPath = properties.getProperty("localPath");
        minioClient = MinioClient.builder().endpoint(url).credentials(user, pwd).build();
    }

    public static void main(String[] args) throws Exception {

        MinioClient minioClient = MinioClient.builder().endpoint(url).credentials(user, pwd).build();
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        File file = new File(localPath);
        String rootName = file.getName();
        if (!file.isDirectory()) {
            upload(file, rootName);
        } else {
            recursion(file, rootName);
        }
    }

    public static void recursion(File file, String rootName) throws Exception {
        File[] files = file.listFiles();
        for (File currFile : files) {
            if (currFile.isDirectory()) {
                recursion(currFile, rootName);
            } else {
                upload(currFile, rootName);
            }
        }
    }


    public static void upload(File file, String rootName) throws Exception {
        String rootPath = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(rootName)).replace("\\", "/");
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(minioPath + rootPath)
                        .filename(file.getAbsolutePath())
                        .build());
    }

}
