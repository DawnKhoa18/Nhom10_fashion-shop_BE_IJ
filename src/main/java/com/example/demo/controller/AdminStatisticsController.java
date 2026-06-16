package com.example.demo.controller;

import com.example.demo.dto.AdminRevenueDetailResponse;
import com.example.demo.dto.AdminStatisticsResponse;
import com.example.demo.dto.AdminStatusSummaryResponse;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping
    public AdminStatisticsResponse getStatistics(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = fromDate == null || fromDate.isBlank()
                ? currentMonth.atDay(1)
                : LocalDate.parse(fromDate);
        LocalDate endDate = toDate == null || toDate.isBlank()
                ? LocalDate.now()
                : LocalDate.parse(toDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Order> orders = orderRepository.findAll()
                .stream()
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> !order.getCreatedAt().isBefore(startDateTime) && !order.getCreatedAt().isAfter(endDateTime))
                .toList();

        BigDecimal revenue = orders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int customerCount = (int) orders.stream()
                .map(Order::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        int soldProducts = orderIds.isEmpty()
                ? 0
                : orderIds.stream()
                        .flatMap(orderId -> orderDetailRepository.findByOrderId(orderId).stream())
                        .map(OrderDetail::getQuantity)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();

        DateTimeFormatter detailFormatter = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, BigDecimal> revenueByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.mapping(Order::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, value -> value == null ? BigDecimal.ZERO : value, BigDecimal::add))
                ));

        List<AdminRevenueDetailResponse> revenueDetails = revenueByDate.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AdminRevenueDetailResponse(entry.getKey().format(detailFormatter), entry.getValue()))
                .toList();

        List<AdminStatusSummaryResponse> statusData = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus() == null ? "Không rõ" : order.getStatus(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> new AdminStatusSummaryResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new AdminStatisticsResponse(
                startDate.toString(),
                endDate.toString(),
                revenue,
                orders.size(),
                customerCount,
                soldProducts,
                revenueDetails,
                statusData
        );
    }
}
