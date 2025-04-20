package cn.scut.aicodesmell.controller;

import cn.scut.aicodesmell.common.ProcessTaskAlgorithmEnum;
import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.common.response.Results;
import cn.scut.aicodesmell.service.ProcessOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * @author wanghy
 * 处理订单任务的接口
 */
@RestController
@Slf4j
@RequestMapping("/api/process")
public class ProcessOrderController {

    @Autowired
    private ProcessOrderService processOrderService;

    @Value("${file-save.upload}")
    private String uploadFilePath;

    @Value("${file-save.download}")
    private String downloadFilePath;

    @PostMapping("/upload/doc/{userId}/{orderId}")
    public Result upLoadDocFile(@RequestBody MultipartFile file, @PathVariable String orderId, @PathVariable Integer userId) {
        if (Objects.isNull(file) || file.isEmpty()) {
            log.info("用户上传了空的文件");
            return Results.fileWrong();
        }
        return processOrderService.uploadDocFile(file, orderId, userId);
    }

    @PostMapping("/upload/code/{userId}/{orderId}")
    public Result upLoadCodeFile(@RequestBody MultipartFile file, @PathVariable String orderId, @PathVariable Integer userId) {
        if (Objects.isNull(file) || file.isEmpty()) {
            return Results.fileWrong();
        }
        return processOrderService.uploadCodeFile(file, orderId, userId);
    }

    @GetMapping("/status")
    public Result getOrderStatus(@RequestParam("orderId") String orderId, @RequestParam("userId") Integer userId) {
        return processOrderService.getOrderStatus(orderId, userId);
    }

    @GetMapping("/start/{algorithm}")
    public Result startOrder(@RequestParam("orderId") String orderId, @RequestParam("userId") Integer userId,
                             @RequestParam("mainPackage") String mainPackage, @PathVariable String algorithm) {
        if (!ProcessTaskAlgorithmEnum.contains(algorithm)) {
            return Results.paramWrong("指定了不存在的算法");
        }
        return processOrderService.startProcessOrder(orderId, userId, algorithm, mainPackage);
    }

    @GetMapping("/download/result")
    public ResponseEntity<?> downloadResult(@RequestParam("orderId") String orderId, @RequestParam("fileName") String fileName) {
        String downloadPath = System.getProperty("user.dir") + "/" + downloadFilePath;
        return processOrderService.downloadFile(downloadPath + fileName, orderId);
    }

    @GetMapping("/download/source")
    public ResponseEntity<?> downloadSource(@RequestParam("orderId") String orderId, @RequestParam("fileName") String fileName) {
        String uploadPath = System.getProperty("user.dir") + "/" + uploadFilePath;
        return processOrderService.downloadFile(uploadPath + fileName, orderId);
    }
}
