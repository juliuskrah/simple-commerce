package com.simplecommerce.actor;

import com.simplecommerce.actor.bot.BotEntity;
import com.simplecommerce.actor.user.UserEntity;
import com.simplecommerce.actor.user.Users;
import com.simplecommerce.shared.exceptions.NotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Actor management related classes and services.
 *
 * @author julius.krah
 */
@Transactional
@Configurable(autowire = Autowire.BY_TYPE)
public class ActorManagement implements ActorService {

  public void setActorRepository(ObjectFactory<Actors> actorRepository) {
    this.actorRepository = actorRepository.getObject();
  }

  public void setUserRepository(ObjectFactory<Users> userRepository) {
    this.userRepository = userRepository.getObject();
  }

  private Actors actorRepository;
  private Users userRepository;

  private Actor fromEntity(ActorEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return switch (entity) {
      case UserEntity user -> fromEntity(user);
      case BotEntity bot -> new Bot(
          bot.getId().toString(),
          bot.getUsername(),
          bot.getLastModifiedDate().orElseGet(epoch),
          bot.getCreatedDate().orElseGet(epoch)
      );
      default -> throw new IllegalStateException("Unknown actor entity type: " + entity.getClass());
    };
  }

  private User fromEntity(UserEntity entity) {
    Supplier<OffsetDateTime> epoch = () -> OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    return new User(
        entity.getId().toString(),
        entity.getUsername(),
        entity.getLastModifiedDate().orElseGet(epoch),
        entity.getCreatedDate().orElseGet(epoch),
        entity.getLastLogin(),
        entity.getEmail()
    );
  }

  @Override
  public Optional<Actor> findActor(String username) {
    return actorRepository.findByUsername(username).map(this::fromEntity);
  }

  @Override
  public User findUser(String username) {
    return userRepository.findByUsername(username).map(this::fromEntity).orElseThrow(NotFoundException::new);
  }
}
