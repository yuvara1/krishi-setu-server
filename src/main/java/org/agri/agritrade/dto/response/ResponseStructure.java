package org.agri.agritrade.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseStructure<T> {
    private int statusCode;
    private String message;
    private T data;
}
