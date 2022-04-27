package fr.epita.assistant.jws.presentation.rest;

import fr.epita.assistant.jws.converter.GameEntityToGameDetailResponseDTO;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.service.GameService;
import fr.epita.assistant.jws.presentation.rest.request.CreateGameRequestDTO;
import fr.epita.assistant.jws.presentation.rest.request.MoveRequestDTO;
import fr.epita.assistant.jws.presentation.rest.response.GameResponseDTO;
import fr.epita.assistant.jws.utils.TodoState;
import fr.epita.assistant.jws.utils.exception_400;
import fr.epita.assistant.jws.utils.exception_404;
import fr.epita.assistant.jws.utils.exception_429;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameRessource {
    @Inject
    GameService gameService;
    @Inject
    GameEntityToGameDetailResponseDTO gameEntityToGameDetailResponseDTO;

    @GET
    @Path("/games")
    public List<GameResponseDTO> getallgame() {
        var todos = gameService.getAll();

        return todos.stream().map(todo -> new GameResponseDTO(
                todo.id,
                todo.state,
                todo.players.size()
        )).collect(Collectors.toList());
    }

    @POST
    @Path("/games")
    public Response create(CreateGameRequestDTO request) {
        if (request == null || request.name == null) {
            return Response.status(400).build();
        }

        var gameEntity = gameService.createGame(request.name);
        return Response.ok(gameEntityToGameDetailResponseDTO.convert(gameEntity)).build();
    }

    @GET
    @Path("/games/{gameId}")
    public Response getInfo(@PathParam("gameId") Long gameId) {
        var check = gameService.getInfo(gameId);
        if (gameId == null){
            return Response.status(400).build();
        }
        if (check == null) {
            return Response.status(404).build();
        }
        return Response.ok(gameEntityToGameDetailResponseDTO.convert(check)).build();
    }

    @POST
    @Path("/games/{gameId}")
    public Response join(@PathParam("gameId") Long gameId, CreateGameRequestDTO requestDTO) {
        if (requestDTO == null ||requestDTO.name == null){
            return Response.status(400).build();
        }
        GameEntity gameEntity = null;
        try {
            gameEntity = gameService.joinGame(gameId, requestDTO);
        } catch (exception_404 e) {
            return Response.status(404).build();
        } catch (exception_429 e) {
            return Response.status(429).build();
        } catch (exception_400 e) {
            return Response.status(400).build();
        }
        return Response.ok(gameEntityToGameDetailResponseDTO.convert(gameEntity)).build();
    }

    @PATCH
    @Path("/games/{gameId}/start")
    public Response start(@PathParam("gameId") Long gameId) {
        var gameEntity = gameService.startGame(gameId);
        if (gameEntity == null) {
            return Response.status(404).build();
        }
        return Response.ok(gameEntityToGameDetailResponseDTO.convert(gameEntity)).build();
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/move")
    public Response move(@PathParam("gameId") Long gameId, @PathParam("playerId") Long playerId, MoveRequestDTO requestDTO) {

        if (gameId == null || playerId == null){
            Response.status(404).build();
        }
        GameEntity game = null;
        try {
            game = gameService.move(gameId, playerId, requestDTO.posX , requestDTO.posY);
        }
        catch (exception_400 e) {
            return Response.status(400).build();
        }
        catch (exception_404 e){
            return Response.status(404).build();
        }
        catch (exception_429 e) {
            return Response.status(429).build();
        }
        return Response.ok(gameEntityToGameDetailResponseDTO.convert(game)).build();
    }

    @ConfigProperty(name = "JWS_TICK_DURATION") int tick_dur;
    @ConfigProperty(name = "JWS_DELAY_BOMB") int delay_bomb;
    @POST
    @Path("/games/{gameId}/players/{playerId}/bomb")
    public Response bomb(@PathParam("gameId") Long gameId, @PathParam("playerId") Long playerId, MoveRequestDTO requestDTO) {

        if (gameId == null || playerId == null) {
            Response.status(404).build();
        }
        GameEntity game = null;
        try {
            game = gameService.bomb(gameId, playerId, requestDTO);
        }
        catch (exception_400 e) {
            return Response.status(400).build();
        }
        catch (exception_404 e){
            return Response.status(404).build();
        }
        catch (exception_429 e) {
            return Response.status(429).build();
        }
        CompletableFuture.runAsync(() -> gameService.bomb_explosion(gameId, requestDTO), CompletableFuture.delayedExecutor(delay_bomb, TimeUnit.MILLISECONDS));
        return Response.ok(gameEntityToGameDetailResponseDTO.convert(game)).build();
    }
}
