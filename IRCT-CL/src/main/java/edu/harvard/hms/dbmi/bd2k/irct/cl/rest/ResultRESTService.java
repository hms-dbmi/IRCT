package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Joinable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

/**
 * Creates the result service for the JAX-RS REST service
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/resultService")
@RequestScoped
public class ResultRESTService {
	@Inject
	private ResultController rc;

	/**
	 * Returns a JSON Array list of available formats that can be returned by
	 * this service
	 * 
	 * @return JSON Array of available formats
	 */
	@GET
	@Path("/availableFormats")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure availableFormats() {
		JsonArrayBuilder build = Json.createArrayBuilder();
		build.add("json");
		build.add("xml");
		build.add("xlsx");
		build.add("csv");

		return build.build();
	}

	/**
	 * Returns a JSON Array list of available results
	 * 
	 * NOTE: NOT IMPLEMENTED YET
	 * 
	 * @return JSON Array of available results
	 */
	@GET
	@Path("/results")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure availableResults() {
		JsonObjectBuilder build = Json.createObjectBuilder();
		// TODO: IMPLEMENT
		return build.build();
	}

	/**
	 * Returns a result
	 * 
	 * NOTE: NOT IMPLEMENTED YET
	 * 
	 * @param resultId
	 *            Result ID
	 * @return JSON Object of the result
	 */
	@GET
	@Path("/result{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure availableResults(@PathParam("id") String resultId) {
		JsonObjectBuilder build = Json.createObjectBuilder();
		// TODO: IMPLEMENT
		return build.build();
	}

	/**
	 * Returns a JSON representation of the result
	 * 
	 * @param resultId
	 *            Result ID
	 * @return JSON representation of the result
	 */
	@GET
	@Path("/result/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure jsonResultSet(@PathParam("id") String resultId) {
		JsonObjectBuilder build = Json.createObjectBuilder();

		try {
			ResultSet rs = rc.getResultSet(Long.valueOf(resultId));

			if (rs.getSize() != 0) {
				JsonArrayBuilder objects = Json.createArrayBuilder();

				rs.beforeFirst();
				while (rs.next()) {
					JsonObjectBuilder result = Json.createObjectBuilder();
					for (int columnIndex = 0; columnIndex < rs.getColumnSize(); columnIndex++) {
						result.add(rs.getColumn(columnIndex).getName(),
								((Joinable) rs).getObject(columnIndex)
										.toString());
					}
					objects.add(result);
				}
				return objects.build();
			}
		} catch (NumberFormatException | ResultSetException
				| PersistableException e) {

			build.add("Error", e.getLocalizedMessage());
			e.printStackTrace();
		}

		return build.build();
	}

	/**
	 * Returns a JSON representation of the result set as an attachment. It
	 * streams the response to lessen the memory footprint.
	 * 
	 * @param resultId
	 *            Result ID
	 * @param servletResponse
	 *            Servlet Response
	 * @return A JSON file
	 */
	@GET
	@Path("/download/json/{id}")
	public Response jsonResultSet(@PathParam("id") String resultId,
			@Context HttpServletResponse servletResponse) {
		JsonObjectBuilder build = Json.createObjectBuilder();

		try {
			ResultSet rs = rc.getResultSet(Long.valueOf(resultId));

			if (rs.getSize() != 0) {
				JsonArrayBuilder objects = Json.createArrayBuilder();

				rs.beforeFirst();
				while (rs.next()) {
					JsonObjectBuilder result = Json.createObjectBuilder();
					for (int columnIndex = 0; columnIndex < rs.getColumnSize(); columnIndex++) {
						result.add(rs.getColumn(columnIndex).getName(),
								((Joinable) rs).getObject(columnIndex)
										.toString());
					}
					objects.add(result);
				}
				return Response
						.ok(objects.build().toString())
						.header("Content-Disposition",
								"attachment; filename=IRCT-" + resultId
										+ ".json").build();
			}

		} catch (NumberFormatException | ResultSetException
				| PersistableException e) {

			build.add("Error", e.getLocalizedMessage());
			e.printStackTrace();
		}

		return Response
				.ok(build.build().toString())
				.header("Content-Disposition",
						"attachment; filename=IRCT-" + resultId + ".json")
				.build();
	}

	/**
	 * Returns an XML representation of the result set as an attachment. It
	 * streams the response to lessen the memory footprint.
	 * 
	 * @param resultId
	 *            Result ID
	 * @param servletResponse
	 *            Servlet Response
	 * @return An XML file
	 */
	@GET
	@Path("/download/xml/{id}")
	public Response xmlResultSet(@PathParam("id") final String resultId,
			@Context HttpServletResponse servletResponse) {

		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException,
					WebApplicationException {
				try {
					ResultSet rs = rc.getResultSet(Long.valueOf(resultId));

					XMLOutputFactory xof = XMLOutputFactory.newInstance();
					XMLStreamWriter xtw = xof
							.createXMLStreamWriter(new OutputStreamWriter(os));
					xtw.writeStartDocument("utf-8", "1.0");
					xtw.writeStartElement("results");
					rs.beforeFirst();
					while (rs.next()) {
						xtw.writeStartElement("result");
						for (int i = 0; i < rs.getColumnSize(); i++) {
							xtw.writeStartElement(rs.getColumn(i).getName());
							xtw.writeCharacters(((Joinable) rs).getObject(i)
									.toString());
							xtw.writeEndElement();
						}
						xtw.writeEndElement();
					}
					xtw.writeEndElement();
					xtw.writeEndDocument();

					xtw.flush();
					xtw.close();
				} catch (XMLStreamException | NumberFormatException
						| ResultSetException | PersistableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		return Response
				.ok(stream)
				.header("Content-Disposition",
						"attachment; filename=IRCT-" + resultId + ".xml")
				.build();

	}

	/**
	 * Returns an XSLX representation of the result set as an attachment. It
	 * streams the response to lessen the memory footprint.
	 * 
	 * @param resultId
	 *            Result ID
	 * @param servletResponse
	 *            Servlet Response
	 * @return XSLX file
	 */
	@GET
	@Path("/download/xlsx/{id}")
	public Response xlsxResultSet(@PathParam("id") final String resultId,
			@Context HttpServletResponse servletResponse) {

		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException,
					WebApplicationException {
				try {
					ResultSet rs = rc.getResultSet(Long.valueOf(resultId));
					SXSSFWorkbook wb = new SXSSFWorkbook(100);

					// Create Sheet
					Sheet sh = wb.createSheet("Results");

					// Create Header
					CellStyle headerStyle = wb.createCellStyle();
					Font font = wb.createFont();
					font.setBoldweight(Font.BOLDWEIGHT_BOLD);
					headerStyle.setFont(font);

					Row headerRow = sh.createRow(0);
					for (int i = 0; i < rs.getColumnSize(); i++) {
						Cell cell = headerRow.createCell(i);
						cell.setCellStyle(headerStyle);
						cell.setCellValue(rs.getColumn(i).getName());
					}

					// Add data
					rs.beforeFirst();
					int rowNum = 1;
					while (rs.next()) {
						Row row = sh.createRow(rowNum);
						for (int i = 0; i < rs.getColumnSize(); i++) {
							Cell cell = row.createCell(i);
							cell.setCellValue(((Joinable) rs).getObject(i)
									.toString());
						}
						rowNum++;
					}
					wb.write(os);
					wb.close();
				} catch (NumberFormatException | ResultSetException
						| PersistableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		return Response
				.ok(stream)
				.header("Content-Disposition",
						"attachment; filename=IRCT-" + resultId + ".xlsx")
				.build();
	}

	/**
	 * Returns an CSV representation of the result set as an attachment. It
	 * streams the response to lessen the memory footprint.
	 * 
	 * @param resultId
	 *            Result Id
	 * @param servletResponse
	 *            Servlet Response
	 * @return CSV file
	 */
	@GET
	@Path("/download/csv/{id}")
	public Response csvResultSet(@PathParam("id") final String resultId,
			@Context HttpServletResponse servletResponse) {

		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException,
					WebApplicationException {
				try {
					CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(
							os), CSVFormat.DEFAULT);

					ResultSet rs = rc.getResultSet(Long.valueOf(resultId));

					String[] columnHeaders = new String[rs.getColumnSize()];
					for (int i = 0; i < rs.getColumnSize(); i++) {
						columnHeaders[i] = rs.getColumn(i).getName();
					}
					printer.printRecord((Object[]) columnHeaders);

					rs.beforeFirst();
					while (rs.next()) {
						String[] row = new String[rs.getColumnSize()];
						for (int i = 0; i < rs.getColumnSize(); i++) {
							row[i] = ((Joinable) rs).getObject(i).toString();
						}
						printer.printRecord((Object[]) row);
					}

					printer.flush();
					printer.close();
				} catch (NumberFormatException | ResultSetException
						| PersistableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		return Response
				.ok(stream)
				.header("Content-Disposition",
						"attachment; filename=IRCT-" + resultId + ".csv")
				.build();
	}
}
