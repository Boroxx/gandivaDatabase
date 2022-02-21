package com.boristenelsen.app.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexResponse {
    public List<String> memoryAdress;
    public List<Integer> offsets;
    public List<String> types;

}
