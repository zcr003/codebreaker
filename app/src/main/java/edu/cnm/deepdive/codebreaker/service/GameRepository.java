package edu.cnm.deepdive.codebreaker.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import edu.cnm.deepdive.codebreaker.model.dao.GameDao;
import edu.cnm.deepdive.codebreaker.model.dao.GuessDao;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.model.pojo.GameWithGuesses;
import edu.cnm.deepdive.codebreaker.model.view.GameSummary;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class GameRepository {

  private final WebServiceProxy proxy;
  private final GameDao gameDao;
  private final GuessDao guessDao;


  public GameRepository() {
    proxy = WebServiceProxy.getInstance();
    CodebreakerDatabase database = CodebreakerDatabase.getInstance();
    gameDao = database.getGameDao();
    guessDao = database.getGuessDao();

  }

  //typical choreography for a reactive stream
  //capable of producing a Game object but not yet a method itself.
  public Single<Game> save(Game game) {
    return proxy
        .startGame(game)
        .map((startedGame) -> {
          int poolSize = (int) startedGame
              .getPool()
              .codePoints()
              .count();
          startedGame.setPoolSize(poolSize);
          return startedGame;
        })
        .subscribeOn(Schedulers.io());
  }

  public Single<Game> save(Game game, Guess guess) {
    //All this is only if the game is solved
    return proxy
        .submitGuess(guess, game.getServiceKey())
        .map((processedGuess) -> {
          game.getGuesses().add(processedGuess);
          game.setSolved(processedGuess.isSolution());
          return game;
        })
        .flatMap(this::insertGameWithGuesses)
        .subscribeOn(Schedulers.io());
  }

  //This method will give us a single game and all of its guesses at the same time .
  public LiveData<GameWithGuesses> get(long gameId) {
    return gameDao.select(gameId);
  }

  public LiveData<List<GameSummary>> getOrderedByGuessCount(int poolSize, int length) {
    return gameDao.selectSummariesByGuessCount(poolSize, length);

  }

  public LiveData<List<GameSummary>> getOrderedByTotalTime(int poolSize, int length) {
    return gameDao.selectSummariesByTotalTime(poolSize, length);

  }


  @NonNull
  private Single<Game> insertGameWithGuesses(Game game) {
    return (game.isSolved())
        //If game is solved we return everything after the ?
        ? gameDao
        .insert(game)
        .map((id) -> {
          game.setId(id);
          for (Guess guess : game.getGuesses()) {
            guess.setGameId(id);
          }
          return game;
        })
        .flatMap((g2) -> guessDao
            .insert(g2.getGuesses())
            // TODO invoke Guess.setId for all of the guesses.
            .map((ids) -> g2))
        //If not solved we return everything after this :
        : Single.just(game);
  }

}
