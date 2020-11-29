package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
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
    public AnswerEntity createAnswer(AnswerEntity answerEntity, final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {
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

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity getAnswerbyUuid(final String uuid) {
        AnswerEntity answerEntity = answerDao.getAnswerByUuId(uuid);
        return answerEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(AnswerEntity answerEntity)
    {
        AnswerEntity deletedAnswer = answerDao.deleteAnswer(answerEntity);
        return deletedAnswer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity validateAnswerEntity(AnswerEntity answerEntity, final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException {

        if (answerEntity == null) {
            throw new AuthorizationFailedException("ANS-001", "Entered answer uuid does not exist");
        }

        UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        UserEntity userEntity = userAuthToken.getUserEntity();
        Boolean isUserOwnerOfAnswer = answerDao.isUserOwnerOfAnswer(answerEntity.getUuid(), userEntity.getUuid());

        if (isUserOwnerOfAnswer == false && userEntity.getRole() != "admin") {
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner can edit or delete the answer");
        }
        return answerEntity;
    }
}
