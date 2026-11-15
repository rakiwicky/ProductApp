package rw.kmp.product.data.mapper

import rw.kmp.product.data.dto.ProductDto
import rw.kmp.product.domain.model.Product

internal fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    imageUrl = image,
)
