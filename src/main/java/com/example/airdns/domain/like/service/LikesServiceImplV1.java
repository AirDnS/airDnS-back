package com.example.airdns.domain.like.service;

import com.example.airdns.domain.like.dto.LikesResponseDto;
import com.example.airdns.domain.like.entity.Likes;
import com.example.airdns.domain.like.exception.LikesExceptionCode;
import com.example.airdns.domain.like.exception.UserNotLikedException;
import com.example.airdns.domain.like.repository.LikesRepository;
import com.example.airdns.domain.room.entity.Rooms;
import com.example.airdns.domain.room.service.RoomsService;
import com.example.airdns.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikesServiceImplV1 implements LikesService {

    private final LikesRepository likesRepository;
    private final RoomsService roomsService;

    @Override
    @Transactional(readOnly = true)
    public List<LikesResponseDto.GetLikeResponseDto> getLikeList(Long roomsId, Users user){
        Rooms room = roomsService.findById(roomsId);

        List<Likes> likesList = likesRepository.findAllByRoomsId(roomsId);

        return likesList.stream()
                .map(like -> LikesResponseDto.GetLikeResponseDto.builder()
                        .roomName(room.getName())
                        .roomAddress(room.getAddress())
                        .nickName(user.getNickName())
                        .createdAt(like.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public LikesResponseDto.AddLikeResponseDto addLike(Long roomsId, Users user){
        Rooms room = roomsService.findById(roomsId);

        Likes likes = Likes.builder()
                .rooms(room)
                .users(user)
                .build();
        likesRepository.save(likes);

        return LikesResponseDto.AddLikeResponseDto.builder()
                .roomName(room.getName())
                .nickName(user.getNickName())
                .createdAt(likes.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public LikesResponseDto.DeleteLikeResponseDto cancelLike(Long roomsId, Users user) {
        Rooms room = roomsService.findById(roomsId);

        likesRepository.findByRoomsId(roomsId).orElseThrow(
                ()-> new UserNotLikedException(LikesExceptionCode.USER_NOT_LIKED)
        );

        likesRepository.deleteByRoomsId(roomsId);

        return LikesResponseDto.DeleteLikeResponseDto.builder()
                .roomName(room.getName())
                .nickName(user.getNickName())
                .build();
    }
}