package com.ricemarch;


import com.ricemarch.Dto.MinioMultUploadDto;
import com.ricemarch.Dto.MinioUploadDto;
import com.ricemarch.common.api.CommonResult;
import io.minio.MinioClient;
import io.minio.policy.PolicyType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@Api(tags = "MinioController", description = "MinIO对象存储管理")
@Controller
@RequestMapping("/minio")
public class MinioController {

    @Autowired
    UploadService uploadService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioController.class);
    @Value("${minio.endpoint}")
    private String ENDPOINT;
    @Value("${minio.bucketName}")
    private String BUCKET_NAME;
    @Value("${minio.accessKey}")
    private String ACCESS_KEY;
    @Value("${minio.secretKey}")
    private String SECRET_KEY;

    @ApiOperation("文件上传")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult upload(@RequestParam("file") MultipartFile file) {
        try {
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESS_KEY, SECRET_KEY);
            boolean isExist = minioClient.bucketExists(BUCKET_NAME);
            if (isExist) {
                LOGGER.info("存储桶已经存在！");
            } else {
                //创建存储桶并设置只读权限
                minioClient.makeBucket(BUCKET_NAME);
                minioClient.setBucketPolicy(BUCKET_NAME, "*.*", PolicyType.READ_ONLY);
            }
            String filename = UUID.randomUUID() + file.getOriginalFilename();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            // 设置存储对象名称
            String objectName = sdf.format(new Date()) + "/" + filename;
            // 使用putObject上传一个文件到存储桶中
            minioClient.putObject(BUCKET_NAME, objectName, file.getInputStream(), file.getContentType());
            LOGGER.info("文件上传成功!");
            MinioUploadDto minioUploadDto = new MinioUploadDto();
            minioUploadDto.setName(filename);
            minioUploadDto.setUrl(ENDPOINT + "/" + BUCKET_NAME + "/" + objectName);
            return CommonResult.success(minioUploadDto);
        } catch (Exception e) {
            LOGGER.info("上传发生错误: {}！", e.getMessage());
        }
        return CommonResult.failed();
    }


    @ApiOperation("多个文件上传")
    @RequestMapping(value = "/uploads", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult uploads(@RequestParam("files") ArrayList<MultipartFile> files) {
        try {
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESS_KEY, SECRET_KEY);
            boolean isExist = minioClient.bucketExists(BUCKET_NAME);
            if (isExist) {
                LOGGER.info("存储桶已经存在！");
            } else {
                //创建存储桶并设置只读权限
                minioClient.makeBucket(BUCKET_NAME);
                minioClient.setBucketPolicy(BUCKET_NAME, "*.*", PolicyType.READ_ONLY);
            }
            MinioMultUploadDto minioMultUploadDto = new MinioMultUploadDto();
            ArrayList<String> filenames = new ArrayList<>();
            ArrayList<String> urls = new ArrayList<>();
            for (MultipartFile file : files
            ) {
                String filename = UUID.randomUUID() + file.getOriginalFilename();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                // 设置存储对象名称
                String objectName = sdf.format(new Date()) + "/" + filename;
                // 使用putObject上传一个文件到存储桶中
                minioClient.putObject(BUCKET_NAME, objectName, file.getInputStream(), file.getContentType());
                LOGGER.info("文件上传成功!");
                filenames.add(filename);
                urls.add(ENDPOINT + "/" + BUCKET_NAME + "/" + objectName);

            }
            minioMultUploadDto.setNames(filenames);
            minioMultUploadDto.setUrls(urls);
            return CommonResult.success(minioMultUploadDto);

        } catch (Exception e) {
            LOGGER.info("上传发生错误: {}！", e.getMessage());
        }
        return CommonResult.failed();
    }


    @ApiOperation("多个文件+信息上传")
    @RequestMapping(value = "/uploadsInfo", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult upload_info(@RequestParam("files") ArrayList<MultipartFile> files,
                                    @RequestParam String name,
                                    @RequestParam String title,
                                    @RequestParam String content,
                                    @RequestParam String width,
                                    @RequestParam String height
    ) {
        try {

            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESS_KEY, SECRET_KEY);
            boolean isExist = minioClient.bucketExists(BUCKET_NAME);
            if (isExist) {
                LOGGER.info("存储桶已经存在！");
            } else {
                //创建存储桶并设置只读权限
                minioClient.makeBucket(BUCKET_NAME);
                minioClient.setBucketPolicy(BUCKET_NAME, "*.*", PolicyType.READ_ONLY);
            }
            MinioMultUploadDto minioMultUploadDto = new MinioMultUploadDto();
            ArrayList<String> filenames = new ArrayList<>();
            ArrayList<String> urls = new ArrayList<>();
            for (MultipartFile file : files
            ) {
//                UUID.randomUUID().toString().replace("-", "").toLowerCase()
                String filename = height + "_" + width + "_" + file.getOriginalFilename();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                // 设置存储对象名称
                String objectName = sdf.format(new Date()) + "/" + filename;
                // 使用putObject上传一个文件到存储桶中
                minioClient.putObject(BUCKET_NAME, objectName, file.getInputStream(), file.getContentType());
                LOGGER.info("文件上传成功! user:" + name + " filename:" + filename);
                LOGGER.info("width:" + width + " height:" + height);
                filenames.add(filename);
                urls.add(ENDPOINT + "/" + BUCKET_NAME + "/" + objectName);


            }
            uploadService.postMoment(title, content, name, urls);
            minioMultUploadDto.setNames(filenames);
            minioMultUploadDto.setUrls(urls);
            return CommonResult.success(minioMultUploadDto);

        } catch (Exception e) {
            LOGGER.info("上传发生错误: {}！", e.getMessage());
        }
        return CommonResult.failed();
    }


    @ApiOperation("文件删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@RequestParam("objectName") String objectName) {
        try {
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESS_KEY, SECRET_KEY);
            minioClient.removeObject(BUCKET_NAME, objectName);
            return CommonResult.success(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CommonResult.failed();
    }
}
