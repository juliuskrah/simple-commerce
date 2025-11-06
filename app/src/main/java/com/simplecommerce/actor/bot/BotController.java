package com.simplecommerce.actor.bot;

import com.simplecommerce.actor.Bot;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/// @author julius.krah
@Controller
class BotController {

  @MutationMapping
  Bot addBot(@Argument CreateBotInput input) {
    // Implementation for creating a bot goes here
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
