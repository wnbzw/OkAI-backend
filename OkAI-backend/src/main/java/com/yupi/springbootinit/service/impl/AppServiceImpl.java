package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.App;
import com.yupi.springbootinit.service.AppService;
import com.yupi.springbootinit.mapper.AppMapper;
import org.springframework.stereotype.Service;

/**
* @author 16247
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-01-08 14:20:49
*/
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService{

}



