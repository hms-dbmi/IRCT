/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataConverter;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;

/**
 * A data converter that returns a CSV Stream
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class CSVTabularDataConverter implements ResultDataConverter {

	private Log log;
	
	public CSVTabularDataConverter() {
		log = LogFactory.getLog("CSV Tabular Data Converter");
	}
	
	@Override
	public ResultDataType getResultDataType() {
		return ResultDataType.TABULAR;
	}
	
	@Override
	public String getFileExtension() {
		return ".csv";
	}

	@Override
	public String getName() {
		return "CSV";
	}

	@Override
	public String getMediaType() {
		return "text/csv";
	}

	@Override
	public StreamingOutput createStream(final Result result) {
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream outputStream) throws IOException,
					WebApplicationException {
				ResultSet rs = null;
				CSVPrinter printer = null;
				try {
					rs = (ResultSet) result.getData();
					rs.load(result.getResultSetLocation());
					
					if (rs.getColumnSize() < 1) {
						throw new RuntimeException("No columns/data available for retrieval.");
					}
					
					printer = new CSVPrinter(new OutputStreamWriter(
							outputStream), CSVFormat.DEFAULT);

					String[] columnHeaders = new String[rs.getColumnSize()];
					for (int i = 0; i < rs.getColumnSize(); i++) {
						columnHeaders[i] = rs.getColumn(i).getName();
					}
					printer.printRecord((Object[]) columnHeaders);

					rs.beforeFirst();
					while (rs.next()) {
						String[] row = new String[rs.getColumnSize()];
						for (int i = 0; i < rs.getColumnSize(); i++) {
							row[i] = rs.getString(i);
						}
						printer.printRecord((Object[]) row);
					}
					printer.flush();
					
				} catch (Exception e) {
					log.error("createStream() Exception (creating CSV): " + e.getMessage());
				} finally {
					if(printer != null) {
						printer.close();
					}
					
					if(rs != null && !rs.isClosed()) {
						try {
							rs.close();
						} catch (ResultSetException e) {
							e.printStackTrace();
						}
					}
					if(outputStream != null) {
						outputStream.close();
					}
				}
				
			}
		};
		return stream;
	}
}
