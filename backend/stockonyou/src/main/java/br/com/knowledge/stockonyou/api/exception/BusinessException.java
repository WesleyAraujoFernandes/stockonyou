package br.com.knowledge.stockonyou.api.exception;

public class BusinessException extends RuntimeException  {
    public BusinessException(String message) {
        super(message);
    }
}
