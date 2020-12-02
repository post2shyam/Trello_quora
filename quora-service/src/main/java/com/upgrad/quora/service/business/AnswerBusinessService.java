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

import java.util.List;

@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;

    /**
     * @param answerEntity
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(final AnswerEntity answerEntity, final String authorization) throws InvalidQuestionException {
        if (answerEntity.getQuestion() == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        final UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        answerEntity.setUser(userAuthToken.getUserEntity());
        return answerDao.createAnswer(answerEntity);
    }

    /**
     * @param uuid
     * @return
     */
    public AnswerEntity getAnswerbyUuid(final String uuid) {
        return answerDao.getAnswerByUuId(uuid);
    }

    /**
     * @param answerEntity
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final AnswerEntity answerEntity) {
        AnswerEntity deletedAnswer = answerDao.deleteAnswer(answerEntity);
        return deletedAnswer;
    }

    /**
     * @param answerEntity
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(final AnswerEntity answerEntity) {
        return answerDao.editAnswer(answerEntity);
    }

    /**
     * @param questionEntity
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     */
    public List<AnswerEntity> getAllAnswersToQuestion(final QuestionEntity questionEntity) {
        return answerDao.getAllAnswersToQuestion(questionEntity.getUuid());
    }

    /**
     * @param answerEntity
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    public AnswerEntity validateAnswerEntity(final AnswerEntity answerEntity, final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException {
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }

        final UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        final UserEntity userEntity = userAuthToken.getUserEntity();
        final Boolean isUserOwnerOfAnswer = answerDao.isUserOwnerOfAnswer(answerEntity.getUuid(), userEntity.getUuid());

        if (!isUserOwnerOfAnswer) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }
        return answerEntity;
    }
}
