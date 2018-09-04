/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataConverter;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.DataConverterImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A stateless controller for retrieving the status of a result as well as the
 * result themselves.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */

@Stateless
public class ResultController {
	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

	@Inject
	private IRCTApplication irctApp;
	
	@Inject
	private Logger logger;
	
	@Inject
	private IRCTEventListener irctEventListener;

	/**
	 * Returns a list of results that are available for the user to download
	 * 
	 * @param user
	 *            User
	 * @return List of Resutls
	 */
	public List<Result> getAvailableResults(User user) {
//		EntityManager entityManager = objectEntityManager.createEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<Result> criteria = cb.createQuery(Result.class);
		Root<Result> result = criteria.from(Result.class);
		criteria.select(result);

		Predicate restrictions = cb.conjunction();
		restrictions = cb.and(restrictions, cb.isNull(result.get("user")));
		restrictions = cb.or(restrictions, cb.equal(result.get("user"), user));
		restrictions = cb.and(restrictions,
				cb.equal(result.get("resultStatus"), ResultStatus.AVAILABLE));
		criteria.where(restrictions);

		return entityManager.createQuery(criteria).getResultList();
	}

	/**
	 * Returns the status of a result if the user has access to it
	 * 
	 * @param user
	 *            User
	 * @param resultId
	 *            Result Id
	 * @return Status of the result
	 */
	public ResultStatus getResultStatus(User user, Long resultId) {
		List<Result> results = getResults(user, resultId);
		if ((results == null) || (results.isEmpty())) {
			return null;
		}
		return results.get(0).getResultStatus();
	}

	/**
	 * Returns a list of available formats that the user can use to download a
	 * given result if they have access to it
	 * 
	 * @param user
	 *            User
	 * @param resultId
	 *            Result Id
	 * @return Available Formats
	 */
	public List<String> getAvailableFormats(User user, Long resultId) {
		List<Result> results = getResults(user, resultId);

		if (results == null || results.size() == 0) {
			return null;
		}
		if(results.get(0).getResultStatus() != ResultStatus.AVAILABLE) {
			return null;
		}
		
		List<DataConverterImplementation> rdc = irctApp
				.getResultDataConverters().get(results.get(0).getDataType());

		List<String> converterNames = new ArrayList<String>();
		for (DataConverterImplementation rd : rdc) {
			converterNames.add(rd.getFormat());
		}
		return converterNames;
	}

	/**
	 * Returns a datastream object for the given format and result if the user
	 * has access to the result, and the format is of the correct type
	 * 
	 * @param user
	 *            User
	 * @param resultId
	 *            Result Id
	 * @param format
	 *            Form
	 * @return Result Data Stream
	 */
	public ResultDataStream getResultDataStream(User user, Long resultId,
			String format) {
		
		logger.log(Level.FINE, "getResultDataStream() user:"+user.getName()+" resultId:"+resultId+" format:"+(format==null?"NULL":format));
		ResultDataStream rds = new ResultDataStream();
		List<Result> results = getResults(user, resultId);

		if (results == null || results.size() == 0) {
			rds.setMessage("Unable to find result with current user: " + user.getName() +
					" and resultId: " + resultId);
			return rds;
		} else {
			logger.log(Level.FINE, "getResultDataStream() there are ```"+results.size()+"``` results found.");
		}
		Result result = results.get(0);
		
		logger.log(Level.FINE, "getResultDataStream() The first result status is "+result.getResultStatus().name());
		if(result.getResultStatus() != ResultStatus.AVAILABLE) {
			rds.setMessage("Result is not available");
			return rds;
		}
		
		ResultDataConverter rdc = irctApp.getResultDataConverter(
				result.getDataType(), format);
		
		logger.log(Level.FINE, "getResultDataStream() ResultDataConverter has been retrieved");
		if (rdc == null) {
			rds.setMessage("Unable to find format");
			return rds;
		}

		rds.setMediaType(rdc.getMediaType());
		rds.setResult(rdc.createStream(result));
		rds.setFileExtension(rdc.getFileExtension());
		rds.setMessage(result.getMessage());

		return rds;
	}

	/**
	 * Returns the results if the user has access to it
	 * 
	 * @param user
	 *            user
	 * @param resultId
	 *            Result Id
	 * @return Result
	 */
	public Result getResult(User user, Long resultId) {
		List<Result> results = getResults(user, resultId);

		if ((results == null) || (results.isEmpty())) {
			return null;
		}

		return results.get(0);
	}

	private List<Result> getResults(User user, Long resultId) {
		irctEventListener.beforeGetResult(user, resultId);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<Result> criteria = cb.createQuery(Result.class);
		Root<Result> resultRoot = criteria.from(Result.class);
		criteria.select(resultRoot);

		Predicate restrictions = cb.conjunction();
		restrictions = cb.and(restrictions, cb.isNull(resultRoot.get("user")));
		restrictions = cb.or(restrictions,
				cb.equal(resultRoot.get("user"), user));
		restrictions = cb.and(restrictions,
				cb.equal(resultRoot.get("id"), resultId));
		criteria.where(restrictions);

		List<Result> results = entityManager.createQuery(criteria).getResultList();

		if ((results == null) || (results.isEmpty())) {
			return null;
		}
		
		irctEventListener.afterGetResult(results.get(0));
		
		return results;
	}

	/**
	 * Creates a new result that is associated with a default Result Data Type
	 * 
	 * @param resultDataType
	 *            Data Type
	 * @return Result
	 * @throws PersistableException
	 *             An error occurred creating the Result
	 */
	public Result createResult(ResultDataType resultDataType)
			throws PersistableException {
		logger.log(Level.FINE, "createResult() "+resultDataType.toString());
		
		Result result = new Result();
		entityManager.persist(result);

		return updateResult(resultDataType, result);
	}

	public Result updateResult(ResultDataType resultDataType, Result result)
			throws PersistableException {
		logger.log(Level.FINE, "createResult() "+resultDataType.toString());

		result.setDataType(resultDataType);
		result.setStartTime(new Date());

		if (resultDataType == ResultDataType.TABULAR) {
			FileResultSet frs = new FileResultSet();
			frs.persist(irctApp.getResultDataFolder()
					+ "/" + result.getId());
			result.setResultSetLocation(irctApp.getResultDataFolder()
					+ "/" + result.getId());
			result.setData(frs);
		} else if (resultDataType == ResultDataType.JSON) {
			throw new PersistableException("ResultDataType JSON is not implemented");
		} else {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Unknown Result Data Type");
			return result;
		}
		result.setResultStatus(ResultStatus.CREATED);
		entityManager.merge(result);
		return result;
	}

	/**
	 * Updates the given result with the new information
	 * 
	 * @param result
	 *            Result
	 */
	public void mergeResult(Result result) {
		irctEventListener.beforeSaveResult(result);
		entityManager.merge(result);
		irctEventListener.afterSaveResult(result);
	}

}
