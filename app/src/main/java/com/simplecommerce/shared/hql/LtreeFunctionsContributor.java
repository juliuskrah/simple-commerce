package com.simplecommerce.shared.hql;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

/**
 * @author julius.krah
 */
public class LtreeFunctionsContributor implements FunctionContributor {

  @Override
  public void contributeFunctions(FunctionContributions functionContributions) {
    var registry = functionContributions.getFunctionRegistry();
    registry.register("ancestorsof", new AncestorsOfSQLFunction("ancestorsof"));
    registry.register("descendantsof", new DescendantsOfSQLFunction("descendantsof"));
  }
}
