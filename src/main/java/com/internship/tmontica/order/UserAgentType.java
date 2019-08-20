package com.internship.tmontica.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.mobile.device.Device;

@Getter
@AllArgsConstructor
public enum UserAgentType {
    MOBILE,
    TABLET,
    PC;

    public static String toString(Device device) {
        if (device.isMobile()) {
            return UserAgentType.MOBILE.toString();
        } else if (device.isTablet()) {
            return UserAgentType.TABLET.toString();
        } else {
            return UserAgentType.PC.toString();
        }
    }
}
