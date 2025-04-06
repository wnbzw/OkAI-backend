package com.zw.okai.scoring;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zw.okai.manager.AiManager;
import com.zw.okai.model.dto.question.QuestionAnswerDTO;
import com.zw.okai.model.dto.question.QuestionContentDTO;
import com.zw.okai.model.entity.App;
import com.zw.okai.model.entity.Question;
import com.zw.okai.model.entity.ScoringResult;
import com.zw.okai.model.entity.UserAnswer;
import com.zw.okai.model.vo.QuestionVO;
import com.zw.okai.service.QuestionService;
import com.zw.okai.service.ScoringResultService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI测评类应用评分策略
 *
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AITestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    // 缓存用户答案
    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 缓存5分钟移除
                    .expireAfterAccess(5L, TimeUnit.MINUTES)
                    .build();

    @Resource
    private RedissonClient redissonClient;
    // AI 获取用户答案的锁
    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";



    private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价，包括评价结果（简洁明确）和评价描述（尽量详细，100字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
            "```\n" +
            "{\"resultName\": \"评价结果\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象";

    /**
     * 获取AI测评类应用评分策略的AI用户信息
     * @param app
     * @param questionContentDTOList
     * @param choices
     * @return
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices){
        StringBuilder builder = new StringBuilder();
        builder.append(app.getAppName()).append("\n");
        builder.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for(int i = 0; i < questionContentDTOList.size(); i++){
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(QuestionContentDTO.getValue(questionContentDTOList.get(i).getOptions(), choices.get(i)));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        builder.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return builder.toString();
    }
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        String jsonStr = JSONUtil.toJsonStr(choices);
        String cacheKey = buildCacheKey(appId, jsonStr);
        String answerJson = answerCacheMap.getIfPresent(cacheKey);
        // 命中缓存则直接返回结果
        if (StrUtil.isNotBlank(answerJson)) {
            UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;
        }
        // 1. 获取锁
        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);
        try {
            // 2. 尝试获取锁，最多等待 3 秒，上锁以后 15 秒自动解锁
            boolean res= lock.tryLock(3, 15, TimeUnit.SECONDS);
            if (!res){
               return null;
            }
            // 1. 根据 id 查询到题目和题目结果信息
            Question question = questionService.getOne(
                    Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
            );

            QuestionVO questionVO = QuestionVO.objToVo(question);
            // 获取题目信息
            List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
            //获取给ai的用户答案
            String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
            //调用ai 生成题目答案
            String aiMessage = aiManager.doSyncStableRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage);

            //获取ai返回的json数据
            int start=aiMessage.indexOf("{");
            int end=aiMessage.lastIndexOf("}");
            String json=aiMessage.substring(start,end+1);
            // 4. 构造返回值，填充答案对象的属性
            UserAnswer userAnswer = JSONUtil.toBean(json, UserAnswer.class);
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            //5.缓存AI结果
            answerCacheMap.put(cacheKey, json);
            return userAnswer;
        }finally {
            if(lock!=null && lock.isLocked()){
                if (lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }
    }

    // 构建缓存的 key
    private String buildCacheKey(Long appId, String choicesStr) {
        return DigestUtil.md5Hex(appId + ":" + choicesStr);
    }

}