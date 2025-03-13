package cn.scut.aicodesmell.controller;

import cn.scut.aicodesmell.common.dto.CreateOrderDto;
import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.common.response.Results;
import cn.scut.aicodesmell.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @author wanghy
 * 增删改查的接口
 */
@RestController
@Slf4j
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result createOrder(@RequestBody CreateOrderDto createOrderDto) {
        if (Objects.isNull(createOrderDto.getUserId())) {
            return Results.paramWrong("userId为空");
        }
        return orderService.createOrder(createOrderDto.getUserId());
    }

    @GetMapping("/delete")
    public Result deleteOrder(@RequestParam("orderId") String orderId, @RequestParam("userId") Integer userId) {
        return orderService.deleteOrder(orderId, userId);
    }

    @GetMapping("/get_single")
    public Result getSingleOrder(@RequestParam("orderId") String orderId, @RequestParam("userId") Integer userId) {
        return orderService.getSingleOrder(orderId, userId);
    }

    @GetMapping("/count")
    public Result getCount(@RequestParam("userId") Integer userId) {
        return orderService.getOrderCount(userId);
    }

    @GetMapping("/get_by_page/{curPage}/{pageSize}")
    public Result getOrderByPage(@RequestParam("userId") Integer userId, @RequestParam("total") Integer total, @PathVariable Integer curPage, @PathVariable Integer pageSize) {
        return orderService.getOrderByPage(userId, total, curPage, pageSize);
    }
}
