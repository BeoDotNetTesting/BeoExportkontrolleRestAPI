package testCases;

import static io.restassured.RestAssured.given;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.restassured.response.Response;

public class ImportAndAuftrangsprufungOnTestServer {
	String token;

	public String generateCurrentDateAndTime() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyhhmmss");
		return formatter.format(date);
	}

	public String date(String format) {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	@Test(priority = 1)
	public void getTocken() {
		JSONObject data = new JSONObject();
		data.put("username", "BEO India Test");
		data.put("Password", "TYt4OOqiAM");
		Response res = given().contentType("application/json").body(data.toString()).when()
				.post("http://49.13.243.142/ExportKontrolle/api/Token/GetToken").then().statusCode(200)
				.header("Content-Type", "application/json; charset=utf-8").log().all().extract().response();

		JSONObject jsonResponse = new JSONObject(res.asString());
		String ststus = jsonResponse.getString("Status");
		Assert.assertEquals(ststus, "Success", "Ststus Success message not as expected");

		String token = jsonResponse.getString("Token");
		this.token = token;
		Assert.assertNotNull(token, "The token is null");
		Assert.assertFalse(token.isEmpty(), "Token is empty");
	}

	 @Test(priority = 2)
	public void importArtikels() {
		int limit = 65;

		JSONObject data = new JSONObject();

		JSONArray artikelList = new JSONArray();
		// Add articles to the JSON array
		for (int i = 0; i < limit; i++) {
			String artNo = "AN_" + date("ddMMyy") + "_" + i;
			artikelList.put(new JSONObject()

					.put("ArtikelNummer", artNo).put("Artikelbezeichnung", "D" + generateCurrentDateAndTime())
					.put("Warennummer", "38260090").put("ALNummern", "AL123").put("AtlasUnterlagenCodes", "3LLA")
					.put("Bestimmungsland", "USA").put("Produktgruppename", "W1234" + date("ddMMyy"))
					.put("Artikelarchivierung", false));
		}
		// Add the article list to the main data object
		data.put("ArtikelList", artikelList);

		given().header("Authorization", "Bearer " + token).contentType("application/json").body(data.toString()).when()
				.post("http://49.13.243.142/Exportkontrolle/api/ArtikelstammImport/ImportArtikelstamm").then()
				.statusCode(200).header("Content-Type", "application/json; charset=utf-8").log().all();
	}

	@Test(priority = 3)
	public void auftrangsprufungImportArtikels() {
		int belegdatenNo = 3;
		int artikeldatenNo = 10;

		JSONObject data = new JSONObject();
		JSONArray belegdatenList = new JSONArray();

		for (int i = 0; i < belegdatenNo; i++) {
			// Add Beleg data to the JSON array
			JSONObject beleg1 = new JSONObject();
			beleg1.put("Belegnummer", "Order_12" + i);
			beleg1.put("Empfängername", "Akhil");
			beleg1.put("StraßeHausnummer", "31");
			beleg1.put("Postleitzahl", "683556");
			beleg1.put("Ort", "valayanchirangara");
			beleg1.put("Land", "IN");
			beleg1.put("LandName", "India");
			beleg1.put("Rechnungsnummer", "R-123");
			beleg1.put("Lieferscheinnummer", "L-456");
			beleg1.put("Rechnungsbetrag", "150.00");
			beleg1.put("GesamtRohmasse", "500.00");

			// Adding Artikeldaten to beleg1
			JSONArray artikeldatenList = new JSONArray();

			for (int j = 0; j < artikeldatenNo; j++) {
				artikeldatenList
						.put(new JSONObject().put("Artikelnummer", "AN12" + i + "_" + date("ddMMyyhhmmss") + "_" + j)
								.put("Position", j).put("Warennummer", "38260090").put("AlNummer", "1c001,8001")
								.put("Artikelbezeichnung", "Product A")
								.put("Produktgruppenname", "Group12" + date("ddMMyy") + "_" + i));

			}
			beleg1.put("Artikeldaten", artikeldatenList);
			belegdatenList.put(beleg1);

		}
		// Add the list of Belegdaten to the main data object
		data.put("Belegdaten", belegdatenList);

		given().header("Authorization", "Bearer " + token).contentType("application/json").body(data.toString()).when()
				.post("http://49.13.243.142/Exportkontrolle/api/AuftrangsprufungImportJson/ImportAuftrangsPrufung")
				.then().statusCode(200).header("Content-Type", "application/json; charset=utf-8").log().all();
	}
}
