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
    public SupplierResponse createSupplier(@RequestBody SupplierRequest supplierRequest) {
        Supplier saved = supplierService.create(supplierRequest);
        return mapper.toResponse(saved);
    }

    @PutMapping("/{id}")
    public SupplierResponse updateSupplier(@PathVariable Long id, @RequestBody SupplierRequest supplierRequest) {
        Supplier existing = supplierService.findById(id);
        if (existing.getId() == null) {
            throw new RuntimeException("Supplier not found with id " + id);
        }
        Supplier updated = supplierService.update(id, supplierRequest);
        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
    }
}
