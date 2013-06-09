package es.weso.amg.rest;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mongodb.MongoException;

import es.weso.wesomender.business.WesomenderLogic;

@Controller
public class WesomenderServices {

	@RequestMapping(value = "/rate", method = RequestMethod.POST)
	public void rank(@RequestParam String articleId,
			@RequestParam String userId, @RequestParam Double score)
			throws UnknownHostException, MongoException {
		WesomenderLogic.getInstance().rate(articleId, userId, score);
	}

	@RequestMapping(value = "/recommend/{userId}/{lat}/{lon}", method = RequestMethod.GET)
	public ResponseEntity<String> recommend(@PathVariable String userId,
			@PathVariable double lat, @PathVariable double lon)
			throws MongoException, TasteException, JsonGenerationException,
			JsonMappingException, IOException {
		return new ResponseEntity<String>("jsonpCallback("
				+ new ObjectMapper().writeValueAsString(WesomenderLogic
						.getInstance().recommend(userId, lat, lon)) + ")",
				org.springframework.http.HttpStatus.OK);
	}

	@RequestMapping(value = "/evaluate", method = RequestMethod.GET)
	public String evaluate() throws UnknownHostException, MongoException {
		WesomenderLogic.getInstance().evaluate();
		return "Evaluating";
	}
}
