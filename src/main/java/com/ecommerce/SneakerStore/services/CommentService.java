package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.CommentDTO;
import com.ecommerce.SneakerStore.entities.Comment;
import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.User;
import com.ecommerce.SneakerStore.repositories.CommentRepository;
import com.ecommerce.SneakerStore.repositories.ProductRepository;
import com.ecommerce.SneakerStore.responses.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    public Comment addComment(CommentDTO commentDTO, String token) throws Exception {
        User user = userService.getUserFromToken(token);

        Product product = productRepository.findById(commentDTO.getProductId())
                .orElseThrow(() -> new Exception("Sản phẩm không tồn tại"));

        Comment comment = Comment
                .builder()
                .user(user)
                .product(product)
                .content(commentDTO.getContent())
                .star(commentDTO.getStar())
                .build();

        return commentRepository.save(comment);
    }

    public List<CommentResponse> getCommentsByProductId(Long productId){
        return commentRepository.findByProductId(productId)
                .stream()
                .map(CommentResponse::fromComment)
                .collect(Collectors.toList());
    }
    public CommentResponse getCommentById(Long id) throws Exception {
        Optional<Comment> optionalComment = commentRepository.findById(id);
        if (optionalComment.isEmpty()){
            throw new Exception("Đánh giá không tồn tại");
        }
        return CommentResponse.fromComment(optionalComment.get());
    }
    public Map<String, Object> getPositiveCommentsByMonth(){
        return Map.of(
                "months",List.of("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5",
                        "Tháng 6", "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"),
                "positiveComments",List.of(commentRepository.countPositiveCommentsByMonth(1),
                        commentRepository.countPositiveCommentsByMonth(2),
                        commentRepository.countPositiveCommentsByMonth(3),
                        commentRepository.countPositiveCommentsByMonth(4),
                        commentRepository.countPositiveCommentsByMonth(5),
                        commentRepository.countPositiveCommentsByMonth(6),
                        commentRepository.countPositiveCommentsByMonth(7),
                        commentRepository.countPositiveCommentsByMonth(8),
                        commentRepository.countPositiveCommentsByMonth(9),
                        commentRepository.countPositiveCommentsByMonth(10),
                        commentRepository.countPositiveCommentsByMonth(11),
                        commentRepository.countPositiveCommentsByMonth(12)));
    }

    public Map<String, Object> getNegativeCommentsByMonth(){
        return Map.of(
                "months",List.of("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5",
                        "Tháng 6", "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"),
                "negativeComments",List.of(commentRepository.countNegativeCommentsByMonth(1),
                        commentRepository.countNegativeCommentsByMonth(2),
                        commentRepository.countNegativeCommentsByMonth(3),
                        commentRepository.countNegativeCommentsByMonth(4),
                        commentRepository.countNegativeCommentsByMonth(5),
                        commentRepository.countNegativeCommentsByMonth(6),
                        commentRepository.countNegativeCommentsByMonth(7),
                        commentRepository.countNegativeCommentsByMonth(8),
                        commentRepository.countNegativeCommentsByMonth(9),
                        commentRepository.countNegativeCommentsByMonth(10),
                        commentRepository.countNegativeCommentsByMonth(11),
                        commentRepository.countNegativeCommentsByMonth(12)));
    }
}
