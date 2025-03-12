    package com.example.DATN_Fashion_Shop_BE.model;
    
    import jakarta.persistence.*;
    import lombok.*;
    import lombok.extern.apachecommons.CommonsLog;
    
    import java.util.Date;
    
    @Entity
    @Table(name = "payment")
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class Payment {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        @ManyToOne(cascade = CascadeType.ALL)
        @JoinColumn(name = "order_id", nullable = false)
        private Order order;
    
        @ManyToOne
        @JoinColumn(name = "payment_method_id", nullable = false)
        private PaymentMethod paymentMethod;
    
        @Column(name ="payment_date", nullable = false)
        private Date paymentDate;
    
        @Column(name = "amount", nullable = false)
        private Double amount;
    
        @Column(name="status",columnDefinition = "NVARCHAR(255)", nullable = false)
        private String status;
    
        @Column(name="transaction_code", nullable = false)
        private String transactionCode;
    
    
    }
