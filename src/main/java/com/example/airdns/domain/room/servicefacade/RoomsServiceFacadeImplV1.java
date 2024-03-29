package com.example.airdns.domain.room.servicefacade;

import com.example.airdns.domain.deleteinfo.service.DeleteInfoService;
import com.example.airdns.domain.equipment.service.EquipmentsService;
import com.example.airdns.domain.image.entity.Images;
import com.example.airdns.domain.image.service.ImagesService;
import com.example.airdns.domain.payment.service.PaymentService;
import com.example.airdns.domain.reservation.service.ReservationService;
import com.example.airdns.domain.restschedule.service.RestScheduleService;
import com.example.airdns.domain.room.converter.RoomsConverter;
import com.example.airdns.domain.room.dto.RoomsRequestDto.*;
import com.example.airdns.domain.room.dto.RoomsResponseDto;
import com.example.airdns.domain.room.dto.RoomsResponseDto.ReadRoomsListContentDto;
import com.example.airdns.domain.room.dto.RoomsResponseDto.ReadRoomsResponseDto;
import com.example.airdns.domain.room.dto.RoomsResponseDto.UpdateRoomsImagesResponseDto;
import com.example.airdns.domain.room.dto.RoomsResponseDto.ReadRoomsListResponseDto;
import com.example.airdns.domain.room.entity.Rooms;
import com.example.airdns.domain.room.exception.RoomsCustomException;
import com.example.airdns.domain.room.exception.RoomsExceptionCode;
import com.example.airdns.domain.room.service.RoomsService;
import com.example.airdns.domain.roomequipment.service.RoomEquipmentsService;
import com.example.airdns.domain.user.entity.Users;
import com.example.airdns.domain.user.enums.UserRole;
import com.example.airdns.global.address.AddressUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoomsServiceFacadeImplV1 implements RoomsServiceFacade {

    private final RoomsService roomsService;
    private final ImagesService imagesService;
    private final RoomEquipmentsService roomEquipmentsService;
    private final RestScheduleService restScheduleService;
    private final EquipmentsService equipmentsService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final DeleteInfoService deleteInfoService;

    @Transactional
    @Override
    public ReadRoomsResponseDto createRooms(
            CreateRoomsRequestDto requestDto,
            List<MultipartFile> files,
            Users users) {
        if (users.getRole() != UserRole.HOST && users.getRole() != UserRole.ADMIN) {
            throw new RoomsCustomException(RoomsExceptionCode.NO_PERMISSION_USER);
        }

        Rooms rooms = RoomsConverter.toEntity(requestDto, users);
        roomsService.save(rooms);

        updateEquipments(rooms, requestDto.getEquipment());
        uploadImages(rooms, files);

        return RoomsConverter.toDto(rooms);
    }

    @Override
    public ReadRoomsResponseDto readRooms(Long roomsId) {
        return RoomsConverter.toDto(roomsService.findById(roomsId));
    }

    @Override
    public ReadRoomsListResponseDto readRoomsList(
            ReadRoomsListRequestDto requestDto) {

        Integer searchLevel = requestDto.getSearchLevel();

        List<ReadRoomsListContentDto> roomsList;
        if (searchLevel != null) {
            Double searchDistance = Math.pow(2, searchLevel - 7) * 10;
            searchDistance = searchDistance < 100 ? searchDistance : 100.0;

            roomsList = roomsService.findAllSearchFilter(
                    RoomsConverter.toRoomsSearchCondition(requestDto, searchDistance));

            // 데이터가 나올때까지 검색
            while (roomsList.isEmpty() && searchDistance < 100) {
                searchDistance *= 2;
                searchLevel++;

                roomsList = roomsService.findAllSearchFilter(
                        RoomsConverter.toRoomsSearchCondition(requestDto, searchDistance));
            }

        } else {
            roomsList = roomsService.findAllSearchFilter(
                    RoomsConverter.toRoomsSearchCondition(requestDto));
        }

        List<Long> roomsIdList = roomsList.stream()
                .map(ReadRoomsListContentDto::getRoomsId)
                .toList();

        Map<Long, List<String>> imagesMap = imagesService.findAllByRoomsId(roomsIdList);

        for (ReadRoomsListContentDto result : roomsList) {
            result.setImage(
                    imagesMap.get(result.getRoomsId()) != null
                            ? imagesMap.get(result.getRoomsId()).get(0)
                            : null
            );
        }

        if (requestDto.getSearchLevel() != null
                && requestDto.getLatitude() != null
                && requestDto.getLongitude() != null) {
            for (ReadRoomsListContentDto result : roomsList) {
                if (result.getLatitude() != null) {
                    result.setDistance(
                            AddressUtil.distance(
                                    result.getLatitude(), requestDto.getLatitude(),
                                    result.getLongitude(), requestDto.getLongitude()
                            )
                    );

                }
            }

            roomsList.sort(Comparator.comparing(ReadRoomsListContentDto::getDistance));
        }

        return RoomsResponseDto.ReadRoomsListResponseDto.builder()
                .content(roomsList)
                .searchLevel(searchLevel)
                .build();
    }

    @Override
    public Page<ReadRoomsResponseDto> readRoomsListByHost(
            Pageable pageable,
            ReadRoomsListByHostRequestDto requestDto,
            Users users) {
        if (users.getRole() != UserRole.HOST && users.getRole() != UserRole.ADMIN) {
            throw new RoomsCustomException(RoomsExceptionCode.NO_PERMISSION_USER);
        }

        return roomsService.findAllByHost(pageable, RoomsConverter.toRoomsSearchCondition(requestDto, users));
    }

    @Transactional
    @Override
    public ReadRoomsResponseDto updateRooms(
            UpdateRoomsRequestDto requestDto,
            Long roomsId,
            Users users) {
        Rooms rooms = roomsService.findById(roomsId);

        verifyUserHasThisRooms(rooms, users);

        roomEquipmentsService.deleteAll(rooms.getRoomEquipmentsList());
        rooms.resetEquipments();

        updateEquipments(rooms, requestDto.getEquipment());

        rooms.updateRooms(
                requestDto.getName(),
                requestDto.getPrice(),
                requestDto.getAddress(),
                requestDto.getSize(),
                requestDto.getDesc()
        );

        return RoomsConverter.toDto(rooms);
    }

    @Transactional
    @Override
    public void updateRoomsIsClosed(
            UpdateRoomsIsClosedRequestDto requestDto,
            Long roomsId,
            Users users) {
        Rooms rooms = roomsService.findById(roomsId);

        verifyUserHasThisRooms(rooms, users);

        rooms.updateIsClosed(requestDto.getIsClosed());
    }

    @Transactional
    @Override
    public UpdateRoomsImagesResponseDto updateRoomsImages(
            UpdateRoomsImagesRequestDto requestDto,
            Long roomsId,
            List<MultipartFile> files,
            Users users) {
        Rooms rooms = roomsService.findById(roomsId);

        verifyUserHasThisRooms(rooms, users);

        if (requestDto != null) {
            deleteImage(rooms, requestDto.getRemoveImages());
        }

        if (files != null) {
            uploadImages(rooms, files);
        }

        return RoomsConverter.toImagesDto(rooms);

    }

    @Override
    public void deleteRooms(Long roomsId, Users users) {
        Rooms rooms = roomsService.findById(roomsId);

        verifyUserHasThisRooms(rooms, users);

        roomsService.delete(rooms);
    }


    @Override
    public Page<RoomsResponseDto.ReadRoomsRestScheduleResponseDto> ReadRoomsRestSchedule(
            Pageable pageable, Long roomsId, Users users) {
        Rooms rooms = roomsService.findById(roomsId);

        verifyUserHasThisRooms(rooms, users);

        return restScheduleService.readRestSchedule(pageable, rooms)
                .map(RoomsConverter::toDto);
    }

    @Transactional
    @Override
    public void CreateRoomsRestSchedule(
            CreateRoomsRestScheduleRequestDto requestDto,
            Long roomsId,
            Users users) {
        Rooms rooms = roomsService.findById(roomsId);

        verifyUserHasThisRooms(rooms, users);

        if (reservationService.isReserved(rooms, requestDto.getStartDate(), requestDto.getEndDate())) {
            throw new RoomsCustomException(RoomsExceptionCode.EXIST_RESERVATION);
        }

        rooms.addRestSchedule(
                restScheduleService.createRestSchedule(
                        rooms, requestDto.getStartDate(), requestDto.getEndDate()
                )
        );
    }

    @Transactional
    @Override
    public void DeleteRoomsRestSchedule(
            Long roomsId,
            Long restscheduleId,
            Users users) {

        Rooms rooms = roomsService.findById(roomsId);

        verifyUserHasThisRooms(rooms, users);

        restScheduleService.deleteRestSchedule(restscheduleId, rooms);
    }

    private void verifyUserHasThisRooms(Rooms rooms, Users users) {
        if (!rooms.getUsers().getId().equals(users.getId())) {
            throw new RoomsCustomException(RoomsExceptionCode.NO_PERMISSION_USER);
        }
    }

    private void updateEquipments(Rooms rooms, List<Long> equipments) {
        equipments.stream().distinct().forEach(
                equipment -> rooms.addEquipments(
                        roomEquipmentsService.createRoomEquipments(
                                rooms, equipmentsService.findById(equipment)
                        )
                )
        );
    }

    private void uploadImages(Rooms rooms, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file == null) continue;

            //TODO 롤백 시 이미지 제거 (선택1: 롤백 로직 추가, 선택2: 배치 시스템 구성)
            rooms.addImage(imagesService.createImages(rooms, file));
        }
    }

    private void deleteImage(Rooms rooms, List<Long> removeImages) {
        if (!rooms.getImagesList().stream().map(Images::getId).toList()
                .containsAll(removeImages)) {
            throw new RoomsCustomException(RoomsExceptionCode.IMAGES_NOT_EXIST);
        }

        removeImages.forEach(images -> imagesService.deleteImages(images, rooms));
    }

    @Override
    public void deleteRooms(LocalDateTime deleteTime){
        // 삭제할 Rooms의 ID 조회
        List<Long> roomIds = roomsService.findRoomIds(deleteTime);
        for (Long roomId : roomIds) {
            // 연관된 Reservations, Payments의 ID 조회
            List<Long> reservationIds = reservationService.findReservationIdsByRoomId(roomId);
            List<Long> paymentIds = paymentService.findPaymentIdsByReservationIds(reservationIds);

            // DeleteInfo 저장
            saveDeleteRoomInfo(roomId);

            // 예약이 없으면 결제는 없다. 예약이 있으면 결제는 있을 수 있고 없을 수도 있다.
            if(!reservationIds.isEmpty() && !paymentIds.isEmpty()){
                // 예약이 존재
                reservationIds.forEach(reservationId -> reservationService.saveDeletedReservationInfo(reservationId));
                paymentIds.forEach(paymentId -> paymentService.saveDeletedPaymentInfo(paymentId));
                // 연관된 엔터티 삭제
                paymentService.deleteByRoomId(roomId);
                reservationService.deleteByRoomId(roomId);
            }
            // 마지막으로 Rooms 삭제
            roomsService.deleteRoomInfo(roomId);
        }
    }

    private void saveDeleteRoomInfo(Long roomId){
        Rooms room = roomsService.findByIdAndIsDeletedTrue(roomId);
        deleteInfoService.saveDeletedRoomsInfo(room);
    }
}
