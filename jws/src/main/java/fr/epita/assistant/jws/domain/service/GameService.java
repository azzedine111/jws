package fr.epita.assistant.jws.domain.service;

import fr.epita.assistant.jws.converter.GameModelToGameEntityConverter;
import fr.epita.assistant.jws.data.model.GameModel;
import fr.epita.assistant.jws.data.model.PlayerModel;
import fr.epita.assistant.jws.data.repository.GameRepository;
import fr.epita.assistant.jws.data.repository.PlayerRepository;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.presentation.rest.request.CreateGameRequestDTO;
import fr.epita.assistant.jws.presentation.rest.request.MoveRequestDTO;
import fr.epita.assistant.jws.utils.TodoState;
import fr.epita.assistant.jws.utils.exception_400;
import fr.epita.assistant.jws.utils.exception_404;
import fr.epita.assistant.jws.utils.exception_429;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@ApplicationScoped
public class GameService {
    @Inject
    GameRepository GameRepository;
    @Inject
    PlayerRepository PlayerRepository;
    @Inject
    GameModelToGameEntityConverter gameModelToGameEntityConverter;

    @Transactional
    public List<GameEntity> getAll() {
        var todos = GameRepository.findAll();
        return todos.stream().map(todo -> gameModelToGameEntityConverter.convert(todo))
                .collect(Collectors.toList());        //todoRepository.listAll()
    }

    @ConfigProperty(name = "JWS_MAP_PATH", defaultValue = "src/test/resources/map1.rle")
    String path;

    @Transactional
    public GameEntity createGame(String playerName) {
        var gameModel = new GameModel()
                .withStarttime(LocalDateTime.now())
                .withPlayers(new ArrayList<>())
                .withState(TodoState.STARTING)
                .withMap(new ArrayList<>());
        var player = new PlayerModel()
                .withGame(gameModel)
                .withName(playerName)
                .withLives(3)
                .withPosX(1)
                .withPosY(1)
                .withPosition(1)
                .withLastmovement(LocalDateTime.now().minusHours(1))
                .withLastbomb(LocalDateTime.now().minusHours(1));
        FileReader fileReader = null;
        System.out.println(path);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) { //check si on peut envoyer le .env
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                gameModel.map.add(str);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        gameModel.players.add(player);
        GameRepository.persist(gameModel);
        return gameModelToGameEntityConverter.convert(gameModel);

    }

    @Transactional
    public GameEntity getInfo(Long id) {
        var ent = GameRepository.findById(id);
        if (ent == null) {
            return null;
        }
        return gameModelToGameEntityConverter.convert(ent);
    }

    @Transactional
    public GameEntity joinGame(Long gameId, CreateGameRequestDTO requestDTO) throws exception_400, exception_404, exception_429 {
        var gamer = GameRepository.findById(gameId);
        if (gamer == null){
            throw new exception_404();
        }
        if (requestDTO == null || requestDTO.name == null || gamer.state != TodoState.STARTING || gamer.players.size() >= 4) {
            throw new exception_400();
        }
        var p = new PlayerModel()
                .withName(requestDTO.name)
                .withLives(3)
                .withGame(gamer)
                .withLastbomb(null)
                .withLastmovement(null);
        gamer.players.add(p);
        if (gamer.players.size() == 2) {
            p.posX = 15;
            p.posY = 1;
        }
        else if (gamer.players.size() == 3) {
            p.posX = 15;
            p.posY = 13;
        }
        else {
            p.posX = 1;
            p.posY = 13;
        }
        p.position = gamer.players.size();
        PlayerRepository.persist(p);
        return gameModelToGameEntityConverter.convert(gamer);
    }

    @Transactional
    public GameEntity startGame(Long gameId) {
        var gamer = GameRepository.findById(gameId);
        if (gamer == null || gamer.state != TodoState.STARTING) {
            return null;
        }
        if (gamer.players.size() < 2) {
            gamer.state = TodoState.FINISHED;
        }
        else {
            gamer.state = TodoState.RUNNING;
        }
        return gameModelToGameEntityConverter.convert(gamer);
    }

