package com.simplecommerce.shared.types;

import com.simplecommerce.shared.authorization.BasePermissions;
import java.util.List;

public record Role(String name, List<BasePermissions> permissions) {

}
