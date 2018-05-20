package de.codeboje.tuts.sheet2site

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import org.springframework.beans.factory.annotation.Value
import com.google.api.services.sheets.v4.SheetsRequestInitializer

@Controller
class PageController (@Value("\${google.api.key}") googleKey:String) {
	
	val googleKey = googleKey

	val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
	val JSON_FACTORY = JacksonFactory.getDefaultInstance()

	@GetMapping("/site/{sheetId}")
	fun getSite(@PathVariable("sheetId") sheetId: String?, model: ModelAndView): String {

		println(googleKey)
		val sheetsService = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, null).setSheetsRequestInitializer(SheetsRequestInitializer(googleKey)).build()


		val response = sheetsService.spreadsheets().values()
				.get(sheetId, "!A3:E")
				.execute();
		val values = response.getValues();
		if (values != null && !values.isEmpty()) {
			for (row in values) {
				// Print columns A and E, which correspond to indices 0 and 4.
				System.out.printf("%s, %s\n", row.get(0), row.get(1));
			}
		}

		return "site"
	}
}