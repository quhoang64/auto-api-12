package model.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressRequest {
    private String streetNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String state;
    private String zip;
    private String country;

    public static UserAddressRequest getDefault(){
        return UserAddressRequest.builder()
                .streetNumber("123")
                .street("Main St")
                .ward("Ward 7")
                .district("District 7")
                .city("Thu Duc")
                .state("Ho Chi Minh")
                .zip("34343")
                .country("VN")
                .build();
    }
}
