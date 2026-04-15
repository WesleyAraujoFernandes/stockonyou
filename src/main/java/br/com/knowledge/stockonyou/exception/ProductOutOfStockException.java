package br.com.knowledge.stockonyou.exception;

public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String message) {
        super(message);
    }
}
