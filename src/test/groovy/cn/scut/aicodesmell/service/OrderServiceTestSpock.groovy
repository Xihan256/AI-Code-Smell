package cn.scut.aicodesmell.service

import cn.scut.aicodesmell.common.OrderEntity
import cn.scut.aicodesmell.common.response.Result
import cn.scut.aicodesmell.mapper.OrderMapper
import cn.scut.aicodesmell.service.impl.OrderServiceImpl
import spock.lang.Specification

class OrderServiceTestSpock extends Specification {

    def orderMapper = Mock(OrderMapper)

    def orderService = new OrderServiceImpl(orderMapper: orderMapper)

    def "createOrder"() {
        given:
        orderMapper.insertOrder(*_) >> rowInserted

        when:
        Result result = orderService.createOrder(1)

        then:
        code == result.code

        where:
        rowInserted | code
        1           | 200
        0           | 50001
    }

    def "deleteOrder"() {
        given:
        orderMapper.getOrderById(*_) >> theOrder
        orderMapper.deleteOrder(*_) >> {}

        when:
        Result result = orderService.deleteOrder("1", 1)

        then:
        code == result.code

        where:
        theOrder                     | code
        null                         | 40001
        new OrderEntity(userId: 234) | 40001
        new OrderEntity(userId: 1)   | 200
    }

    def "getSingleOrder"() {
        given:
        orderMapper.getOrderById(*_) >> theOrder

        when:
        Result result = orderService.getSingleOrder("1", 1)

        then:
        code == result.code

        where:
        theOrder                     | code
        null                         | 40001
        new OrderEntity(userId: 234) | 40001
        new OrderEntity(userId: 1)   | 200
    }

    def "getOrderByPage"() {
        given:
        orderMapper.batchGetOrderByPage(*_) >> orderList

        when:
        Result result = orderService.getOrderByPage(1, 100, 1, 5)

        then:
        code == result.code

        where:
        orderList                    | code
        null                         | 200
        [new OrderEntity(userId: 1)] | 200
    }

    def "getOrderCount"() {
        given:
        orderMapper.getUserOrderCount(*_) >> 1

        when:
        Result result = orderService.getOrderCount(1)

        then:
        200 == result.code

    }
}
