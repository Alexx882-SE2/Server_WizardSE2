package com.aau.wizard.service.impl;

import com.aau.wizard.dto.CardDto;
import com.aau.wizard.dto.PlayerDto;
import com.aau.wizard.dto.request.GameRequest;
import com.aau.wizard.dto.response.GameResponse;
import com.aau.wizard.model.Game;
import com.aau.wizard.model.Player;
import com.aau.wizard.service.interfaces.GameService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the GameService interface.
 * Manages active games in memory and handles game-related logic like joining and tracking player state.
 */
@Service
public class GameServiceImpl implements GameService {
    /**
     * In-memory storage of all active games, keyed by their gameId.
     */
    private final Map<String, Game> games = new HashMap<>();


    /**
     * Handles a player joining a game. Creates the game if it doesn't exist,
     * adds the player if not already present, and returns the updated game state.
     *
     * @param request contains the gameId and player details
     * @return a GameResponse reflecting the current state of the game
     */
    @Override
    public GameResponse joinGame(GameRequest request) {
        // check if there is a game, if not create one with the given id
        Game game = games.computeIfAbsent(request.getGameId(), Game::new);

        addPlayerIfAbsent(game, request);

        return createGameResponse(game, request.getPlayerId());
    }

    /**
     * Constructs a GameResponse for a given game and requesting player.
     * This includes visible player data and the requesting player's hand cards.
     *
     * @param game the game object to transform
     * @param requestingPlayerId the player for whom the response is built
     * @return a fully populated GameResponse
     */
    private GameResponse createGameResponse(Game game, String requestingPlayerId) {
        List<PlayerDto> playerDtos = game.getPlayers().stream()
                .map(PlayerDto::from)
                .toList();

        Player requestingPlayer = game.getPlayerById(requestingPlayerId);

        List<CardDto> handCards = requestingPlayer != null
                ? requestingPlayer.getHandCards().stream()
                .map(CardDto::from)
                .toList()
                : List.of();

        return new GameResponse(
                game.getGameId(),
                game.getStatus(),
                game.getCurrentPlayerId(),
                playerDtos,
                handCards,
                null // lastPlayedCard can be set here later on
        );
    }

    /**
     * Adds a new player to the game if they are not already part of it.
     *
     * @param game the game the player wants to join
     * @param request the request containing playerId and playerName
     */
    private void addPlayerIfAbsent(Game game, GameRequest request) {
        if (playerNotInGame(game, request)) {
            Player newPlayer = new Player(request.getPlayerId(), request.getPlayerName());
            game.getPlayers().add(newPlayer);
        }
    }

    /**
     * Checks whether the player is not yet part of the game.
     * Used to avoid duplicate joins.
     */
    private boolean playerNotInGame(Game game, GameRequest request) {
        return game.getPlayerById(request.getPlayerId()) == null;
    }
}
