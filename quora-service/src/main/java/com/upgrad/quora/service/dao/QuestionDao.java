package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * This method persists a new question to db
     *
     * @param questionEntity - question which has to be persisted to db
     * @return - question which is persisted
     */
    public QuestionEntity createQuestion(final QuestionEntity questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }


    /**
     * Fetch question from db of a given id.
     *
     * @param uuid - id of question to be fetched from db
     * @return question
     */
    public QuestionEntity getQuestionByUUId(final String uuid) {
        try {
            return entityManager.createNamedQuery("questionById", QuestionEntity.class).setParameter("questionUUId", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Return list of questions belonging to a user.
     *
     * @param uuid - user for which the question-list is to fetched
     * @return list of questions
     */
    public List<QuestionEntity> getAllQuestionsByUser(final String userUuid) {
        try {
            return entityManager.createNamedQuery("allQuestionsByUser", QuestionEntity.class).setParameter("userUuid", userUuid).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Return list of all questions in db
     *
     * @return list of questions
     */
    public List<QuestionEntity> getAllQuestions() {
        try {
            return entityManager.createNamedQuery("allQuestions", QuestionEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Returns user belonging to the access token from db
     *
     * @param accesstoken - token of user which has to be fetched from db
     * @return - user
     */
    public UserAuthEntity getUserAuthToken(final String accesstoken) {
        try {
            return entityManager.createNamedQuery("userAuthByAccessToken", UserAuthEntity.class).setParameter("accessToken", accesstoken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Deletes question with a given uuId
     *
     * @param questionId - of the question to be deleted
     * @return question that has been deleted.
     */
    public QuestionEntity deleteQuestion(final String questionId) {
        final QuestionEntity questionEntity = getQuestionByUUId(questionId);
        entityManager.remove(questionEntity);
        return questionEntity;
    }

    public QuestionEntity editQuestionContent(final QuestionEntity questionEntity) {
        entityManager.merge(questionEntity);
        return questionEntity;
    }
}