package com.mykovolod.mando.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;


@Data
@RequiredArgsConstructor
public class BaseAudiEntity {

    @CreatedDate
    private Date createDate;

    @LastModifiedDate
    private Date updateDate;
}
