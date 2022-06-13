package com.changgou.file.controller;

import com.changgou.file.jopo.FastDFSFile;
import com.changgou.file.utils.FastDFSClient;
import entity.Result;
import entity.StatusCode;

import org.mockito.internal.util.StringUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@RestController
@CrossOrigin
public class FileController {

    @RequestMapping("upload")
    public Result upload(MultipartFile file){
        try {
            //原来文件名
            String oldName = file.getOriginalFilename();
            //获取后缀名
            String extName = StringUtils.getFilenameExtension(oldName);
            FastDFSFile dfsFile = new FastDFSFile(oldName,file.getBytes() ,extName );

            String[] upload = FastDFSClient.upload(dfsFile);

            String url = FastDFSClient.getTrackerUrl()+ upload[0] + "/" + upload[1];;
            return new Result(true, StatusCode.OK,"文件上传成功",url);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new Result(false, StatusCode.ERROR,"文件上传失败");
    }
}
