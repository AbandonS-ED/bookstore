package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.AddressAddDTO;
import com.example.bookstore.dto.AddressUpdateDTO;
import com.example.bookstore.service.AddressService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.AddressVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/list")
    public Result<List<AddressVO>> list() {
        List<AddressVO> addresses = addressService.getList(AuthContext.getCurrentUserId());
        return Result.success(addresses);
    }

    @GetMapping("/{id}")
    public Result<AddressVO> getById(@PathVariable Long id) {
        AddressVO address = addressService.getById(AuthContext.getCurrentUserId(), id);
        return Result.success(address);
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody AddressAddDTO addressAddDTO) {
        addressService.add(AuthContext.getCurrentUserId(), addressAddDTO);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody AddressUpdateDTO addressUpdateDTO) {
        addressService.update(AuthContext.getCurrentUserId(), addressUpdateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(AuthContext.getCurrentUserId(), id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(AuthContext.getCurrentUserId(), id);
        return Result.success();
    }
}