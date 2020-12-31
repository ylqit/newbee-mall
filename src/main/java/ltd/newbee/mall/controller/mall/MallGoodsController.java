package ltd.newbee.mall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.base.BaseController;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.entity.Goods;
import ltd.newbee.mall.entity.GoodsCategory;
import ltd.newbee.mall.entity.ShopCat;
import ltd.newbee.mall.entity.vo.MallUserVO;
import ltd.newbee.mall.entity.vo.SearchObjVO;
import ltd.newbee.mall.entity.vo.SearchPageCategoryVO;
import ltd.newbee.mall.entity.vo.SearchPageGoodsVO;
import ltd.newbee.mall.service.GoodsCategoryService;
import ltd.newbee.mall.service.GoodsService;
import ltd.newbee.mall.service.ShopCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@Controller
public class MallGoodsController extends BaseController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @Autowired
    private ShopCatService shopCatService;

    @GetMapping("/search")
    public String searchPage(SearchObjVO searchObjVO, HttpServletRequest request) {
        Page<SearchPageGoodsVO> page = getPage(request, Constants.GOODS_SEARCH_PAGE_LIMIT);
        String keyword = searchObjVO.getKeyword();
        IPage<Goods> iPage = goodsService.findMallGoodsListBySearch(page, searchObjVO);
        request.setAttribute("keyword", keyword);
        request.setAttribute("pageResult", iPage);
        request.setAttribute("orderBy", searchObjVO.getSidx());
        request.setAttribute("order", searchObjVO.getOrder());
        Long goodsCategoryId = searchObjVO.getGoodsCategoryId();
        if (goodsCategoryId != null) {
            Goods goods = new Goods();
            goods.setGoodsCategoryId(goodsCategoryId);
            SearchPageCategoryVO searchPageCategoryVO = new SearchPageCategoryVO();
            GoodsCategory thirdCategory = goodsCategoryService.getById(goodsCategoryId);
            GoodsCategory secondCategory = goodsCategoryService.getById(thirdCategory.getParentId());
            List<GoodsCategory> thirdCateGoryList = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                    .eq("parent_id", thirdCategory.getParentId()));
            searchPageCategoryVO.setCurrentCategoryName(thirdCategory.getCategoryName());
            searchPageCategoryVO.setSecondLevelCategoryName(secondCategory.getCategoryName());
            searchPageCategoryVO.setThirdLevelCategoryList(thirdCateGoryList);
            request.setAttribute("goodsCategoryId", goodsCategoryId);
            request.setAttribute("searchPageCategoryVO", searchPageCategoryVO);
        }
        return "mall/search";
    }


    @GetMapping("/goods/detail/{goodsId}")
    public String detail(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) {
        Goods goods = goodsService.getById(goodsId);
        request.setAttribute("cartItemId", 0);
        request.setAttribute("goodsCount", 0);
        // 查询购物车中是否有该商品
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (userVO != null) {
            ShopCat shopCat = shopCatService.getOne(new QueryWrapper<ShopCat>()
                    .eq("user_id", userVO.getUserId())
                    .eq("goods_id", goodsId));
            request.setAttribute("goodsCount", 0);
            if (Objects.nonNull(shopCat)) {
                request.setAttribute("cartItemId", shopCat.getCartItemId());
                request.setAttribute("goodsCount", shopCat.getGoodsCount());
            }
        }
        request.setAttribute("goodsDetail", goods);
        return "mall/detail";
    }
}
