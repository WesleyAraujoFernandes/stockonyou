package br.com.knowledge.stockonyou.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.knowledge.stockonyou.dto.SupplierDTO;
import br.com.knowledge.stockonyou.dto.request.SupplierRequest;
import br.com.knowledge.stockonyou.dto.response.SupplierResponse;
import br.com.knowledge.stockonyou.mapper.SupplierMapper;
import br.com.knowledge.stockonyou.model.Supplier;
import br.com.knowledge.stockonyou.service.SupplierService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;
    private final SupplierMapper mapper;

    @GetMapping("/{id}")
    public SupplierResponse getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.findById(id);
        return mapper.toResponse(supplier);
    }

    @GetMapping
    public List<SupplierResponse> getAllSuppliers() {
        return supplierService.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping
    public SupplierResponse createSupplier(@RequestBody SupplierRequest supplierDTO) {
        Supplier saved = supplierService.create(supplierDTO);
        return mapper.toResponse(saved);
    }

    @PutMapping("/{id}")
    public SupplierDTO updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO supplierDTO) {
        Supplier existing = supplierService.findById(id);

        Supplier updated = SupplierMapper.INSTANCE.toEntity(supplierDTO);
        updated.setId(existing.getId()); // mantém o ID original

        Supplier saved = supplierService.save(updated);
        return SupplierMapper.INSTANCE.toDTO(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
    }
}
