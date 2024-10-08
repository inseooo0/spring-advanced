package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    public void 정상_삭제() {
        // given
        long commentId = 1;
        // when
        commentAdminService.deleteComment(commentId);
        //then
        verify(commentRepository).deleteById(commentId); // 행위 검증

    }

}