package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionBusinessService {

    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity, final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        questionEntity.setUser(userAuthToken.getUserEntity());
        return questionDao.createQuestion(questionEntity);
    }

    public QuestionEntity getQuestionEntity(final String uuid) {
        QuestionEntity questionEntity = questionDao.getQuestionByUUId(uuid);
        return questionEntity;
    }

    public List<QuestionEntity> getAllQuestions(final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        return questionDao.getAllQuestions();
    }

    public List<QuestionEntity> getAllQuestionsByUser(final String userId, final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        return questionDao.getAllQuestionsByUser(userId);
    }
}
