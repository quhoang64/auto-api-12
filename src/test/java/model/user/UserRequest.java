package model.user;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private List<UserAddressRequest> addresses;

    public static UserRequest getDefault(){
        return UserRequest.builder()
                .firstName("Donal")
                .lastName("Trumb")
                .middleName("Leo")
                .birthday("01-02-1992")
                .email("")
                .phone("0971844992")
                .addresses(new ArrayList<>())
                .build();
    }

}
