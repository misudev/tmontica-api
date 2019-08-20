package com.internship.tmontica.cart;

import com.internship.tmontica.cart.exception.CartException;
import com.internship.tmontica.cart.exception.CartExceptionType;
import com.internship.tmontica.cart.model.request.CartOptionRequest;
import com.internship.tmontica.cart.model.request.CartRequest;
import com.internship.tmontica.cart.model.request.CartUpdateRequest;
import com.internship.tmontica.cart.model.response.CartIdResponse;
import com.internship.tmontica.cart.model.response.CartMenusResponse;
import com.internship.tmontica.cart.model.response.CartResponse;
import com.internship.tmontica.menu.CategoryName;
import com.internship.tmontica.menu.Menu;
import com.internship.tmontica.menu.MenuDao;
import com.internship.tmontica.option.Option;
import com.internship.tmontica.option.OptionDao;
import com.internship.tmontica.option.OptionType;
import com.internship.tmontica.order.exception.NotEnoughStockException;
import com.internship.tmontica.order.exception.StockExceptionType;
import com.internship.tmontica.security.JwtService;
import com.internship.tmontica.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartMenuService {

    private final CartMenuDao cartMenuDao;
    private final OptionDao optionDao;
    private final MenuDao menuDao;
    private final JwtService jwtService;

    // 카트 정보 가져오기 api
    public CartResponse getCartMenuApi(){
        // 토큰에서 아이디 가져오기
        String userId = getTokenId();

        List<CartMenusResponse> menus = new ArrayList<>(); // 반환할 객체 안의 menus에 들어갈 리스트

        // userId로 카트메뉴 정보 가져오기
        List<CartMenu> cartMenus = cartMenuDao.getCartMenuByUserId(userId);
        int size = 0;
        int totalPrice = 0;
        for (CartMenu cartMenu: cartMenus) {
            // 메뉴아이디로 메뉴정보 가져오기
            Menu menu = menuDao.getMenuById(cartMenu.getMenuId());

            // 삭제된 메뉴일 경우 카트에서 삭제하고 continue!
            if(menu == null){
                cartMenuDao.deleteCartMenu(cartMenu.getId());
                continue;
            }

            // 메뉴 옵션 "1__1/4__2" => "HOT/샷추가(2개)" 로 바꾸는 작업
            String option = "";
            if(!cartMenu.getOption().equals("")){
                option = convertOptionStringToCli(cartMenu.getOption());
            }

            int price = menu.getSellPrice()+cartMenu.getPrice(); // 메뉴가격 + 옵션가격

            // List<CartMenusResponse> 에 넣기
            CartMenusResponse cartMenusResponse = new CartMenusResponse(cartMenu.getId(), cartMenu.getMenuId(), menu.getNameEng(),
                                                                menu.getNameKo(),"/images/".concat(menu.getImgUrl()), option ,
                                                                cartMenu.getQuantity(), price, menu.getStock());
            menus.add(cartMenusResponse);

            // totalPrice 에 가격 누적
            totalPrice += cartMenu.calculateTotalPrice(price);
            // size에 quantity 누적
            size += cartMenu.getQuantity();
        }
        return new CartResponse(size, totalPrice, menus); // 반환할 객체
    }


    // 카트에 추가하기 api
    @Transactional
    public List<CartIdResponse> addCartApi(List<CartRequest> cartRequests){
        List<CartIdResponse> cartIds = new ArrayList<>();
        // 토큰에서 userId 가져오기
        String userId = getTokenId();

        for (CartRequest cartRequest : cartRequests) {
            Menu menu = menuDao.getMenuById(cartRequest.getMenuId());

            // 재고 체크와 예외처리
            menu.checkMenuStock(cartRequest.getQuantity());

            // direct : true 이면 userId 의 카트에서 direct = true 인 것을 삭제하기 (바로구매를 눌러서 장바구니에 담긴 메뉴)
            if (cartRequest.getDirect()) {
                cartMenuDao.deleteDirectCartMenu(userId);
            }

            List<CartOptionRequest> options = cartRequest.getOption();

            // 음료의 옵션에 HOT/ICE 선택이 안들어가있을때 익셉션 처리
            menu.checkDrinkOption(options);

            StringBuilder optionStr = new StringBuilder();
            int optionPrice = 0;
            for (CartOptionRequest option : options) {
                // DB에 들어갈 옵션 문자열 만들기
                option.convertOptionStringToDB(optionStr);

                // 옵션들의 가격 계산
                optionPrice += ((optionDao.getOptionById(option.getId()).getPrice()) * option.getQuantity());
            }

            // 카트 테이블에 추가하기
            CartMenu cartMenu = new CartMenu(cartRequest.getQuantity(), optionStr.toString(), userId,
                    optionPrice, cartRequest.getMenuId(), cartRequest.getDirect());
            cartMenuDao.addCartMenu(cartMenu);
            int cartId = cartMenu.getId();

            cartIds.add(new CartIdResponse(cartId));

        }//List forEach end

        return cartIds;
    }

    // 카트 수정하기 api
    public int updateCartApi(int id, CartUpdateRequest cartUpdateRequest){
        // 토큰의 아이디와 카트 테이블의 userId 비교
        String userId = getTokenId();
        String cartUserId = cartMenuDao.getCartMenuByCartId(id).getUserId();
        if(!userId.equals(cartUserId)){
            // 아이디 일치하지 않을 경우
            throw new CartException(CartExceptionType.FORBIDDEN_ACCESS_CART_DATA);
        }
        // 재고 체크하기
        int menuId = cartMenuDao.getCartMenuByCartId(id).getMenuId();
        int stock = menuDao.getMenuById(menuId).getStock(); // 현재 메뉴의 재고량
        int quantity = cartUpdateRequest.getQuantity();
        // 재고가 모자랄 경우
        if(stock - quantity < 0){
            throw new NotEnoughStockException(id, StockExceptionType.NOT_ENOUGH_STOCK);
        }

        int result = cartMenuDao.updateCartMenuQuantity(id, cartUpdateRequest.getQuantity());
        return result;
    }

    // 카트 삭제하기 api
    public int deleteCartApi(int id){
        // 토큰의 아이디와 카트 테이블의 userId 비교
        String userId = getTokenId();
        String cartUserId = cartMenuDao.getCartMenuByCartId(id).getUserId();
        if(!userId.equals(cartUserId)){
            // 아이디 일치하지 않을 경우
            throw new CartException(CartExceptionType.FORBIDDEN_ACCESS_CART_DATA);
        }
        //카트에 담긴 정보 삭제하기
        return cartMenuDao.deleteCartMenu(id);
    }


    // DB의 옵션 문자열을 변환
    public String convertOptionStringToCli(String option){
        //메뉴 옵션 "1__1/4__2" => "HOT/샷추가(2개)" 로 바꾸는 작업
        StringBuilder convert = new StringBuilder();
        String[] arrOption = option.split("/");
        for (String opStr : arrOption) {
            String[] oneOption = opStr.split("__");
            Option tmpOption = optionDao.getOptionById(Integer.parseInt(oneOption[0]));

            // 각각의 enum값에 맞는 메서드를 구현?
            OptionType optionType = OptionType.valueOf(tmpOption.getType());
            optionType.attachString(convert, oneOption[1], tmpOption);

        }
        return convert.toString();
    }


    // 토큰에서 아이디 정보를 꺼내는 함수
    public String getTokenId(){
        return JsonUtil.getJsonElementValue(jwtService.getUserInfo("userInfo"), "id");
    }

}
