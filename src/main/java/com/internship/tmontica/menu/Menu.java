package com.internship.tmontica.menu;

import com.internship.tmontica.cart.exception.CartException;
import com.internship.tmontica.cart.exception.CartExceptionType;
import com.internship.tmontica.cart.model.request.CartOptionRequest;
import com.internship.tmontica.order.exception.NotEnoughStockException;
import com.internship.tmontica.order.exception.StockExceptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class  Menu {
    private int id;

    @NotEmpty
    private String nameEng;

    @Min(0)
    private int productPrice;

    @NotEmpty
    private String categoryKo;

    @NotEmpty
    private String categoryEng;

    private boolean monthlyMenu;

    private boolean usable;

    @NotEmpty
    private String imgUrl;

    private String description;

    @Min(0)
    private int sellPrice;

    @Min(0)
    private int discountRate;

    private Date createdDate;

    private Date updatedDate;

    private String creatorId;

    private String updaterId;

    @Min(0)
    private int stock;

    @NotEmpty
    private String nameKo;

    private Date startDate;

    private Date endDate;

    private boolean deleted = false;


    // 카테고리
    public boolean isSameCategory(String categoryEng){
        return this.categoryEng.equals(categoryEng);
    }


    // 재고 확인
    public void checkMenuStock(int quantity){
        boolean result = this.stock - quantity > 0;
        // 재고가 모자랄 경우 exception 예외처리
        if (!result){
            throw new NotEnoughStockException(this.id, StockExceptionType.NOT_ENOUGH_STOCK);
        }
    }


    // 음료의 옵션에 HOT/ICE 선택이 안들어가있을때 익셉션 처리
    public void checkDrinkOption(List<CartOptionRequest> options){
        if(!this.categoryEng.equals(CategoryName.CATEGORY_BREAD.getCategoryEng()) && (options.size()==0 || options.get(0).getId() > 2)){
            throw new CartException(CartExceptionType.DEFAULT_OPTION_NOT_SELECTED);
        }
    }

}
