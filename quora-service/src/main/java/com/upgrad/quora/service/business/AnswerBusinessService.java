package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(AnswerEntity answerEntity, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (answerEntity.getQuestion() == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        answerEntity.setUser(userAuthToken.getUserEntity());
        return answerDao.createAnswer(answerEntity);
    }
}
