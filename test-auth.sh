#!/bin/bash

# Test frontend GraphQL proxy with session authentication
echo "Testing frontend authentication integration..."

# First, let's test the frontend GraphQL endpoint directly
echo "1. Testing frontend /graphql proxy..."
curl -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $GRAPHQL_ACCESS_TOKEN" \
  -d '{
    "query": "mutation AddProductVariant($productId: ID!, $input: ProductVariantInput!) { addProductVariant(productId: $productId, input: $input) { id title sku price { amount currency } createdAt } }",
    "variables": {
      "productId": "Z2lkOi8vU2ltcGxlQ29tbWVyY2UvUHJvZHVjdC82ZTY2MjZhYy0yNzZkLTQ1YTItYTMwNi01YjgwYzlmNDcwNGE=",
      "input": {
        "title": "Frontend Test Edition",
        "sku": "FRONTEND-TEST-001",
        "price": {
          "amount": 299.99,
          "currency": "USD"
        }
      }
    }
  }' \
  http://localhost:5173/graphql 2>/dev/null

echo -e "\n\n2. Testing backend /graphql directly..."
curl -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $GRAPHQL_ACCESS_TOKEN" \
  -d '{
    "query": "mutation AddProductVariant($productId: ID!, $input: ProductVariantInput!) { addProductVariant(productId: $productId, input: $input) { id title sku price { amount currency } createdAt } }",
    "variables": {
      "productId": "Z2lkOi8vU2ltcGxlQ09tbWVyY2UvUHJvZHVjdC82ZTY2MjZhYy0yNzZkLTQ1YTItYTMwNi01YjgwYzlmNDcwNGE=",
      "input": {
        "title": "Backend Test Edition",
        "sku": "BACKEND-TEST-001",
        "price": {
          "amount": 399.99,
          "currency": "USD"
        }
      }
    }
  }' \
  http://localhost:8080/graphql 2>/dev/null

echo -e "\n\nTesting complete!"
