package com.example.bookstore.service;

import com.example.bookstore.dto.AddressAddDTO;
import com.example.bookstore.dto.AddressUpdateDTO;
import com.example.bookstore.vo.AddressVO;

import java.util.List;

public interface AddressService {

    void add(Long userId, AddressAddDTO addressAddDTO);

    void update(Long userId, AddressUpdateDTO addressUpdateDTO);

    void delete(Long userId, Long addressId);

    void setDefault(Long userId, Long addressId);

    List<AddressVO> getList(Long userId);

    AddressVO getById(Long userId, Long addressId);
}