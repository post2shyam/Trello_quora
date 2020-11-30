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

    /**
     * This method persists the new question to the db
     *
     * @param questionEntity - new question which has to be persisted
     * @param authorization  - logged-in user
     * @return persisted new question
     * @throws AuthorizationFailedException - if the user fails to authenticate
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity, final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        questionEntity.setUser(userAuthToken.getUserEntity());
        return questionDao.createQuestion(questionEntity);
    }

    /**
     * This method fetches the question from dbm corresponding to a given id.
     *
     * @param uuid - id of the question which has to be fetched from db
     * @return - asked question
     */
    public QuestionEntity getQuestionEntity(final String uuid) {
        QuestionEntity questionEntity = questionDao.getQuestionByUUId(uuid);
        return questionEntity;
    }

    /**
     * Returns all questions from the database
     *
     * @param authorization - logged-in user
     * @return list of all questions
     * @throws AuthorizationFailedException - if the user is not authenticated.
     */
    public List<QuestionEntity> getAllQuestions(final String authorization) throws AuthorizationFailedException {
        isUserAuthenticated(authorization);
        return questionDao.getAllQuestions();
    }

    /**
     * Returns all questions from the database belonging to a particular user
     *
     * @param authorization - logged-in user
     * @param userId        - user for which the questions are to be listed.
     * @return list of all questions
     * @throws AuthorizationFailedException - if the user is not authenticated.
     */
    public List<QuestionEntity> getAllQuestionsByUser(final String userId, final String authorization) throws AuthorizationFailedException {
        isUserAuthenticated(authorization);
        return questionDao.getAllQuestionsByUser(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(final String questionId, final String authorization) throws AuthorizationFailedException {
        isUserAuthenticated(authorization);
        return questionDao.deleteQuestion(questionId);
    }


    private void isUserAuthenticated(String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }
}
