package cn.scut.aicodesmell.service.impl;

import cn.scut.aicodesmell.common.OrderEntity;
import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.common.response.Results;
import cn.scut.aicodesmell.core.Processor;
import cn.scut.aicodesmell.mapper.OrderMapper;
import cn.scut.aicodesmell.service.ProcessOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author wanghy
 */
@Service
@Slf4j
public class ProcessOrderServiceImpl implements ProcessOrderService {

    private static String[] ACCEPTABLE_DOC_EXTENSION = new String[]{".doc", ".docx"};
    private static String[] ACCEPTABLE_CODE_EXTENSION = new String[]{".zip", ".rar"};

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private Map<String, Processor> processors;

    @Value("${file-save.upload}")
    private String uploadFilePath;

    @Override
    public Result uploadCodeFile(MultipartFile file, String orderId, Integer userId) {
        Result result = this.checkUploadAvailable(file, orderId, userId, ACCEPTABLE_CODE_EXTENSION);
        if (Objects.nonNull(result)) {
            return result;
        }

        String path;
        try {
            path = this.saveFile(file, orderId);
        } catch (IOException e) {
            log.error("保存文件出错, log: {}", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            return Results.internalError("保存文件失败, 请重试");
        }

        //save成功了, 把path存数据库里 path是文件指定目录下文件的名, 也就是说数据库里存的其实不是完整的path, 只有文件名
        log.info("文件保存至: {}", path);
        orderMapper.updateCodeFilePath(orderId, path);
        return Results.ok("上传成功");
    }

    @Override
    public Result uploadDocFile(MultipartFile file, String orderId, Integer userId) {
        Result result = this.checkUploadAvailable(file, orderId, userId, ACCEPTABLE_DOC_EXTENSION);
        if (Objects.nonNull(result)) {
            return result;
        }

        String path = null;
        try {
            path = this.saveFile(file, orderId);
        } catch (IOException e) {
            log.error("保存文件出错, log: {}", Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            return Results.internalError("保存文件失败, 请重试");
        }

        //save成功了, 把path存数据库里
        log.info("文件保存至: {}", path);
        orderMapper.updateDocFilePath(orderId, path);
        return Results.ok("上传成功");
    }

    @Override
    public Result getOrderStatus(String orderId, Integer userId) {
        OrderEntity orderEntity = orderMapper.getOrderById(orderId);

        //订单不存在
        if (Objects.isNull(orderEntity) || !userId.equals(orderEntity.getUserId())) {
            log.info("查询的订单不存在或不属于用户, orderId: {}, userId: {}", orderId, userId);
            return Results.paramWrong("查询的订单不存在或不属于用户");
        }
        String status = orderMapper.getOrderStatus(orderId);
        return Results.ok(status);
    }

    @Override
    public Result startProcessOrder(String orderId, Integer userId, String algorithm) {
        OrderEntity orderEntity = orderMapper.getOrderById(orderId);

        //订单不存在
        if (Objects.isNull(orderEntity) || !userId.equals(orderEntity.getUserId())) {
            log.info("查询的订单不存在或不属于用户, orderId: {}, userId: {}", orderId, userId);
            return Results.paramWrong("查询的订单不存在或不属于用户");
        }

        if (StringUtils.isEmpty(orderEntity.getCodeUrl()) || StringUtils.isEmpty(orderEntity.getDocUrl())) {
            log.info("查询的订单未上传代码和文档, orderId: {}", orderId);
            return Results.fileNotUploaded();
        }

        //处理任务开始
        Processor processor = processors.get(algorithm);
        processor.generateResult(orderEntity.getDocUrl(), orderEntity.getCodeUrl());
        return Results.ok("已提交任务");
    }

    @Override
    public ResponseEntity<?> downloadFile(String path, String orderId) {
        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.OK).body(Results.paramWrong("文件不存在"));
        }
        String fileOrderId = file.getName().substring(0, file.getName().lastIndexOf('.'));
        if (!orderId.equals(fileOrderId)) {
            return ResponseEntity.status(HttpStatus.OK).body(Results.paramWrong("文件不存在"));
        }

        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(file.getName()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
    }

    private Result checkUploadAvailable(MultipartFile file, String orderId, Integer userId, String[] acceptableFileExtensions) {
        OrderEntity orderEntity = orderMapper.getOrderById(orderId);

        //订单不存在
        if (Objects.isNull(orderEntity) || !userId.equals(orderEntity.getUserId())) {
            log.info("查询的订单不存在或不属于用户, orderId: {}, userId: {}", orderId, userId);
            return Results.paramWrong("查询的订单不存在或不属于用户");
        }

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            return Results.paramWrong("文件名为空");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isEmpty(fileExtension) || !Arrays.stream(acceptableFileExtensions).toList().contains(fileExtension)) {
            return Results.paramWrong("文件后缀为空或不兼容");
        }

        return null;
    }

    private String saveFile(MultipartFile file, String orderId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        //这个调用链里, 其实originalFileName不会是空的
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

        //上传到指定目录
        String cwd = System.getProperty("user.dir");
        String filePath = cwd + File.separator + uploadFilePath + File.separator + orderId + fileExtension;
        //Files.createFile(Path.of(filePath));
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());
        return orderId + fileExtension;
    }
}
