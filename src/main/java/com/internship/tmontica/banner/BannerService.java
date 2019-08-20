package com.internship.tmontica.banner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerDao bannerDao;

    // usePage로 배너 가져오기
    public List<Banner> getBannersByPage(UsePage usePage){
        List<Banner> banners = bannerDao.getBannersByUsePage(usePage.name());
        return banners;
    }
}
