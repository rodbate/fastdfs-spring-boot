package com.rodbate.fastdfs.api;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rodbate.fastdfs.client.FastdfsClient;
import com.rodbate.fastdfs.client.StorageServer;
import com.rodbate.fastdfs.client.common.FastdfsConstants;
import com.rodbate.fastdfs.client.exception.FastdfsNotFoundException;
import com.rodbate.fastdfs.client.vo.FileId;
import com.rodbate.fastdfs.client.vo.FileInfo;
import com.rodbate.fastdfs.client.vo.FileMetadata;
import com.rodbate.fastdfs.client.vo.GroupStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/test")
public class FileTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileTest.class);

    @Autowired
    FastdfsClient client;


    private static final Gson gson = new Gson();



    @RequestMapping(value = "/storage")
    public String uploadFile() throws ExecutionException, InterruptedException {


        CompletableFuture<StorageServer> future = client.getTrackerClient().uploadStorageGet();

        return gson.toJson(future.get());
    }


   @RequestMapping("/groups")
    public String listGroups() {


       try {
           CompletableFuture<List<GroupStat>> future = client.getTrackerClient().listGroups();

           return gson.toJson(future.get());

       } catch (Exception e) {

           return handleException(e);

       }
    }


    @RequestMapping("/storages")
    public String listStorage(@RequestParam(value = "group") String groupName,
                              @RequestParam(value = "storageIp", required = false) String storageIp) {
        try {

            return gson.toJson(client.getTrackerClient().listStorages(groupName, storageIp).get());

        } catch (Exception e) {

            return handleException(e);

        }
    }


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(@RequestParam(value = "groupName", required = false) String groupName,
                         @RequestParam(value = "file") MultipartFile file) {

        try {

            Map<String, String> map = new HashMap<>();
            map.put("realName", file.getOriginalFilename());
            FileMetadata metadata = new FileMetadata(map);


            CompletableFuture<FileId> upload;

            if (groupName == null || groupName.trim().length() == 0) {
                upload = client.upload(file.getInputStream(), file.getOriginalFilename(), file.getSize(), metadata);
            }
            else
            {
                upload = client.upload(groupName, file.getInputStream(), file.getOriginalFilename(), file.getSize(), metadata);
            }


            return "group : " + upload.get().group() + "   path:" + upload.get().path();

        } catch (Exception e) {

            return handleException(e);

        }
    }



    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public String download(@RequestParam(value = "group") String group,
                         @RequestParam(value = "filename") String filename,
                         HttpServletResponse response) {



        try {

            CompletableFuture<FileMetadata> metaFuture = client.metadataGet(group + "/" + filename);
            FileMetadata metadata = metaFuture.get();

            String file = metadata.values().get("realName");

            System.out.println("========== file " + file);

            file = file == null ? filename : file;

            response.setHeader("Content-Disposition", "attachment; filename=" + URLDecoder.decode(file, "utf-8"));
            response.setContentType("application/octet-stream");

            ServletOutputStream outputStream = response.getOutputStream();


            CompletableFuture<Long> download = client.download(group + "/" + filename, outputStream);


            System.out.println(" ======================= " + download.get());

            response.setContentLengthLong(download.get());
            outputStream.flush();

            return "success";

        } catch (Exception e) {

            return handleException(e);

        }

    }


    @RequestMapping(value = "/file/info", method = RequestMethod.POST)
    public String getFileInfo(@RequestParam(value = "group") String group,
                              @RequestParam(value = "filename") String filename) {


        try {

            CompletableFuture<FileInfo> fileFuture = client.infoGet(new FileId(group, filename));

            return gson.toJson(fileFuture.get());

        } catch (Exception e) {
            return handleException(e);

        }
    }


    @RequestMapping(value = "/meta/set", method = RequestMethod.POST)
    public String setFileMeta(@RequestParam(value = "group") String group,
                              @RequestParam(value = "filename") String filename,
                              @RequestParam(value = "meta") String meta) {


        try {

            Map<String, String> map =
                    gson.fromJson(meta, new TypeToken<Map<String, String>>() {}.getType());

            FileMetadata metadata = new FileMetadata(map);

            CompletableFuture<Void> future = client.metadataSet(new FileId(group, filename), metadata, FastdfsConstants.METADATA_MERGE);

            future.get();

            return "success";

        } catch (Exception e) {
            return handleException(e);

        }
    }

    @RequestMapping(value = "/meta/get", method = RequestMethod.POST)
    public String getFileMeta(@RequestParam(value = "group") String group,
                              @RequestParam(value = "filename") String filename) {


        try {

            CompletableFuture<FileMetadata> future = client.metadataGet(new FileId(group, filename));

            FileMetadata metadata = future.get();

            return gson.toJson(metadata);

        } catch (Exception e) {
            return handleException(e);
        }
    }


    @RequestMapping(value = "/file/delete", method = RequestMethod.POST)
    public String deleteFile(@RequestParam(value = "group") String group,
                             @RequestParam(value = "filename") String filename) {

        try {

            CompletableFuture<Void> delete = client.delete(new FileId(group, filename));
            delete.get();

        } catch (Exception e) {

            return handleException(e);

        }
        return "success";
    }


    @RequestMapping(value = "/append/new", method = RequestMethod.POST)
    public String uploadAppendFile(@RequestParam(value = "groupName", required = false) String groupName,
                                   @RequestParam(value = "file") MultipartFile file) {


        try {

            Map<String, String> map = new HashMap<>();
            map.put("realName", file.getOriginalFilename());
            FileMetadata metadata = new FileMetadata(map);

            CompletableFuture<FileId> upload;
            if (groupName == null || groupName.trim().length() == 0)
            {
                upload = client.uploadAppender(file.getInputStream(), file.getOriginalFilename(), file.getSize(), metadata);
            }
            else
            {
                upload = client.uploadAppender(groupName, file.getInputStream(), file.getOriginalFilename(), file.getSize(), metadata);
            }

            FileId fileId = upload.get();

            return "group : " + fileId.group() + "   path: " + fileId.path();

        } catch (Exception e) {

            return handleException(e);

        }

    }


    @RequestMapping(value = "/append/add", method = RequestMethod.POST)
    public String appendFile(@RequestParam(value = "groupName") String group,
                             @RequestParam(value = "filename") String filename,
                             @RequestParam(value = "file") MultipartFile file) {


        try {

            CompletableFuture<Void> append = client.append(new FileId(group, filename), file.getInputStream(), file.getSize());

            append.get();

        } catch (Exception e) {

            return handleException(e);

        }

        return "success";
    }


    @RequestMapping(value = "/append/modify", method = RequestMethod.POST)
    public String modify(@RequestParam(value = "groupName") String group,
                         @RequestParam(value = "filename") String filename,
                         @RequestParam(value = "file") MultipartFile file,
                         @RequestParam(value = "offset") long offset) {

        try {

            CompletableFuture<Void> modify = client.modify(new FileId(group, filename), file.getInputStream(), file.getSize(), offset);

            modify.get();

        } catch (Exception e) {

            return handleException(e);

        }

        return "success";
    }


    @RequestMapping(value = "/append/truncate", method = RequestMethod.POST)
    public String truncate(@RequestParam(value = "groupName") String group,
                           @RequestParam(value = "filename") String filename,
                           @RequestParam(value = "offset", required = false) long offset) {



        try {

            CompletableFuture<Void> truncate = client.truncate(new FileId(group, filename), offset);

            truncate.get();

        } catch (Exception e) {
            return handleException(e);
        }

        return "success";
    }





    private String handleException(Exception e) {
        if (e instanceof FastdfsNotFoundException) {
            return "Fast DFS Resource Not Found";
        }

        else if (e instanceof ExecutionException)
        {
            ExecutionException exception = (ExecutionException) e;
            if (exception.getCause() instanceof FastdfsNotFoundException) {
                LOGGER.warn(e.getMessage(), e);
                return "Fast DFS Resource Not Found";
            }
            return "failed";
        }

        else {

            return "failed";
        }
    }


}
