-- 插入MBTI 16种人格类型的评分结果
INSERT INTO scoring_result (resultName, resultDesc, resultPicture, resultProp, resultScoreRange, appId, userId)
VALUES ('INTJ', '内向、直觉、思考、判断型', 'https://example.com/intj.jpg', '[I,N,T,J]', 90, 1879160617705476097, 1878092630856675329),
       ('INTP', '内向、直觉、思考、感知型', 'https://example.com/intp.jpg', '[I,N,T,P]', 85, 1879160617705476097, 1878092630856675329),
       ('ENTJ', '外向、直觉、思考、判断型', 'https://example.com/entj.jpg', '[E,N,T,J]', 80, 1879160617705476097, 1878092630856675329),
       ('ENTP', '外向、直觉、思考、感知型', 'https://example.com/entp.jpg', '[E,N,T,P]', 75, 1879160617705476097, 1878092630856675329),
       ('INFJ', '内向、直觉、情感、判断型', 'https://example.com/infj.jpg', '[I,N,F,J]', 70, 1879160617705476097, 1878092630856675329),
       ('INFP', '内向、直觉、情感、感知型', 'https://example.com/infp.jpg', '[I,N,F,P]', 65, 1879160617705476097, 1878092630856675329),
       ('ENFJ', '外向、直觉、情感、判断型', 'https://example.com/enfj.jpg', '[E,N,F,J]', 60, 1879160617705476097, 1878092630856675329),
       ('ENFP', '外向、直觉、情感、感知型', 'https://example.com/enfp.jpg', '[E,N,F,P]', 55, 1879160617705476097, 1878092630856675329),
       ('ISTJ', '内向、感觉、思考、判断型', 'https://example.com/istj.jpg', '[I,S,T,J]', 50, 1879160617705476097, 1878092630856675329),
       ('ISFJ', '内向、感觉、情感、判断型', 'https://example.com/isfj.jpg', '[I,S,F,J]', 45, 1879160617705476097, 1878092630856675329),
       ('ESTJ', '外向、感觉、思考、判断型', 'https://example.com/estj.jpg', '[E,S,T,J]', 40, 1879160617705476097, 1878092630856675329),
       ('ESFJ', '外向、感觉、情感、判断型', 'https://example.com/esfj.jpg', '[E,S,F,J]', 35, 1879160617705476097, 1878092630856675329),
       ('ISTP', '内向、感觉、思考、感知型', 'https://example.com/istp.jpg', '[I,S,T,P]', 30, 1879160617705476097, 1878092630856675329),
       ('ISFP', '内向、感觉、情感、感知型', 'https://example.com/isfp.jpg', '[I,S,F,P]', 25, 1879160617705476097, 1878092630856675329),
       ('ESTP', '外向、感觉、思考、感知型', 'https://example.com/estp.jpg', '[E,S,T,P]', 20, 1879160617705476097, 1878092630856675329),
       ('ESFP', '外向、感觉、情感、感知型', 'https://example.com/esfp.jpg', '[E,S,F,P]', 15,1879160617705476097 , 1878092630856675329);
