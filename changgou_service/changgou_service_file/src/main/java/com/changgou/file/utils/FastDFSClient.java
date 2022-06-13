package com.changgou.file.utils;

import com.changgou.file.jopo.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class FastDFSClient {

    //类加载执行一次
    static {
        try {
            //1、获取配置文件路径-filePath = new ClassPathResource("fdfs_client.conf").getPath()
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            //2、加载配置文件-ClientGlobal.init(配置文件路径)
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取TrackerServer
     * @return
     */
    public static TrackerServer getTrackerServer(){
        TrackerServer trackerServer = null;
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackerServer;
    }

    /**
     * 获取StorageClient
     * @return
     */
    public static StorageClient getStorageClient(){
        TrackerServer trackerServer = getTrackerServer();
        return new StorageClient(trackerServer,null);
    }

    /**
     * SpringMVC-文件上传
     * @param fastDFSFile 上传包装参数
     * @return
     */
    public static String[] upload(FastDFSFile fastDFSFile){
        String[] uploadFile = null;
        try {
            NameValuePair[] meta_list = new NameValuePair[1];
            //给文件追加一个扩展属性-作者
            meta_list[0] = new NameValuePair("author",fastDFSFile.getAuthor());
            //上传文件到FastDFS
            uploadFile = getStorageClient().upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadFile;
    }

    /**
     * 获取文件信息
     * @param group_name 组名
     * @param remote_filename FileID
     * @return 文件信息对象
     */
    public static FileInfo getFileInfo(String group_name, String remote_filename){
        FileInfo info = null;
        try {
            info = getStorageClient().get_file_info(group_name, remote_filename);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 文件下载
     * @param group_name  组名
     * @param remote_filename 文件全路径
     * @return InputStream
     */
    public static InputStream downloadFile(String group_name, String remote_filename){
        try {
            byte[] bytes = getStorageClient().download_file(group_name, remote_filename);
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件删除
     * @param group_name  组名
     * @param remote_filename 文件全路径
     */
    public static void deleteFile(String group_name, String remote_filename){
        try {
            //返回0代表删除成功
            int count = getStorageClient().delete_file(group_name, remote_filename);
            //System.out.println("删除了文件的个数为：" + count);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取StorageServer信息
     * @param group_name
     * @return
     */
    public static StorageServer getStorageServer(String group_name){
        StorageServer storeStorage = null;
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();
            storeStorage = trackerClient.getStoreStorage(trackerServer, group_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return storeStorage;
    }

    /**
     * 获取整个组的服务器信息
     * @param group_name
     * @return
     */
    public static ServerInfo[] getServerInfo(String group_name, String remote_filename){
        ServerInfo[] infos = null;
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();
            infos = trackerClient.getFetchStorages(trackerServer, group_name, remote_filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return infos;
    }

    /**
     * 获取Tracker地址与http端口
     * @return
     */
    public static String getTrackerUrl(){
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();
            //"http://192.168.211.132:8080/"
            String url = "http://" + trackerServer.getInetSocketAddress().getHostString()
                    + ":" + ClientGlobal.getG_tracker_http_port() + "/";
            return url;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        //测试获取文件信息
        /*FileInfo fileInfo = getFileInfo("group1", "M00/00/00/wKjThF3wXX6AIUOsAA832942OCg949.jpg");
        System.out.println(fileInfo);*/

        //测试文件下载
        /*try {
            InputStream in = downloadFile("group1", "M00/00/00/wKjThF3wXX6AIUOsAA832942OCg949.jpg");
            //把文件输出在本地
            OutputStream out = new FileOutputStream("D:/1.jpg");
            //缓冲区
            byte[] buff = new byte[1024];
            while (in.read(buff) > -1){
                out.write(buff);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //测试删除文件
        //deleteFile("group1", "M00/00/00/wKjThF3wXX6AIUOsAA832942OCg949.jpg");


        //获取StorageServer信息
        StorageServer storageServer = getStorageServer("group1");
        System.out.println("当前服务器下标：" + storageServer.getStorePathIndex());
        System.out.println("服务器信息：" + storageServer.getInetSocketAddress());

        //获取整个组的服务器信息
        ServerInfo[] infos = getServerInfo("group1", "M00/00/00/wKjThF3wTxSAdIExAAcHN3pW-Rw434.jpg");
        for (ServerInfo info : infos) {
            System.out.println(info.getIpAddr() + ":" + info.getPort());
        }
    }
}
