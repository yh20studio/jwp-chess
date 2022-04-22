package chess.service;

import chess.dao.ChessGameDao;
import chess.dao.PieceDao;
import chess.domain.ChessBoard;
import chess.domain.Position;
import chess.domain.PromotionPiece;
import chess.domain.piece.PieceFactory;
import chess.domain.state.ChessGameState;
import chess.domain.state.Turn;
import org.springframework.stereotype.Service;

@Service
public class ChessGameService {

	private final PieceDao pieceDao;
	private final ChessGameDao chessGameDao;

	public ChessGameService(PieceDao pieceDao, ChessGameDao chessGameDao) {
		this.pieceDao = pieceDao;
		this.chessGameDao = chessGameDao;
	}

	public long createNewChessGame() {
		long chessGameId = chessGameDao.createChessGame(Turn.WHITE_TURN);
		pieceDao.savePieces(chessGameId, PieceFactory.createNewChessBoard(chessGameId));
		return chessGameId;
	}

	public ChessBoard findChessBoard(long chessGameId) {
		return pieceDao.findChessBoardByChessGameId(chessGameId);
	}

	public void move(long chessGameId, Position source, Position target) {
		ChessGameState chessGameState = findChessGameState(chessGameId);
		chessGameState.movePiece(source, target);

		pieceDao.delete(target);
		pieceDao.updatePiecePosition(source, target);
		chessGameDao.changeChessGameTurn(chessGameId, chessGameState.nextTurn());
	}

	public void promotion(long chessGameId, PromotionPiece promotionPiece) {
		ChessGameState chessGameState = findChessGameState(chessGameId);
		Position position = chessGameState.promotion(promotionPiece);

		pieceDao.updatePieceRule(position, promotionPiece.pieceRule());
		chessGameDao.changeChessGameTurn(chessGameId, chessGameState.nextTurn());
	}

	private ChessGameState findChessGameState(long chessGameId) {
		Turn currentTurn = findChessGameTurn(chessGameId);
		ChessBoard chessBoard = pieceDao.findChessBoardByChessGameId(chessGameId);
		return currentTurn.createGameTurn(chessBoard);
	}

	private Turn findChessGameTurn(long chessGameId) {
		return chessGameDao.findChessGame(chessGameId);
	}
}