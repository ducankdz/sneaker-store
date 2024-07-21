    package com.ecommerce.SneakerStore.entities;

    import com.fasterxml.jackson.annotation.JsonManagedReference;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDate;
    import java.util.List;

    @Entity
    @Table(name = "orders")
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @Column(name = "fullname",length = 100)
        private String fullName;

        @Column(name = "email",length = 100)
        private String email;

        @Column(name = "phone_number",nullable = false,length = 11)
        private String phoneNumber;

        @Column(name = "address",nullable = false,length = 200)
        private String address;

        @Column(name = "note",length = 200)
        private String note;

        @Column(name = "order_date")
        private LocalDate orderDate;

        @Column(name = "status")
        private String status;

        @Column(name = "payment_status")
        private String paymentStatus;

        @Column(name = "total_money")
        private Long totalMoney;

        @Column(name = "shipping_method")
        private String shippingMethod;

        @Column(name = "shipping_date")
        private LocalDate shippingDate;

        @Column(name = "payment_method")
        private String paymentMethod;

        @Column(name = "active")
        private Boolean active;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
        @JsonManagedReference
        private List<OrderDetail> orderDetails;

        @ManyToOne
        @JoinColumn(name = "coupon_id")
        private Coupon coupon;
    }
