package com.simplecommerce.product;

/**
 * Input record for category operations.
 * 
 * @author julius.krah
 */
record CategoryInput(
    String title,
    String slug,
    String description,
    String parentId
) {

}