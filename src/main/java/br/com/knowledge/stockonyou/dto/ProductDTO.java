package br.com.knowledge.stockonyou.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    private Long id;
    private String name;
    private String category;
    private String unit;
    private Double purchasePrice;
    private Double salePrice;
    private Integer stockQuantity;
    private Integer minimumStock;

}