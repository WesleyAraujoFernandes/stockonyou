package br.com.knowledge.stockonyou.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierDTO {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private String phone;
    private String cnpj;
}
