package com.internship.tmontica.order.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderRequest {
    @NotEmpty
    @Valid
    private List<OrderMenusRequest> menus;
    private int usedPoint;
    @Min(0)
    private int totalPrice;
    @NotEmpty
    private String payment;

}
