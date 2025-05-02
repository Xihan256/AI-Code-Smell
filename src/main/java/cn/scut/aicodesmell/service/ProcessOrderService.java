package cn.scut.aicodesmell.service;

import cn.scut.aicodesmell.common.response.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wanghy
 */
public interface ProcessOrderService {
    /**
     * 用户上传源代码压缩包
     *
     * @param file    源代码
     * @param orderId 订单id
     * @param userId  用户id
     * @return Result
     */
    Result uploadCodeFile(MultipartFile file, String orderId, Integer userId);

    /**
     * 用户上传文档
     *
     * @param file    文档
     * @param orderId 订单id
     * @param userId  用户id
     * @return Result
     */
    Result uploadDocFile(MultipartFile file, String orderId, Integer userId);

    /**
     * 轮询订单状态
     *
     * @param orderId 订单id
     * @param userId  用户id
     * @return Result
     */
    Result getOrderStatus(String orderId, Integer userId);

    /**
     * 开始处理
     *
     * @param orderId     订单id
     * @param userId      用户id
     * @param algorithm   指定的算法
     * @param mainPackage 要分析的主包
     * @return Result
     */
    Result startProcessOrder(String orderId, Integer userId, String algorithm, String mainPackage);

    /**
     * 下载文件
     *
     * @param path    文件路径
     * @param orderId 订单id
     * @return ResponseEntity
     */
    ResponseEntity<?> downloadFile(String path, String orderId);

    /**
     * 获取订单详情
     *
     * @param orderId 订单id
     * @return ResponseEntity
     */
    Result getOrderDetail(String orderId);

    /**
     * 获取组件出现的句子
     *
     * @param orderId       订单id
     * @param componentName 组件name
     * @return ResponseEntity
     */
    Result getComponentSentences(String orderId, String componentName);

    /**
     * 获取订单代码组件列表
     *
     * @param orderId 订单id
     * @return ResponseEntity
     */
    Result getOrderCodeComponents(String orderId);
}
