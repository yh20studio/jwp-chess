package chess.service;

import chess.dao.ChessDao;
import chess.dao.MovementDao;
import chess.domain.board.Board;
import chess.domain.game.ChessGame;
import chess.domain.position.Position;
import chess.entity.Chess;
import chess.entity.Movement;
import chess.exception.NotExistRoomException;
import chess.service.dto.ChessSaveRequestDto;
import chess.service.dto.CommonResponseDto;
import chess.service.dto.GameStatusDto;
import chess.service.dto.GameStatusRequestDto;
import chess.service.dto.MoveRequestDto;
import chess.service.dto.MoveResponseDto;
import chess.service.dto.ResponseCode;
import chess.service.dto.TilesDto;
import java.util.List;

public class ChessService {

    private final ChessDao chessDao;
    private final MovementDao movementDao;

    public ChessService(final ChessDao chessDao, final MovementDao movementDao) {
        this.chessDao = chessDao;
        this.movementDao = movementDao;
    }

    public TilesDto emptyBoard() {
        return new TilesDto(Board.emptyBoard());
    }

    public CommonResponseDto<MoveResponseDto> movePiece(final String gameName, final MoveRequestDto requestDto) {
        try {
            final ChessGame chessGame = ChessGame.newGame();
            final List<Movement> movements = movementDao.findByChessName(gameName);

            for (final Movement movement : movements) {
                chessGame.moveByTurn(new Position(movement.getSourcePosition()), new Position(movement.getTargetPosition()));
            }

            chessGame.moveByTurn(new Position(requestDto.getSource()), new Position(requestDto.getTarget()));
            final Chess chess = findChessByName(gameName);
            movementDao.save(new Movement(chess.getId(), requestDto.getSource(), requestDto.getTarget()));

            if (chessGame.isGameOver()) {
                chess.changeRunning(!chessGame.isGameOver());
                chess.changeWinnerColor(chessGame.findWinner());
                chessDao.update(chess);
            }
            return new CommonResponseDto<>(
                new MoveResponseDto(requestDto.getSource(), requestDto.getTarget(), chessGame.calculateScore(),
                    !chess.isRunning()),
                ResponseCode.OK.code(), ResponseCode.OK.message());
        } catch (RuntimeException exception) {
            return new CommonResponseDto<>(ResponseCode.BAD_REQUEST.code(), exception.getMessage());
        }
    }

    public void changeGameStatus(final GameStatusRequestDto requestDto) {
        final ChessGame chessGame = ChessGame.newGame();
        final Chess chess = findChessByName(requestDto.getChessName());
        final List<Movement> movements = movementDao.findByChessName(chess.getName());

        for (final Movement movement : movements) {
            chessGame.moveByTurn(new Position(movement.getSourcePosition()), new Position(movement.getTargetPosition()));
        }

        chess.changeRunning(!requestDto.isGameOver());
        chess.changeWinnerColor(chessGame.findWinner());
        chessDao.update(chess);
    }

    public CommonResponseDto<GameStatusDto> startChess(final ChessSaveRequestDto request) {
        try {
            final ChessGame chessGame = ChessGame.newGame();
            final Chess chess = new Chess(request.getName());
            chessDao.save(chess);
            return new CommonResponseDto<>(
                new GameStatusDto(chessGame.pieces(), chessGame.calculateScore(), chessGame.isGameOver(),
                    chess.getWinnerColor()), ResponseCode.OK.code(), ResponseCode.OK.message());
        } catch (RuntimeException exception) {
            return new CommonResponseDto<>(ResponseCode.BAD_REQUEST.code(), exception.getMessage());
        }
    }

    public CommonResponseDto<GameStatusDto> loadChess(final String chessName) {
        try {
            final ChessGame chessGame = ChessGame.newGame();
            final Chess chess = findChessByName(chessName);
            final List<Movement> movements = movementDao.findByChessName(chess.getName());

            for (final Movement movement : movements) {
                chessGame.moveByTurn(new Position(movement.getSourcePosition()), new Position(movement.getTargetPosition()));
            }
            return new CommonResponseDto<>(new GameStatusDto(chessGame.pieces(),
                chessGame.calculateScore(), !chess.isRunning(), chess.getWinnerColor()),
                ResponseCode.OK.code(), ResponseCode.OK.message());
        } catch (RuntimeException exception) {
            return new CommonResponseDto<>(ResponseCode.BAD_REQUEST.code(), exception.getMessage());
        }
    }

    private Chess findChessByName(final String chessName) {
        return chessDao.findByName(chessName).orElseThrow(NotExistRoomException::new);
    }
}