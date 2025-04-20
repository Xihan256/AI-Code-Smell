package cn.scut.aicodesmell.threadPool;

import cn.scut.aicodesmell.common.OrderEntity;
import cn.scut.aicodesmell.mapper.OrderDetailMapper;
import cn.scut.aicodesmell.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wanghy
 */
@Configuration
@Slf4j
@EnableScheduling
public class DeleteOrderTask {

    //删多少天前的订单
    @Value("${task.delete-order-days-diff}")
    private Integer differentDays;

    @Autowired
    private OrderMapper orderMapper;

    private OrderDetailMapper orderDetailMapper;

    @Value("${file-save.upload}")
    private String uploadFilePath;

    @Value("${file-save.download}")
    private String downloadFilePath;

    /**
     * 每天删过期的订单和文件, 0点执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanExpiredOrders() {
        //查出过期了订单的id
        List<OrderEntity> expiredOrders = orderMapper.getExpiredOrders(differentDays);

        //删文件
        String cwd = System.getProperty("user.dir");
        String downloadPath = cwd + File.separator + downloadFilePath + File.separator;
        String uploadPath = cwd + File.separator + uploadFilePath + File.separator;
        // 检查并删除指定文件
        Path downloadDirectory = Paths.get(downloadPath);
        Path uploadDirectory = Paths.get(uploadPath);

        try {
            Files.walkFileTree(downloadDirectory, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new ExpiredFileVisitor(
                            expiredOrders.stream()
                                    .map(OrderEntity::getResultUrl)
                                    .filter(Objects::nonNull).collect(Collectors.toList())));

            Files.walkFileTree(uploadDirectory, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new ExpiredFileVisitor(
                            expiredOrders.stream().flatMap(o -> Stream.of(o.getCodeUrl(), o.getDocUrl()))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //批量删数据库
        List<String> toDeleteIds = orderMapper.batchGetDeleteExpiredOrderIds(differentDays);
        orderMapper.batchDeleteExpiredOrder(differentDays);
        //也删除detail表,数据一致性
        orderDetailMapper.deleteByIds(toDeleteIds);
    }

    private static class ExpiredFileVisitor extends SimpleFileVisitor<Path> {
        public List<String> expireFileNames;

        public ExpiredFileVisitor(List<String> expireFileNames) {
            super();
            this.expireFileNames = expireFileNames;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            //删了名字匹配的文件
            for (String fileName : expireFileNames) {
                if (fileName.equals(file.getFileName().toString())) {
                    Files.delete(file);
                }
            }

            return FileVisitResult.CONTINUE;
        }
    }

}
