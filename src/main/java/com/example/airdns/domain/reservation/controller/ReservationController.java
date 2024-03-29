package com.example.airdns.domain.reservation.controller;

import com.example.airdns.domain.reservation.dto.ReservationRequestDto;
import com.example.airdns.domain.reservation.dto.ReservationResponseDto;
import com.example.airdns.domain.reservation.servicefacade.ReservationServiceFacade;
import com.example.airdns.global.common.dto.CommonResponse;
import com.example.airdns.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "reservation", description = "Reservation API")
@SecurityRequirement(name = "Bearer Authentication")
public class ReservationController {

    private final ReservationServiceFacade reservationServiceFacade;

    @PostMapping("/rooms/{roomsId}/reservation")
    @Operation(summary = "예약 생성", description = "해당 방에 대해 예약을 한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "예약 성공"),
            @ApiResponse(responseCode = "400", description = "예약 시간 입력값이 잘못 입력 되었습니다."),
            @ApiResponse(responseCode = "400", description = "해당 예약 시간에 예약을 못합니다.")
    })
    public ResponseEntity<CommonResponse> createReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long roomsId,
            @Valid @RequestBody ReservationRequestDto.CreateReservationRequestDto createReservation) {
        ReservationResponseDto.CreateReservationResponseDto responseDto = reservationServiceFacade.createReservation(
                userDetails.getUser().getId(),
                roomsId,
                createReservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CommonResponse<>(
                        HttpStatus.CREATED,
                        "예약에 성공했습니다.",
                        responseDto
                )
        );
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "예약 단건 조회", description = "해당 예약을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해당 예약 조회에 성공",
                    content = {@Content(schema = @Schema(implementation = ReservationResponseDto.ReadReservationResponseDto.class))}),
            @ApiResponse(responseCode = "404", description = "해당 예약이 없습니다."),
            @ApiResponse(responseCode = "403", description = "해당 유저가 예약한 것이 아닙니다.")
    })
    public ResponseEntity<CommonResponse> readReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long reservationId) {
        ReservationResponseDto.ReadReservationResponseDto reservationResponseDto = reservationServiceFacade.readReservation(
                userDetails.getUser().getId(),
                reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new CommonResponse(HttpStatus.OK,
                        "해당 예약 조회 성공",
                        reservationResponseDto)
        );
    }

    @GetMapping("/reservation")
    @Operation(summary = "유저 예약 전체 조회", description = "유저의 예약 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "예약 목록 조회에 성공")
    public ResponseEntity<CommonResponse> readReservationList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReservationResponseDto.ReadReservationResponseDto> reservationResponseDtoList = reservationServiceFacade.readReservationList(
                userDetails.getUser().getId(),
                pageable
        );
        return ResponseEntity.status(HttpStatus.OK).body(
                new CommonResponse(HttpStatus.OK,
                        "유저 예약 목록 조회 성공",
                        reservationResponseDtoList)
        );
    }

    @GetMapping("/reservation/rooms/{roomsId}")
    @Operation(summary = "해당 방에 대한 예약 목록을 조회한다", description = "해당 방에 대한 예약 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "해당 방에 대한 예약 목록 조회에 성공")
    public ResponseEntity<CommonResponse> readRoomReservationList(
            @PathVariable Long roomsId,
            @PageableDefault(sort = "checkIn") Pageable pageable
    ) {
        Page<ReservationResponseDto.ReadReservationResponseDto> reservationResponseDtoList = reservationServiceFacade.readRoomReservationList(
                roomsId,
                pageable);
        return ResponseEntity.status(HttpStatus.OK).body(
                new CommonResponse(HttpStatus.OK,
                        "방 예약 목록 조회 성공",
                        reservationResponseDtoList)
        );
    }


    @DeleteMapping("/reservation/{reservationId}")
    @Operation(summary = "해당 예약 취소", description = "해당 예약을 취소.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해당 예약 성공"),
            @ApiResponse(responseCode = "403", description = "해당 유저가 예약한 것이 아닙니다.")
    })
    public ResponseEntity<CommonResponse> deleteReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long reservationId
    ) {
        reservationServiceFacade.deleteReservation(
                userDetails.getUser().getId(),
                reservationId
        );
        return ResponseEntity.status(HttpStatus.OK).body(
                new CommonResponse<>(
                        HttpStatus.OK,
                        "예약 취소 성공"
                )
        );
    }
}
