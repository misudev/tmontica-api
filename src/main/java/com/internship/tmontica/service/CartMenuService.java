package com.internship.tmontica.service;

import com.internship.tmontica.dto.CartMenu;
import com.internship.tmontica.dto.OrderDetail;
import com.internship.tmontica.repository.CartMenuDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartMenuService {

    private final CartMenuDao cartMenuDao;

    // 카트 아이디로 cart_menus 테이블에서 조회하기
    @Transactional(readOnly = true)
    public CartMenu getCartMenuByCartId(int cartId){ return cartMenuDao.getCartMenuByCartId(cartId); }

    // 카트 와 메뉴 테이블 로부터 orderDetails 테이블에 추가할 정보 가져오기
//    @Transactional(readOnly = true)
//    public OrderDetail getCartMenuForOrderDetail(int cartId){ return cartMenuDao.getCartMenuForOrderDetail(cartId); }

    // 카트에서 삭제하기
    @Transactional
    public void deleteCartMenu(int cartId){ cartMenuDao.deleteCartMenu(cartId); }

    // 카트에 추가하기
    @Transactional
    public int addCartMenu(CartMenu cartMenu){ return cartMenuDao.addCartMenu(cartMenu); }

    // direct = true 인 카트메뉴 삭제하기
    @Transactional
    public void deleteDirectCartMenu(){
        cartMenuDao.deleteDirectCartMenu();
    }

    // userId로 cart_menus 조회하기
    @Transactional(readOnly = true)
    public List<CartMenu> getCartMenuByUserId(String userId){
       return cartMenuDao.getCartMenuByUserId(userId);
    }
}
