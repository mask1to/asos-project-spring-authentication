package net.javaguides.springboot.web;

import com.authy.AuthyApiClient;
import com.authy.AuthyException;
import com.authy.api.Token;
import com.authy.api.Tokens;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.service.UserService;
import net.javaguides.springboot.web.dto.SecretCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthyLoginController {
    @Autowired
    ApplicationEventPublisher eventPublisher;

    private UserService userService;

    @Value("${authy.api}")
    private String API_KEY;

    public AuthyLoginController(UserService userService) {
        super();
        this.userService = userService;
    }

    @ModelAttribute("user")
    public SecretCode SecretCode() {
        return new SecretCode();
    }

    @RequestMapping(value = "/authyLogin", method = RequestMethod.GET)
    public String showLoginForm(HttpServletRequest httpServletRequest, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());

        if (httpServletRequest.isUserInRole("ROLE_PRE_USER") && user.getUsingfa()) {
            return null;
        } else if (httpServletRequest.isUserInRole("ROLE_USER")) {
            return "redirect:/home";
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/authyLogin", method = RequestMethod.POST)
    public String loginUser(@ModelAttribute("user") SecretCode secretCode, HttpSession session, RedirectAttributes redirectAttributes) throws AuthyException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        AuthyApiClient client = new AuthyApiClient(API_KEY);

        Tokens tokens = client.getTokens();
        Token response = tokens.verify(Integer.valueOf(user.getAuthyId()), secretCode.getSecret_code());

        if (response.isOk() && (secretCode.getSecret_code().length() == 7)) {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(auth.getAuthorities());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            return "redirect:/";

        } else {
            redirectAttributes.addFlashAttribute("error", response.getError());
            return "redirect:/authyLogin";
        }
    }
}