    public ArrayList<String> decode_map(List<String> map) {
        ArrayList<String> list = new ArrayList<String>();
        for (var index : map) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int k = 0; k < index.length(); k += 2) {
                for (int i = 0; i < Character.getNumericValue(index.charAt(k)); i++) {
                    stringBuilder.append(index.charAt(k + 1));
                }
            }
            list.add(stringBuilder.toString());
        }
        return list;
    }

    public boolean bool_move(Long gameId, Long playerId, int x, int y) {
        var game = GameRepository.findById(gameId);
        List<String> cur_map = decode_map(game.map);
        if (PlayerRepository.findById(playerId).posX != x && PlayerRepository.findById(playerId).posY != y) {
            return false;

        }
        if (cur_map.get(y).charAt(x) != 'G') {
            return false;
        }
        if (Math.abs(PlayerRepository.findById(playerId).posX - x) > 1 || Math.abs(PlayerRepository.findById(playerId).posY - y) > 1) {
            return false;
        }
        return true;
    }

    @ConfigProperty(name = "JWS_TICK_DURATION")
    int tick_duration;
    @ConfigProperty(name = "JWS_DELAY_MOVEMENT")
    int tick_delay;

    @Transactional
    public GameEntity move(Long gameId, Long playerId, int x, int y) throws exception_400, exception_404, exception_429 {
        var game = GameRepository.findById(gameId);
        var play_er = PlayerRepository.findById(playerId);

        if (game == null || gameId == null || playerId == null || play_er == null) {
            throw new exception_404();
        }
        if (play_er.lives <= 0 || game.state != TodoState.RUNNING || !bool_move(gameId, playerId, x, y)) {
            throw new exception_400();
        }
        if (play_er.lastmovement != null && play_er.lastmovement.plus((long) tick_duration * tick_delay, ChronoUnit.MILLIS).isAfter(LocalDateTime.now())) {
            throw new exception_429();
        }
        if (play_er.posX == x && play_er.posY == y) {
            throw new exception_429();
        }
        play_er.posX = x;
        play_er.posY = y;
        play_er.lastmovement = LocalDateTime.now();
        return gameModelToGameEntityConverter.convert(game);
    }

    @Transactional
    public List<String> new_map_created(List<String> map, StringBuilder stringBuilder, int posY) {
        List<String> new_curr_map = new ArrayList<String>();
        int index = 0;
        for (var value : map){
            if (index == posY) {
                new_curr_map.add(stringBuilder.toString());
            }
            else {
                new_curr_map.add(value);
            }
            index++;
        }
        return new_curr_map;
    }

    @Transactional
    public List<String> put_bomb(GameModel game, MoveRequestDTO requestDTO){
        List<String> temp_map = decode_map(game.map);
        StringBuilder stringBuilder = new StringBuilder(temp_map.get(requestDTO.posY));
        stringBuilder.setCharAt(requestDTO.posX, 'B');
        List<String> new_current_map = new_map_created(temp_map, stringBuilder, requestDTO.posY);
        return encode_map((ArrayList<String>) new_current_map);
    }
    @Transactional
    public ArrayList<String> encode_map(ArrayList<String> map) {
        ArrayList<String> new_current_map = new ArrayList<String>();
        int index_character;
        int s_line;
        for (var map_line : map) {
            StringBuilder encodedString = new StringBuilder();
            index_character = 0;
            while (index_character < map_line.length()) {
                var character = map_line.charAt(index_character);
                s_line = 0;
                while (index_character < map_line.length() && map_line.charAt(index_character) == character) {
                    s_line++;
                    index_character++;
                    if (s_line != 9){
                        continue;
                    }
                    break;
                }
                encodedString.append(s_line);
                encodedString.append(character);
            }
            new_current_map.add(encodedString.toString());
        }
        return new_current_map;
    }


    @ConfigProperty(name = "JWS_TICK_DURATION") int tick_dur;
    @ConfigProperty(name = "JWS_DELAY_BOMB") int delay_bomb;
    @Transactional
    public GameEntity bomb(Long gameId, Long playerId, MoveRequestDTO requestDTO) throws exception_400, exception_404, exception_429 {
        var game = GameRepository.findById(gameId);
        var player = PlayerRepository.findById(playerId);
        if (game == null || player == null) {
            throw new exception_404();
        }

        if (player.lastbomb != null && player.lastbomb.plus((long) tick_dur * delay_bomb, ChronoUnit.MILLIS).isAfter(LocalDateTime.now())){
            throw new exception_429();
        }
        if (requestDTO == null || player.name == null || player.lives <= 0 || game.state != TodoState.RUNNING || player.game != game || requestDTO.posX != player.posX || requestDTO.posY != player.posY) {
            throw new exception_400();
        }
        game.map = put_bomb(game, requestDTO);
        player.lastbomb = LocalDateTime.now();
        return gameModelToGameEntityConverter.convert(game);
    }

    @Transactional
    public List<String> map_explosion(GameModel game, MoveRequestDTO requestDTO, char block) {
        List<String> temp_map = decode_map(game.map);
        StringBuilder stringBuilder = new StringBuilder(temp_map.get(requestDTO.posY));
        stringBuilder.setCharAt(requestDTO.posX, block);
        List<String> new_current_map = new_map_created(temp_map, stringBuilder, requestDTO.posY);
        return encode_map((ArrayList<String>) new_current_map);
    }

    @Transactional
    public boolean condition_position_player_bomb(MoveRequestDTO requestDTO, PlayerModel playerModel){
        if ((requestDTO.posX == playerModel.posX && requestDTO.posY == playerModel.posY) || (requestDTO.posX + 1 == playerModel.posX && requestDTO.posY == playerModel.posY)
                || (requestDTO.posX - 1 == playerModel.posX && requestDTO.posY == playerModel.posY)
                || (requestDTO.posX == playerModel.posX && requestDTO.posY - 1 == playerModel.posY)
                || (requestDTO.posX == playerModel.posX && requestDTO.posY + 1 == playerModel.posY)){
            return true;
        }
        return false;
    }

    @Transactional
    public GameEntity bomb_explosion(Long gameId, MoveRequestDTO requestDTO) {
        GameModel game = GameRepository.findById(gameId);
        if (decode_map(game.map).get(requestDTO.posY).charAt(requestDTO.posX + 1) == 'W'){
            requestDTO.posX++;
            game.map = map_explosion(game, requestDTO, 'G');
            requestDTO.posX--;
        }
        if (decode_map(game.map).get(requestDTO.posY).charAt(requestDTO.posX - 1) == 'W'){
            requestDTO.posX--;
            game.map = map_explosion(game, requestDTO, 'G');
            requestDTO.posX++;
        }
        if (decode_map(game.map).get(requestDTO.posY + 1).charAt(requestDTO.posX) == 'W'){
            requestDTO.posY++;
            game.map = map_explosion(game, requestDTO, 'G');
            requestDTO.posY--;
        }
        if (decode_map(game.map).get(requestDTO.posY - 1).charAt(requestDTO.posX) == 'W'){
            requestDTO.posY--;
            game.map = map_explosion(game, requestDTO, 'G');
            requestDTO.posY++;
        }
        game.map = map_explosion(game, requestDTO, 'G');
        GameRepository.persist(game);
        int count = 0;
        for (var this_player : game.players) {
            if (condition_position_player_bomb(requestDTO, this_player)) {
                this_player.lives--;
                if (this_player.position == 1) {
                    this_player.posX = 1;
                    this_player.posY = 1;
                }
                if (this_player.position == 2) {
                    this_player.posX = 15;
                    this_player.posY = 1;
                }
                if (this_player.position == 3) {
                    this_player.posX = 15;
                    this_player.posY = 13;
                } else {
                    this_player.posX = 1;
                    this_player.posY = 13;
                }
            }
            if (this_player.lives != 0) {
                count++;
            }
        }
        if (count <= 1) {
            game.state = TodoState.FINISHED;
        }
        return  gameModelToGameEntityConverter.convert(game);
    }
}