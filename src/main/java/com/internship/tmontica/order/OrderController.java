package com.internship.tmontica.order;

import com.internship.tmontica.order.exception.OrderExceptionType;
import com.internship.tmontica.order.exception.OrderValidException;
import com.internship.tmontica.order.model.request.OrderRequest;
import com.internship.tmontica.order.model.response.OrderListByUserIdResponse;
import com.internship.tmontica.order.model.response.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /** 주문 받기(결제하기) */
    @PostMapping
    public ResponseEntity<Map<String, Integer>> addOrder(Device device, @RequestBody @Valid OrderRequest orderRequest, BindingResult bindingResult){
        if(bindingResult.hasErrors()) {
            throw new OrderValidException(OrderExceptionType.INVALID_ORDER_ADD_FORM, bindingResult);
        }

        Map<String, Integer> map = orderService.addOrderApi(orderRequest, device);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    /** 주문 취소(사용자) */
    @DeleteMapping("/{orderId}")
    public ResponseEntity deleteOrder(@PathVariable("orderId") int orderId){
        orderService.cancelOrderApi(orderId);
        return new ResponseEntity(HttpStatus.OK);
    }

    /** 주문정보 한개 가져오기(상세내역 포함) */
    @GetMapping("/{orderId:\\d+}")
    public ResponseEntity<OrderResponse> getOrderByOrderId(@PathVariable("orderId")int orderId){
        OrderResponse orderResponse = orderService.getOneOrderApi(orderId);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }


    /** 사용자 한명의 주문 전체 내역 가져오기 */
    @GetMapping
    public ResponseEntity<OrderListByUserIdResponse> getOrderList(@RequestParam(value = "page", required = false) int page,
                                                                  @RequestParam(value = "size", required = false) int size){
        OrderListByUserIdResponse orderListByUserIdResponse = orderService.getOrderListApi(page, size);
        return new ResponseEntity<>(orderListByUserIdResponse, HttpStatus.OK);
    }


}
