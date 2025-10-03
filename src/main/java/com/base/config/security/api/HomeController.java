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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author YISivlay
 */
@Controller
public class HomeController {

	@GetMapping("/home")
	public String homePage(Model model, @AuthenticationPrincipal Object principal) {
		if (principal instanceof OAuth2User oauth2User) {
			String registrationId = "unknown";
			if (oauth2User.getAttribute("sub") != null) {
				registrationId = "google";
				model.addAttribute("userName", oauth2User.getAttribute("name"));
				model.addAttribute("userEmail", oauth2User.getAttribute("email"));
			}
			else if (oauth2User.getAttribute("login") != null) {
				registrationId = "github";
				model.addAttribute("userName", oauth2User.getAttribute("name"));
				model.addAttribute("userLogin", oauth2User.getAttribute("login"));
			}
			else if (oauth2User.getAttribute("id") != null) {
				registrationId = "facebook";
				model.addAttribute("userName", oauth2User.getAttribute("name"));
				model.addAttribute("userEmail", oauth2User.getAttribute("email"));
				model.addAttribute("userPicture", oauth2User.getAttribute("picture"));
			}
			model.addAttribute("provider", registrationId);
			model.addAttribute("authType", "oauth2");

		}
		else if (principal instanceof UserDetails userDetails) {
			model.addAttribute("userName", userDetails.getUsername());
			model.addAttribute("authType", "form");
			model.addAttribute("roles", userDetails.getAuthorities());
		}

		return "home";
	}

}
