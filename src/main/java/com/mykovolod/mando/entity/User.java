package com.mykovolod.mando.entity;

import com.mykovolod.mando.conts.LangEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String userName;

    @Getter(AccessLevel.NONE)
    private LangEnum preferredLang;

    private LangEnum accountLang;

    public LangEnum getLang() {
        if (preferredLang == null) {
            return accountLang == null ? LangEnum.ENG : accountLang;
        } else {
            return preferredLang;
        }
    }

    public String getFullName() {
        StringBuilder stringBuilder = new StringBuilder();
        if (firstName != null && !firstName.equals("null")) {
            stringBuilder.append(firstName);
        }
        if (lastName != null && !lastName.equals("null")) {
            stringBuilder.append(" ").append(lastName);
        }
        if (userName != null && !userName.equals("null")) {
            stringBuilder.append(" ").append(userName);
        }
        return stringBuilder.toString();
    }
}
