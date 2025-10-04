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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author YISivlay
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(Model model,
                            HttpServletRequest request,
                            @RequestParam(value = "error", required = false)
                            Boolean error,
                            @RequestParam(value = "oauth2Error", required = false)
                            String oauth2Error,
                            @RequestParam(value = "error_description", required = false)
                            String errorDescription,
                            @RequestParam(value = "logout", required = false)
                            String logout) {
        handleAuthenticationResults(model,
                                    request,
                                    error,
                                    oauth2Error,
                                    errorDescription,
                                    logout);
        return "login";
    }

    private void handleAuthenticationResults(Model model,
                                             HttpServletRequest request,
                                             Boolean error,
                                             String oauth2Error,
                                             String errorDescription,
                                             String logout) {

        HttpSession session = request.getSession();
        if (logout != null) {
            model.addAttribute("successMessage",
                               "You have been logged out successfully.");
        }
        if (error != null) {
            handleFormLoginError(model,
                                 session,
                                 error);
        }
        if (oauth2Error != null) {
            handleOAuth2Error(model,
                              errorDescription);
        }
        handleSessionErrors(model,
                            session);
        handleQueryStringErrors(model,
                                request);
    }

    private void handleFormLoginError(Model model,
                                      HttpSession session,
                                      Boolean error) {
        if (error) {
            Exception authException = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            if (authException != null) {
                model.addAttribute("errorMessage",
                                   "Login failed: " + authException.getMessage());
            } else {
                model.addAttribute("errorMessage",
                                   "Invalid username or password.");
            }
        } else {
            model.addAttribute("errorMessage",
                               "Authentication error. Please try again.");
        }
    }

    private void handleOAuth2Error(Model model,
                                   String errorDescription) {
        if (errorDescription != null) {
            model.addAttribute("errorMessage",
                               "Social login error: " + errorDescription);
        } else {
            model.addAttribute("errorMessage",
                               "Social authentication failed. Please try again.");
        }
    }

    private void handleSessionErrors(Model model,
                                     HttpSession session) {
        String sessionError = (String) session.getAttribute("OAUTH2_ERROR");
        if (sessionError != null) {
            model.addAttribute("errorMessage",
                               sessionError);
            session.removeAttribute("OAUTH2_ERROR");
        }
    }

    private void handleQueryStringErrors(Model model,
                                         HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString != null) {
            if (queryString.contains("error=access_denied")) {
                model.addAttribute("errorMessage",
                                   "Access denied. Permission was not granted.");
            } else if (queryString.contains("error=unauthorized_client")) {
                model.addAttribute("errorMessage",
                                   "Client not authorized. Check OAuth configuration.");
            } else if (queryString.contains("error=invalid_request")) {
                model.addAttribute("errorMessage",
                                   "Invalid request. Please try again.");
            }
        }
    }

    @GetMapping("/login/clear")
    public String clearErrors(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("OAUTH2_ERROR");
        session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        session.removeAttribute("WebAuthenticationDetails");
        return "redirect:/login?cleared=true";
    }

}
