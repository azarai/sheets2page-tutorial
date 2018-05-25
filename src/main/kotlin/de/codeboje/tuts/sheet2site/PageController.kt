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
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange

@Controller
class PageController(@Value("\${google.api.key}") googleKey: String) {

	val googleKey = googleKey

	val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
	val JSON_FACTORY = JacksonFactory.getDefaultInstance()

	@GetMapping("/site/{sheetId}")
	fun getSite(@PathVariable("sheetId") sheetId: String?, model: ModelAndView): ModelAndView {

		println(googleKey)
		val sheetsService = Sheets.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				null
			).setSheetsRequestInitializer(SheetsRequestInitializer(googleKey)).build()

		val response: BatchGetValuesResponse = sheetsService.spreadsheets().values()
				.batchGet(sheetId).setRanges(listOf("items!A3:Z", "siteinfo!A3:Z"))
				.execute();
		val values = response.getValueRanges();

		model.addObject("items", buildItems(values))
		model.addObject("site", buildSiteMeta(values))
		model.addObject("mapsKey", googleKey)
		model.setViewName("site")

		return model
	}

	private fun getSheet(name: String, values: List<ValueRange>): ValueRange? {
		if (!values.isEmpty()) {
			for (vr in values) {
				println(vr.getRange())
				if (vr.getRange().startsWith(name)) {
					return vr
				}
			}
		}
		return null
	}

	private fun buildSiteMeta(values: List<ValueRange>): Site {
		var sitename: String = ""
		var slogan: String = ""
		var owner: String =""

		val vr = getSheet("siteinfo", values)

		for (row in vr!!.getValues()) {
			val fieldValue: String = row.get(0).toString()
			if ("name" == fieldValue) {
				sitename = row.get(1) as String
			}

			if ("slogan" == fieldValue) {
				slogan = row.get(1) as String
			}
			if ("owner" == fieldValue) {
				owner = row.get(1) as String
			}

		}


		return Site(sitename, slogan, owner)
	}

	private fun buildItems(values: List<ValueRange>): List<Item> {
		val result: MutableList<Item> = mutableListOf()
		val vr = getSheet("items", values)

		for (row in vr!!.getValues()) {
			// Print columns A and E, which correspond to indices 0 and 4.
			//System.out.printf("%s, %s\n", row.get(0), row.get(1));

			println(row)

			result.add(Item(
					row.getOrNull(0) as String?,
					row.getOrNull(1) as String?,
					row.getOrNull(3) as String?,
					row.getOrNull(5) as String?,
					row.getOrNull(6) as String?
			))

		}


		return result
	}

}