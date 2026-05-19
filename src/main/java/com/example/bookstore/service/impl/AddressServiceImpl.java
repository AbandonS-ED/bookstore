package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.common.Constants;
import com.example.bookstore.dto.AddressAddDTO;
import com.example.bookstore.dto.AddressUpdateDTO;
import com.example.bookstore.entity.Address;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.AddressMapper;
import com.example.bookstore.service.AddressService;
import com.example.bookstore.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public void add(Long userId, AddressAddDTO addressAddDTO) {
        Integer isDefault = addressAddDTO.getIsDefault();
        if (Objects.equals(isDefault, Constants.ADDRESS_DEFAULT)) {
            clearDefault(userId);
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(addressAddDTO.getReceiverName());
        address.setPhone(addressAddDTO.getPhone());
        address.setProvince(addressAddDTO.getProvince());
        address.setCity(addressAddDTO.getCity());
        address.setDistrict(addressAddDTO.getDistrict());
        address.setDetailAddress(addressAddDTO.getDetailAddress());
        address.setIsDefault(isDefault != null ? isDefault : Constants.ADDRESS_NOT_DEFAULT);
        addressMapper.insert(address);
    }

    @Override
    public void update(Long userId, AddressUpdateDTO addressUpdateDTO) {
        Address address = addressMapper.selectById(Long.parseLong(addressUpdateDTO.getId()));
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(1, "收货地址不存在");
        }

        Integer updateDefault = addressUpdateDTO.getIsDefault();
        if (Objects.equals(updateDefault, Constants.ADDRESS_DEFAULT)) {
            clearDefault(userId);
        }

        address.setReceiverName(addressUpdateDTO.getReceiverName());
        address.setPhone(addressUpdateDTO.getPhone());
        address.setProvince(addressUpdateDTO.getProvince());
        address.setCity(addressUpdateDTO.getCity());
        address.setDistrict(addressUpdateDTO.getDistrict());
        address.setDetailAddress(addressUpdateDTO.getDetailAddress());
        if (updateDefault != null) {
            address.setIsDefault(updateDefault);
        }
        addressMapper.updateById(address);
    }

    @Override
    public void delete(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(1, "收货地址不存在");
        }
        addressMapper.deleteById(addressId);
    }

    @Override
    public void setDefault(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(1, "收货地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(Constants.ADDRESS_DEFAULT);
        addressMapper.updateById(address);
    }

    @Override
    public List<AddressVO> getList(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId).orderByDesc(Address::getIsDefault).orderByDesc(Address::getCreateTime);
        List<Address> addresses = addressMapper.selectList(wrapper);
        return addresses.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public AddressVO getById(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(1, "收货地址不存在");
        }
        return convertToVO(address);
    }

    private void clearDefault(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId).eq(Address::getIsDefault, Constants.ADDRESS_DEFAULT);
        Address defaultAddress = addressMapper.selectOne(wrapper);
        if (defaultAddress != null) {
            defaultAddress.setIsDefault(Constants.ADDRESS_NOT_DEFAULT);
            addressMapper.updateById(defaultAddress);
        }
    }

    private AddressVO convertToVO(Address address) {
        AddressVO vo = new AddressVO();
        vo.setId(address.getId());
        vo.setUserId(address.getUserId());
        vo.setReceiverName(address.getReceiverName());
        vo.setPhone(address.getPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setIsDefault(address.getIsDefault());
        vo.setCreateTime(address.getCreateTime());
        vo.setUpdateTime(address.getUpdateTime());
        return vo;
    }
}