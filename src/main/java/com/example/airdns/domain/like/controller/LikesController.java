package com.example.airdns.domain.like.controller;

import com.example.airdns.domain.like.dto.LikesResponseDto;
import com.example.airdns.domain.like.service.LikesService;
import com.example.airdns.global.common.dto.CommonResponse;
import com.example.airdns.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Like", description = "Like API")
@RequestMapping("/api/v1/rooms")
public class LikesController {

    private final LikesService likesService;

    @ApiResponses(value =  {
            @ApiResponse(responseCode = "200", description = "룸에 대한 좋아요 갯수 조회"),
    })
    @Operation(summary = "방에 대한 좋아요 갯수 조회", description = "방 전체 좋아요 조회를 한다")
    @GetMapping("/{roomsId}/likes")
    public ResponseEntity<CommonResponse<LikesResponseDto.ReadLikeResponseDto>> readRoomLike(
            @PathVariable Long roomsId){
        LikesResponseDto.ReadLikeResponseDto responseDto = likesService.readRoomLike(roomsId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new CommonResponse<>(HttpStatus.OK, "룸 좋아요 조회 성공", responseDto)
        );
    }

    @PostMapping("/{roomsId}/likes")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "방에 대한 좋아요 요청", description = "방 좋아요 요청한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "룸 좋아요 성공"),
            @ApiResponse(responseCode = "400", description = "해당 사용자는 좋아요를 이미 눌렀습니다.")
    })
    public ResponseEntity<CommonResponse<LikesResponseDto.CreateLikeResponseDto>> createLike(
            @PathVariable Long roomsId,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        LikesResponseDto.CreateLikeResponseDto responseDto = likesService.createLike(roomsId, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(
                new CommonResponse<>(HttpStatus.OK, "룸 좋아요 성공", responseDto)
        );
    }

    @DeleteMapping("/{roomsId}/likes/{likeId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "방에 대한 좋아요 취소 요청", description = "방 좋아요 취소를 요청한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "룸 좋아요 취소 성공"),
            @ApiResponse(responseCode = "400", description = "해당 사용자가 좋아요를 누르지 않았습니다."),
    })
    public ResponseEntity<CommonResponse<LikesResponseDto.DeleteLikeResponseDto>> deleteLike(
            @PathVariable Long roomsId,
            @PathVariable Long likeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        LikesResponseDto.DeleteLikeResponseDto responseDto = likesService.deleteLike(roomsId, likeId, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(
                new CommonResponse<>(HttpStatus.OK, "룸 좋아요 취소 성공", responseDto)
        );
    }
}
