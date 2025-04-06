//package com.zw.okai.job.once;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import javax.annotation.Resource;
//
//import com.zw.okai.esdao.AppEsDao;
//import com.zw.okai.model.dto.app.AppEsDTO;
//import com.zw.okai.model.entity.App;
//import com.zw.okai.service.AppService;
//import lombok.extern.slf4j.Slf4j;
//import cn.hutool.core.collection.CollUtil;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
///**
// * 全量同步app到 es
// */
//// todo 取消注释开启任务
////@Component
//@Slf4j
//public class FullSyncAppToEs implements CommandLineRunner {
//
//    @Resource
//    private AppService appService;
//
//    @Resource
//    private AppEsDao appEsDao;
//
//    @Override
//    public void run(String... args) {
//        List<App> appList = appService.list();
//        if (CollUtil.isEmpty(appList)) {
//            return;
//        }
//        List<AppEsDTO> appEsDTOList = appList.stream().map(AppEsDTO::objToDto).collect(Collectors.toList());
//        final int pageSize = 500;
//        int total = appEsDTOList.size();
//        log.info("FullSyncAppToEs start, total {}", total);
//        for (int i = 0; i < total; i += pageSize) {
//            int end = Math.min(i + pageSize, total);
//            log.info("sync from {} to {}", i, end);
//            appEsDao.saveAll(appEsDTOList.subList(i, end));
//        }
//        log.info("FullSyncAppToEs end, total {}", total);
//    }
//}
