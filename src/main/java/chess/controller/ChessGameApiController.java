package chess.controller;

import chess.service.SpringChessService;
import chess.service.dto.ChessSaveRequestDto;
import chess.service.dto.CommonResponseDto;
import chess.service.dto.GameStatusDto;
import chess.service.dto.GameStatusRequestDto;
import chess.service.dto.MoveRequestDto;
import chess.service.dto.MoveResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class ChessGameApiController {

    private final SpringChessService chessService;

    public ChessGameApiController(SpringChessService chessService) {
        this.chessService = chessService;
    }

    @PostMapping
    public ResponseEntity<Object> saveChess(@RequestBody final ChessSaveRequestDto requestDto) {
        chessService.saveChess(requestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Object> finishChess(@RequestBody final GameStatusRequestDto requestDto) {
        chessService.changeGameStatus(requestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{name}")
    public ResponseEntity<GameStatusDto> loadChess(@PathVariable final String name) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chessService.loadChess(name));
    }

    @PutMapping("/{name}/pieces")
    public ResponseEntity<MoveResponseDto> movePieces(@PathVariable("name") final String gameName,
        @RequestBody final MoveRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(chessService.movePiece(gameName, requestDto));
    }
}