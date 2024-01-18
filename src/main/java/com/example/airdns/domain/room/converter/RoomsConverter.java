package com.example.airdns.domain.room.converter;

import com.example.airdns.domain.equipment.dto.EquipmentsResponseDto;
import com.example.airdns.domain.equipment.entity.Equipments;
import com.example.airdns.domain.equipmentcategory.entity.EquipmentCategories;
import com.example.airdns.domain.image.entity.Images;
import com.example.airdns.domain.room.dto.RoomsRequestDto;
import com.example.airdns.domain.room.dto.RoomsResponseDto;
import com.example.airdns.domain.room.dto.RoomsSearchConditionDto;
import com.example.airdns.domain.room.entity.Rooms;
import com.example.airdns.domain.user.entity.Users;

import java.util.*;
import java.util.stream.Collectors;

public class RoomsConverter {

    public static Rooms toEntity(RoomsRequestDto.CreateRoomsRequestDto requestDto, Users users) {
        return Rooms.builder()
                .users(users)
                .price(requestDto.getPrice())
                .address(requestDto.getAddress())
                .size(requestDto.getSize())
                .description(requestDto.getDesc())
                .name(requestDto.getName())
                .build();
    }

    public static RoomsResponseDto.ReadRoomsResponseDto toDto(Rooms rooms) {
        List<Map<String, Object>> equipments = getEquipmentListByRooms(rooms);

        return RoomsResponseDto.ReadRoomsResponseDto.builder()
                .roomsId(rooms.getId())
                .name(rooms.getName())
                .price(rooms.getPrice())
                .address(rooms.getAddress())
                .size(rooms.getSize())
                .desc(rooms.getDescription())
                .equipment(equipments)
                .imageUrl(
                        rooms.getImagesList().stream()
                            .map((Images::getImageUrl))
                            .toList()
                )
                .reservatedTimeList(
                        rooms.getReservationList().stream()
                                .map(reservation -> Arrays.asList(
                                        reservation.getCheckIn(), reservation.getCheckOut()
                                ))
                                .toList()
                )
                .build();
    }

    public static RoomsResponseDto.UpdateRoomsImagesResponseDto toImagesDto(Rooms rooms) {
        return RoomsResponseDto.UpdateRoomsImagesResponseDto.builder()
                .imageUrl(
                        rooms.getImagesList().stream()
                                .map((Images::getImageUrl))
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static RoomsSearchConditionDto toRoomsSearchCondition(RoomsRequestDto.ReadRoomsListRequestDto requestDto) {
        return RoomsSearchConditionDto.builder()
                .keyword(requestDto.getKeyword())
                .startPrice(requestDto.getPrice() != null ? requestDto.getPrice().get(0) : null)
                .endPrice(requestDto.getPrice() != null ? requestDto.getPrice().get(1) : null)
                .startSize(requestDto.getSize() != null ? requestDto.getSize().get(0) : null)
                .endSize(requestDto.getSize() != null ? requestDto.getSize().get(1) : null)
                .equpmentList(requestDto.getEquipment())
                .build();
    }

    private static List<Map<String, Object>> getEquipmentListByRooms(Rooms rooms) {
        List<Map<String, Object>> equipments = new ArrayList<>();

        // map (CategoryId, equipment) 생성
        Map<EquipmentCategories, List<EquipmentsResponseDto.ReadEquipmentsResponseDto>> equipmentsMap = new HashMap<>();

        rooms.getRoomEquipmentsList()
                .forEach((roomEquipments) -> {
                    Equipments _equipments = roomEquipments.getEquipments();
                    EquipmentCategories category = _equipments.getEquipmentCategories();
                    List<EquipmentsResponseDto.ReadEquipmentsResponseDto> responseList =
                            equipmentsMap.getOrDefault(category, new ArrayList<>());

                    responseList.add(
                            EquipmentsResponseDto.ReadEquipmentsResponseDto.builder()
                                    .id(_equipments.getId())
                                    .name(_equipments.getName())
                                    .build()
                    );

                    equipmentsMap.put(category, responseList);
                });

        equipmentsMap.keySet().stream().sorted(Comparator.comparingLong(EquipmentCategories::getId))
                .forEach( category -> {
                            Map<String, Object> categoryResponseMap = new HashMap<>();
                            categoryResponseMap.put("label", category.getName());
                            categoryResponseMap.put("options", equipmentsMap.get(category));
                            equipments.add(categoryResponseMap);
                        }
                );

        return equipments;
    }
}
