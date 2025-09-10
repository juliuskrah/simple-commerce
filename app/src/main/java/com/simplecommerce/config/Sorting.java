package com.simplecommerce.config;

import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.graphql.data.query.AbstractSortStrategy;
import org.springframework.stereotype.Component;

/**
 * Sorting strategy for products.
 *
 * @author julius.krah
 * @since 1.0
 */
@Component
public class Sorting extends AbstractSortStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(Sorting.class);

  @Override
  protected List<String> getProperties(DataFetchingEnvironment environment) {
    List<String> properties = environment.getArgument("orderBy");
    LOG.debug("Using sorting properties: {}", properties);
    return properties;
  }

  @Override
  protected Direction getDirection(DataFetchingEnvironment environment) {
    String directionString = environment.getArgument("orderDirection");
    Direction direction = Direction.ASC;
    if (directionString != null) {
      direction = Enum.valueOf(Direction.class, directionString);
    }
    LOG.debug("Using sorting direction: {}", direction);
    return direction;
  }
}
