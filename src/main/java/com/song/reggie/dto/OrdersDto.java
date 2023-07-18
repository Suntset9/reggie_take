package com.song.reggie.dto;


import com.song.reggie.entity.OrderDetail;
import com.song.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

    private int sumNum;
	
}
