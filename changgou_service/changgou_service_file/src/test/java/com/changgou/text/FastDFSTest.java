package com.changgou.text;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;


public class FastDFSTest {

    @Test
    public void testUpload() throws Exception {
        //1、获取配置文件路径-filePath = new ClassPathResource("fdfs_client.conf").getPath()
        String pash = new ClassPathResource("fdfs_client.conf").getPath();

        //2、加载配置文件-ClientGlobal.init(配置文件路径)
        ClientGlobal.init(pash);

        //3、创建一个TrackerClient对象。直接new一个。
        TrackerClient trackerClient = new TrackerClient();

        //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
        TrackerServer trackerServer = trackerClient.getConnection();

        //5、创建一个StorageClient对象，直接new一个，需要两个参数TrackerServer对象、null
        StorageClient storageClient = new StorageClient(trackerServer,null);

        //上传文件
        String[] uploadFile = storageClient.upload_file("D:/图片/张雨涵-蕾丝内衣/31.jpg","jpg" ,null );

        //输出结果
        for (String s : uploadFile) {
            System.out.println(s);
        }
    }
}
