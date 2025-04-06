package com.zw.okai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zw.okai.model.dto.ojquestion.OJQuestionQueryRequest;
import com.zw.okai.model.dto.question.QuestionQueryRequest;
import com.zw.okai.model.entity.OJQuestion;
import com.zw.okai.model.vo.OJQuestionVO;
import com.zw.okai.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 李鱼皮
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-08-07 20:58:00
*/
public interface OJQuestionService extends IService<OJQuestion> {


    /**
     * 校验
     *
     * @param question
     * @param add
     */
    void validQuestion(OJQuestion question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<OJQuestion> getQueryWrapper(OJQuestionQueryRequest questionQueryRequest);
    
    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    OJQuestionVO getQuestionVO(OJQuestion question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<OJQuestionVO> getQuestionVOPage(Page<OJQuestion> questionPage, HttpServletRequest request);
    
}
