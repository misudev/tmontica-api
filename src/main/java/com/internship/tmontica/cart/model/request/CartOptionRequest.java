package com.internship.tmontica.cart.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
public class CartOptionRequest {
    @Min(1)
    private int id;
    @Min(1)
    private int quantity;

    // DB에 들어갈 옵션 문자열로 바꾸기
    public StringBuilder convertOptionStringToDB(StringBuilder optionStr){
        // DB에 들어갈 옵션 문자열 만들기
        if (this.id > 2) {
            optionStr.append("/");
        }
        int optionId = this.id;
        int opQuantity = this.quantity;
        optionStr.append(optionId + "__" + opQuantity);

        return optionStr;
    }
}
