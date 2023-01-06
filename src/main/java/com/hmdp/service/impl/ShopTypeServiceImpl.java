package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getList() {

        // 1.从redis中查询缓存
        List<String> shopTypeList;
        shopTypeList = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);

        // 2.判断是否存在
        if(CollectionUtils.isNotEmpty(shopTypeList)){
            List<ShopType> shopTypes = new ArrayList<>();
            // 3.存在返回
            for(String shopType : shopTypeList){
                ShopType shop = JSONUtil.toBean(shopType, ShopType.class);
                shopTypes.add(shop);
            }
            return Result.ok(shopTypes);
        }

        // 4.不存在，查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();

        // 5.不存在，返回错误
        if (CollectionUtils.isEmpty(typeList)){
            return Result.fail("不存在该分类");
        }

        // 6.存在,加入缓存
        for(ShopType shopType:typeList){
            String s = JSONUtil.toJsonStr(shopType);
            shopTypeList.add(s);
        }
        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY,shopTypeList);

        // 7.返回
        return Result.ok(typeList);
    }
}
