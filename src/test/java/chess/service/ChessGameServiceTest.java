package chess.service;

import static chess.domain.Color.BLACK;
import static chess.domain.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import chess.dao.ChessGameDao;
import chess.dao.PieceDao;
import chess.domain.ChessGame;
import chess.domain.Position;
import chess.domain.PromotionPiece;
import chess.domain.piece.Piece;
import chess.domain.piece.pawn.Pawn;
import chess.domain.piece.single.Knight;
import chess.domain.state.Turn;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
class ChessGameServiceTest {

    private ChessGameService chessGameService;
    private PieceDao pieceDao;
    private long chessGameId;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;


    @BeforeEach
    void setUp() {
        ChessGameDao chessGameDao = new ChessGameDao(jdbcTemplate, dataSource);
        pieceDao = new PieceDao(jdbcTemplate);
        chessGameService = new ChessGameService(pieceDao, chessGameDao);
        ChessGame chessGame = new ChessGame(Turn.WHITE_TURN.name(), "title", "password");
        ChessGame savedChessGame = chessGameDao.createChessGame(chessGame);
        chessGameId = savedChessGame.getId();
    }

    @Test
    @DisplayName("Position을 받아 상대 기물이 있는 곳에 move")
    void moveTargetPosition() {
        // given
        Position source = Position.of('a', '1');
        Position target = Position.of('b', '2');
        pieceDao.savePieces(chessGameId, Map.of(
                source, new Piece(WHITE, new Pawn(WHITE)),
                target, new Piece(BLACK, new Knight())
        ));

        // when
        chessGameService.move(chessGameId, source, target);
        Piece piece = pieceDao.findChessBoardByChessGameId(chessGameId).getPieces()
                .get(target);

        // then
        assertAll(
                () -> assertThat(piece.name()).isEqualTo("pawn"),
                () -> assertThat(piece.color()).isEqualTo(WHITE)
        );
    }

    @Test
    @DisplayName("Position을 받아 빈 곳에 move")
    void moveEmptyPosition() {
        // given
        Position source = Position.of('a', '1');
        Position target = Position.of('a', '2');
        pieceDao.savePieces(chessGameId, Map.of(source, new Piece(WHITE, new Pawn(WHITE))));

        // when
        chessGameService.move(chessGameId, source, target);
        Piece piece = pieceDao.findChessBoardByChessGameId(chessGameId).getPieces()
                .get(target);

        // then
        assertAll(
                () -> assertThat(piece.name()).isEqualTo("pawn"),
                () -> assertThat(piece.color()).isEqualTo(WHITE)
        );
    }

    @Test
    @DisplayName("pawn 프로모션")
    void promotion() {
        // given
        Position source = Position.of('a', '8');
        pieceDao.savePieces(chessGameId, Map.of(
                source, new Piece(WHITE, new Pawn(WHITE))
        ));

        // when
        chessGameService.promotion(chessGameId, PromotionPiece.BISHOP);
        Piece piece = pieceDao.findChessBoardByChessGameId(chessGameId).getPieces()
                .get(source);

        // then
        assertAll(
                () -> assertThat(piece.name()).isEqualTo("bishop"),
                () -> assertThat(piece.color()).isEqualTo(WHITE)
        );
    }
}
