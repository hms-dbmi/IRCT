package edu.harvard.hms.dbmi.bd2k.irct.model.result.json;

import com.fasterxml.jackson.databind.JsonNode;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * The JSON File Result allows for a json result to persisted.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class JsonFileResult implements Persistable, JSONResultImpl {
	private JsonNode jsonNode;
	private String fileLocation;
	private Path dataFile;
	private boolean current = false;
	private boolean persisted = false;
	private boolean closed = false;

	/**
	 * Creates a JSON File Result
	 */
	public JsonFileResult() {

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(String location) throws ResultSetException,
			PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAvailable(String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void persist() throws PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public void persist(String location) throws PersistableException {
//		try {
//			File file = new File()
//		}


	}

	@Override
	public void merge() throws PersistableException {


	}

	@Override
	public void refresh() throws PersistableException {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONResultImpl setJsonNode(JsonNode jsonNode) {
		this.jsonNode = jsonNode;
		return this;
	}

	@Override
	public boolean isCurrent() {
		return this.current;
	}

	@Override
	public boolean isPersisted() {
		return this.persisted;
	}

	@Override
	public boolean isClosed() {
		return this.closed;
	}

	@Override
	public List<File> getFileList() {
		// TODO Auto-generated method stub
		return null;
	}
}
