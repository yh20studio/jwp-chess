package wooteco.chess.controller;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.handlebars.HandlebarsTemplateEngine;
import wooteco.chess.domain.position.Position;
import wooteco.chess.service.ChessService;

public class SparkChessController implements ChessController {
	private static final Gson GSON = new Gson();
	private static final int MAX_INTERVAL_SECONDS = 300;

	private final ChessService service;

	public SparkChessController(ChessService service) {
		this.service = service;
	}

	@Override
	public void start() {
		get("/", this::renderStart);
		get("/chess", this::renderBoard);
	}

	private String renderStart(Request request, Response response) {
		return render(new HashMap<>(), "index.html");
	}

	private String renderBoard(Request request, Response response) {
		String gameId = request.queryParams("game_id");
		request.session(true).attribute("game_id", gameId);
		request.session().maxInactiveInterval(MAX_INTERVAL_SECONDS);
		service.initialize(gameId);
		return render(service.getBoard(gameId), "chess.html");
	}

	@Override
	public void playTurn() {
		post("/api/move", this::updateBoard);
		get("/status", this::renderResult);
		exception(IllegalArgumentException.class, this::handleException);
	}

	private String updateBoard(Request request, Response response) {
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(request.body());

		String from = element.getAsJsonObject().get("from").getAsString();
		String to = element.getAsJsonObject().get("to").getAsString();
		String gameId = request.session().attribute("game_id");

		service.move(gameId, Position.of(from), Position.of(to));
		return GSON.toJson(from + " " + to);
	}

	private String renderResult(Request request, Response response) {
		String gameId = request.session().attribute("game_id");
		return render(service.getResult(gameId), "status.html");
	}

	private void handleException(IllegalArgumentException exception, Request request, Response response) {
		response.status(400);
		response.body(exception.getMessage());
	}

	private String render(Map<String, String> model, String templatePath) {
		return new HandlebarsTemplateEngine().render(new ModelAndView(model, templatePath));
	}
}