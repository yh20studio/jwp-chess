package wooteco.chess.domain.board;

import static java.util.stream.Collectors.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wooteco.chess.domain.entity.ChessGame;
import wooteco.chess.domain.entity.PieceEntity;
import wooteco.chess.domain.piece.Piece;
import wooteco.chess.domain.piece.PieceFactory;
import wooteco.chess.domain.piece.Turn;
import wooteco.chess.domain.position.Path;
import wooteco.chess.domain.position.Position;

public class Board {
	private static final int ALIVE_COUNT = 2;

	private Set<Piece> pieces;

	private Board(Set<Piece> pieces) {
		this.pieces = pieces;
	}

	public static Board of(Set<PieceEntity> pieces) {
		return new Board(pieces.stream()
			.map(PieceFactory::createBy)
			.collect(toSet()));
	}

	public static Board of(List<Piece> pieces) {
		return new Board(new HashSet<>(pieces));
	}

	public static Board of(Map<Position, Piece> board) {
		return new Board(new HashSet<>(board.values()));
	}

	public void move(PieceEntity source, PieceEntity target, ChessGame chessGame) {
		verifyMove(source.getPosition(), target.getPosition(), chessGame.getTurn());

		target.updateSymbolToSource(source);
		source.updateSymbolToEmpty();
		chessGame.updateTurnToNext();
	}

	public void verifyMove(Position from, Position to, Turn current) {
		if (isEnd()) {
			throw new IllegalArgumentException("게임 끝");
		}

		Piece piece = findPieceIn(from);
		Piece target = findPieceIn(to);

		if (piece.isNotSameTeam(current)) {
			throw new IllegalArgumentException("아군 기물의 위치가 아닙니다.");
		}
		if (hasPieceIn(Path.of(from, to)) || piece.canNotMoveTo(target)) {
			throw new IllegalArgumentException("이동할 수 없는 경로입니다.");
		}
	}

	private Piece findPieceIn(Position position) {
		return pieces.stream()
			.filter(piece -> piece.isPositionEquals(position))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("해당 위치에 기물이 존재하지 않습니다. position = " + position.getName()));
	}

	private boolean hasPieceIn(Path path) {
		return path.toList()
			.stream()
			.anyMatch(position -> findPieceIn(position).isObstacle());
	}

	private boolean isEnd() {
		return pieces.stream()
			.filter(Piece::hasToAlive)
			.count() != ALIVE_COUNT;
	}
}