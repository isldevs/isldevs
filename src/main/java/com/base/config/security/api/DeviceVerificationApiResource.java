/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.config.security.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DeviceVerificationApiResource {

    @GetMapping("/oauth2/device_verification")
    public String deviceVerification(@RequestParam(value = "user_code", required = false) String userCode,
                                     Model model) {
        if (userCode == null) {
            model.addAttribute("error", "Missing user_code");
            return "error";
        }

        model.addAttribute("userCode", userCode);
        return "oauth2/device-verification";
    }

    @GetMapping
    public String verificationSuccess(@RequestParam(value = "success", required = false) String success, Model model) {
        model.addAttribute("message", "Device verification successful!");
        return "oauth2/device-verification-success";
    }

}
